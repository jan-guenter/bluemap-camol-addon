/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.camol.adapter.bluemap522;

import de.bluecolored.bluemap.core.world.BlockState;
import de.bluecolored.bluemap.core.world.block.Block;
import de.bluecolored.bluemap.core.world.block.BlockAccess;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import de.bluecolored.bluemap.core.world.block.ExtendedBlock;
import de.bluecolored.bluemap.core.world.mca.MCAUtil;
import de.bluecolored.bluemap.core.world.mca.MCAWorld;
import de.bluecolored.bluenbt.BlueNBT;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

/** Bounded, read-only Camol attachment lookup against the rendered MCA chunk. */
final class CamolAttachmentReader {

    private static final long RECHECK_NANOS = 500_000_000L;
    private static final int MAX_COMPRESSED_BYTES = 32 * 1024 * 1024;
    private static final int MAX_ENTRIES = 65_536;
    private static final Field BLOCK_ACCESS = blockAccessField();

    private final CamolRuntime runtime;
    private final Map<ChunkKey, CachedChunk> cache = new ConcurrentHashMap<>();

    CamolAttachmentReader(CamolRuntime runtime) {
        this.runtime = runtime;
    }

    BlockState camouflageAt(BlockNeighborhood neighborhood) {
        if (!runtime.active() || BLOCK_ACCESS == null) {
            return null;
        }
        Path dimension = dimensionFolder(neighborhood);
        if (dimension == null) {
            return null;
        }
        int chunkX = neighborhood.getX() >> 4;
        int chunkZ = neighborhood.getZ() >> 4;
        ChunkKey key = new ChunkKey(dimension, chunkX, chunkZ);
        CachedChunk chunk = cache.compute(key, this::refresh);
        return chunk.states().get(blockPosLong(
                neighborhood.getX(),
                neighborhood.getY(),
                neighborhood.getZ()
        ));
    }

    private CachedChunk refresh(ChunkKey key, CachedChunk previous) {
        long now = System.nanoTime();
        if (previous != null && now - previous.checkedAt() < RECHECK_NANOS) {
            return previous;
        }
        Path region = regionPath(key);
        try {
            if (!Files.isRegularFile(region)) {
                return new CachedChunk(now, -1L, -1L, Map.of());
            }
            long modified = Files.getLastModifiedTime(region).toMillis();
            long size = Files.size(region);
            if (previous != null && previous.modified() == modified && previous.size() == size) {
                return new CachedChunk(now, modified, size, previous.states());
            }
            return new CachedChunk(now, modified, size, readChunk(region, key.chunkX(), key.chunkZ()));
        } catch (IOException | RuntimeException exception) {
            runtime.report("chunk-attachment-read-failed-" + exception.getClass().getSimpleName());
            Map<Long, BlockState> fallback = previous == null ? Map.of() : previous.states();
            return new CachedChunk(now, -2L, -2L, fallback);
        }
    }

    private static Map<Long, BlockState> readChunk(Path region, int chunkX, int chunkZ)
            throws IOException {
        int index = (chunkX & 31) + (chunkZ & 31) * 32;
        try (FileChannel channel = FileChannel.open(region, StandardOpenOption.READ)) {
            ByteBuffer location = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
            readFully(channel, location, index * 4L);
            int packed = location.flip().getInt();
            int sector = packed >>> 8;
            int sectors = packed & 0xff;
            if (sector == 0 || sectors == 0) {
                return Map.of();
            }

            ByteBuffer header = ByteBuffer.allocate(5).order(ByteOrder.BIG_ENDIAN);
            readFully(channel, header, sector * 4096L);
            header.flip();
            int length = header.getInt();
            int compression = Byte.toUnsignedInt(header.get());
            int compressedLength = length - 1;
            int allocated = sectors * 4096 - 5;
            if (compressedLength <= 0 || compressedLength > allocated
                    || compressedLength > MAX_COMPRESSED_BYTES || (compression & 0x80) != 0) {
                throw new IOException("invalid or external chunk payload");
            }

            ByteBuffer payload = ByteBuffer.allocate(compressedLength);
            readFully(channel, payload, sector * 4096L + 5L);
            try (InputStream decoded = decompress(payload.array(), compression)) {
                BlueNBT blueNbt = MCAUtil.addCommonNbtSettings(new BlueNBT());
                CamolChunkData data = blueNbt.read(decoded, CamolChunkData.class);
                return normalize(data);
            }
        }
    }

    private static Map<Long, BlockState> normalize(CamolChunkData data) {
        if (data == null || data.attachments() == null) {
            return Map.of();
        }
        HashMap<Long, BlockState> states = new HashMap<>();
        Map<String, CamolChunkData.CamoPosition> current = data.attachments().camouflage();
        if (current != null) {
            for (Map.Entry<String, CamolChunkData.CamoPosition> entry : current.entrySet()) {
                if (states.size() >= MAX_ENTRIES) {
                    break;
                }
                CamolChunkData.CamoPosition value = entry.getValue();
                if (value != null && value.state() != null
                        && (value.camoType() == null
                        || "normal".equals(value.camoType())
                        || "solid".equals(value.camoType()))) {
                    put(states, entry.getKey(), value.state());
                }
            }
        }
        // Camol's own watch-time migration applies legacy values last.
        putLegacy(states, data.attachments().legacyCamouflage());
        return Map.copyOf(states);
    }

    private static void putLegacy(Map<Long, BlockState> target, Map<String, BlockState> legacy) {
        if (legacy == null) {
            return;
        }
        for (Map.Entry<String, BlockState> entry : legacy.entrySet()) {
            if (target.size() >= MAX_ENTRIES) {
                break;
            }
            if (entry.getValue() != null) {
                put(target, entry.getKey(), entry.getValue());
            }
        }
    }

    private static void put(Map<Long, BlockState> target, String key, BlockState state) {
        try {
            target.put(Long.parseLong(key), state);
        } catch (NumberFormatException ignored) {
            // One malformed position is not allowed to hide valid entries.
        }
    }

    private static InputStream decompress(byte[] payload, int compression) throws IOException {
        InputStream raw = new ByteArrayInputStream(payload);
        return switch (compression) {
            case 1 -> new GZIPInputStream(raw);
            case 2 -> new InflaterInputStream(raw);
            case 3 -> new BufferedInputStream(raw);
            default -> throw new IOException("unsupported chunk compression " + compression);
        };
    }

    private static void readFully(FileChannel channel, ByteBuffer target, long position)
            throws IOException {
        while (target.hasRemaining()) {
            int read = channel.read(target, position);
            if (read < 0) {
                throw new IOException("truncated region file");
            }
            position += read;
        }
    }

    private static Path regionPath(ChunkKey key) {
        return key.dimension().resolve("region").resolve(
                "r." + (key.chunkX() >> 5) + "." + (key.chunkZ() >> 5) + ".mca"
        );
    }

    private static Path dimensionFolder(BlockNeighborhood neighborhood) {
        try {
            Object access = BLOCK_ACCESS.get(neighborhood);
            if (access instanceof Block block && block.getWorld() instanceof MCAWorld world) {
                return world.getDimensionFolder().toAbsolutePath().normalize();
            }
        } catch (IllegalAccessException | RuntimeException exception) {
            return null;
        }
        return null;
    }

    private static Field blockAccessField() {
        try {
            Field field = ExtendedBlock.class.getDeclaredField("blockAccess");
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException | RuntimeException exception) {
            return null;
        }
    }

    private static long blockPosLong(int x, int y, int z) {
        return ((long) x & 0x3ffffffL) << 38
                | ((long) z & 0x3ffffffL) << 12
                | (long) y & 0xfffL;
    }

    private record ChunkKey(Path dimension, int chunkX, int chunkZ) {
    }

    private record CachedChunk(
            long checkedAt,
            long modified,
            long size,
            Map<Long, BlockState> states
    ) {
    }
}
