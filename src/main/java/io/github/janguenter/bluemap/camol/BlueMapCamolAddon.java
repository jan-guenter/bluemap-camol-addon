/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.camol;

import io.github.janguenter.bluemap.camol.adapter.bluemap522.AdapterCompatibility;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/** BlueMap add-on entrypoint installed before resource-pack construction. */
public final class BlueMapCamolAddon implements Runnable {

    @Override
    public void run() {
        try {
            if (!AdapterCompatibility.currentRuntimeSupported()) {
                inactive("unsupported BlueMap internal ABI", null);
                return;
            }
            Class<?> adapter = Class.forName(
                    "io.github.janguenter.bluemap.camol.adapter.bluemap522.BlueMap522Adapter",
                    true,
                    BlueMapCamolAddon.class.getClassLoader()
            );
            Method install = adapter.getMethod("install");
            install.invoke(null);
        } catch (InvocationTargetException exception) {
            inactive("adapter initialization failed", exception.getCause());
        } catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
            inactive("adapter unavailable", exception);
        }
    }

    private static void inactive(String reason, Throwable cause) {
        String detail = cause == null ? "" : " (" + cause.getClass().getSimpleName() + ")";
        System.err.println("BlueMap Camol add-on is inactive: " + reason + detail + ".");
    }
}
