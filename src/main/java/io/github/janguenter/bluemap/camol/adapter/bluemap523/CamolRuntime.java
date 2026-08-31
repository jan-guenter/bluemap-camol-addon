/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.camol.adapter.bluemap523;

import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;

import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/** Small shared activation and bounded-diagnostic state. */
final class CamolRuntime {

    static final CamolRuntime INSTANCE = new CamolRuntime();

    private static final int MAX_DIAGNOSTICS = 8;

    private final AtomicBoolean active = new AtomicBoolean();
    private final AtomicInteger diagnostics = new AtomicInteger();
    private final Map<ResourcePack, VariantRendererCatalog> catalogs = new WeakHashMap<>();
    private final Set<String> tracedStages = ConcurrentHashMap.newKeySet();

    private CamolRuntime() {
    }

    boolean active() {
        return active.get();
    }

    void activate() {
        active.set(true);
    }

    void inactive(String reason) {
        active.set(false);
        report("inactive-" + reason);
    }

    synchronized void catalog(ResourcePack pack, VariantRendererCatalog catalog) {
        catalogs.put(pack, catalog);
    }

    synchronized VariantRendererCatalog catalog(ResourcePack pack) {
        return catalogs.get(pack);
    }

    void report(String reason) {
        int count = diagnostics.incrementAndGet();
        if (count <= MAX_DIAGNOSTICS) {
            System.err.println("BlueMap Camol add-on: " + reason + ".");
        }
    }

    void failSoft(String stage, Error error) {
        active.set(false);
        int count = diagnostics.incrementAndGet();
        if (count > MAX_DIAGNOSTICS) {
            return;
        }
        System.err.println("BlueMap Camol add-on fail-soft at " + stage + ": "
                + error.getClass().getName() + ": " + error.getMessage());
        StackTraceElement[] trace = error.getStackTrace();
        int limit = Math.min(trace.length, 24);
        for (int index = 0; index < limit; index++) {
            System.err.println("\tat " + trace[index]);
        }
        if (trace.length > limit) {
            System.err.println("\t... " + (trace.length - limit) + " more");
        }
    }

    void trace(String stage) {
        if (tracedStages.add(stage)) {
            System.err.println("BlueMap Camol add-on trace: " + stage + ".");
        }
    }

    void failSoftMinimal(String stage, Error error) {
        active.set(false);
        int count = diagnostics.incrementAndGet();
        if (count <= MAX_DIAGNOSTICS) {
            System.err.println("BlueMap Camol add-on broad fail-soft at " + stage
                    + ": " + error.getClass().getName() + ".");
        }
    }

    @SuppressWarnings("removal")
    static void throwIfFatal(Error error) {
        if (error instanceof OutOfMemoryError outOfMemory) {
            throw outOfMemory;
        }
        if (error instanceof ThreadDeath threadDeath) {
            throw threadDeath;
        }
    }
}
