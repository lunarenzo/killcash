package com.lunatech.killcash.utility;

import com.lunatech.killcash.AbstractKillCash;
import com.lunatech.killcash.config.ConfigHandler;
import com.lunatech.killcash.config.PluginConfig;
import org.jetbrains.annotations.NotNull;

/**
 * Convenience class for accessing {@link ConfigHandler#getConfig}
 */
public final class Cfg {
    /**
     * Convenience method for {@link ConfigHandler#getConfig} to getConnection {@link PluginConfig}
     *
     * @return the config
     */
    @NotNull
    public static PluginConfig get() {
        return AbstractKillCash.getInstance().getConfigHandler().getConfig();
    }
}
