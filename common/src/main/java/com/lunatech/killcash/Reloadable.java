package com.lunatech.killcash;

/**
 * Implemented in classes that should support being reloaded IE executing the methods during runtime after startup.
 */
public interface Reloadable {
    /**
     * On plugin load.
     */
    default void onLoad(AbstractKillCash plugin) {
    }

    /**
     * On plugin enable.
     */
    default void onEnable(AbstractKillCash plugin) {
    }

    /**
     * On plugin disable.
     */
    default void onDisable(AbstractKillCash plugin) {
    }

}
