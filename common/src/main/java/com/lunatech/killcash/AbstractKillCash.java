package com.lunatech.killcash;

import com.lunatech.killcash.config.ConfigHandler;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractKillCash extends JavaPlugin {
    private static AbstractKillCash instance;

    /**
     * Gets plugin instance.
     *
     * @return the plugin instance
     */
    public static AbstractKillCash getInstance() {
        return AbstractKillCash.instance;
    }

    AbstractKillCash() {
        AbstractKillCash.instance = this;
    }

    /**
     * Gets config handler.
     *
     * @return the config handler
     */
    public abstract @NotNull ConfigHandler getConfigHandler();
}
