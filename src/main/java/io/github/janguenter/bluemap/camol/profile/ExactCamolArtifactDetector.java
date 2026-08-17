/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.camol.profile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.HashSet;
import java.util.Set;

/** Exact-byte activation gate for the All the Mons 1.2.0 Camol artifact. */
public final class ExactCamolArtifactDetector {

    private static final long SIZE = 62_188L;
    private static final String SHA256 =
            "aafdbe962a4bbab97207f747ec52561ea34be9c49a4b044a835da82ff7d45609";
    private static final int MAX_ROOTS = 4096;

    private ExactCamolArtifactDetector() {
    }

    public static boolean matches(Iterable<Path> roots) {
        int count = 0;
        Set<Path> inspected = new HashSet<>();
        for (Path root : roots) {
            if (++count > MAX_ROOTS || Thread.currentThread().isInterrupted()) {
                return false;
            }
            try {
                if (root == null || !Files.isRegularFile(root) || Files.size(root) != SIZE) {
                    continue;
                }
                Path real = root.toRealPath();
                if (inspected.add(real) && SHA256.equals(digest(real))) {
                    return true;
                }
            } catch (IOException exception) {
                return false;
            }
        }
        return false;
    }

    private static String digest(Path path) throws IOException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
        byte[] buffer = new byte[64 * 1024];
        try (InputStream input = Files.newInputStream(path)) {
            int read;
            while ((read = input.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }
}
