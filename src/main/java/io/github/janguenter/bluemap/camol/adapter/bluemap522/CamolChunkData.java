/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.camol.adapter.bluemap522;

import de.bluecolored.bluemap.core.world.BlockState;
import de.bluecolored.bluenbt.NBTName;

import java.util.Map;

/** Narrow BlueNBT projection of Camol's two NeoForge chunk attachments. */
public final class CamolChunkData {

    @NBTName("neoforge:attachments")
    private Attachments attachments;

    public CamolChunkData() {
    }

    Attachments attachments() {
        return attachments;
    }

    /** Attachment namespace compound. */
    public static final class Attachments {

        @NBTName("camol:solid_camo")
        private Map<String, CamoPosition> camouflage;

        @NBTName("camol:camo")
        private Map<String, BlockState> legacyCamouflage;

        public Attachments() {
        }

        Map<String, CamoPosition> camouflage() {
            return camouflage;
        }

        Map<String, BlockState> legacyCamouflage() {
            return legacyCamouflage;
        }
    }

    /** Exact value codec used by the current attachment. */
    public static final class CamoPosition {

        @NBTName("camoType")
        private String camoType;

        @NBTName("state")
        private BlockState state;

        public CamoPosition() {
        }

        String camoType() {
            return camoType;
        }

        BlockState state() {
            return state;
        }
    }
}
