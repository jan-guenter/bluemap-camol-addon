/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.camol.adapter.bluemap522;

import de.bluecolored.bluemap.core.world.mca.MCAUtil;
import de.bluecolored.bluenbt.BlueNBT;
import de.bluecolored.bluenbt.NBTWriter;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CamolChunkDataTest {

    @Test
    void readsExactModernAttachmentPathAndValueCodec() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (NBTWriter writer = new NBTWriter(bytes)) {
            writer.beginCompound();
            writer.name("neoforge:attachments").beginCompound();
            writer.name("camol:solid_camo").beginCompound();
            writer.name("123456789").beginCompound();
            writer.name("camoType").value("solid");
            writer.name("state").beginCompound();
            writer.name("Name").value("minecraft:diamond_block");
            writer.endCompound();
            writer.endCompound();
            writer.endCompound();
            writer.endCompound();
            writer.endCompound();
        }

        CamolChunkData data = MCAUtil.addCommonNbtSettings(new BlueNBT()).read(
                new ByteArrayInputStream(bytes.toByteArray()),
                CamolChunkData.class
        );

        CamolChunkData.CamoPosition position =
                data.attachments().camouflage().get("123456789");
        assertEquals("solid", position.camoType());
        assertEquals("minecraft:diamond_block", position.state().getId().getFormatted());
    }
}
