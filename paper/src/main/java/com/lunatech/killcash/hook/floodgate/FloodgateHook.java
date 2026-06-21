package com.lunatech.killcash.hook.floodgate;

import com.lunatech.killcash.AbstractKillCash;
import com.lunatech.killcash.KillCash;
import com.lunatech.killcash.hook.AbstractHook;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class FloodgateHook extends AbstractHook {

    public FloodgateHook(KillCash plugin) {
        super(plugin);
    }

    @Override
    public void onEnable(AbstractKillCash plugin) {
    }

    @Override
    public void onDisable(AbstractKillCash plugin) {
    }

    @Override
    public boolean isHookLoaded() {
        return isPluginEnabled("Floodgate");
    }

    /**
     * Check if a player is playing via Geyser/Floodgate Bedrock.
     *
     * @param uuid the player's UUID
     * @return true if they are a Bedrock player
     */
    public boolean isBedrockPlayer(@NotNull UUID uuid) {
        if (!isHookLoaded()) {
            return false;
        }
        try {
            return FloodgateApi.getInstance().isFloodgatePlayer(uuid);
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * Get the prefix used by Floodgate Bedrock players.
     *
     * @return the prefix, default to "." if not loaded
     */
    public @NotNull String getPlayerPrefix() {
        if (!isHookLoaded()) {
            return ".";
        }
        try {
            return FloodgateApi.getInstance().getPlayerPrefix();
        } catch (Throwable t) {
            return ".";
        }
    }

    /**
     * Resolves the UUID of a Bedrock player by their gamertag/name (which may include the prefix).
     *
     * @param name the player's name
     * @return the resolved UUID, or null
     */
    public @Nullable UUID resolveBedrockUuid(@NotNull String name) {
        if (!isHookLoaded()) {
            return null;
        }
        try {
            String prefix = getPlayerPrefix();
            String gamertag = name;
            if (name.startsWith(prefix)) {
                gamertag = name.substring(prefix.length());
            }
            return FloodgateApi.getInstance().getUuidFor(gamertag).join();
        } catch (Throwable t) {
            return null;
        }
    }
}
