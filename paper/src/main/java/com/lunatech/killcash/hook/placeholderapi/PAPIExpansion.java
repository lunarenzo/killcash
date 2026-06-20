package com.lunatech.killcash.hook.placeholderapi;

import com.lunatech.killcash.KillCash;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A PlaceholderAPI expansion. Read the docs at <a href="https://wiki.placeholderapi.com/developers/creating-a-placeholderexpansion/">here</a> on how to register your custom placeholders.
 */
public class PAPIExpansion extends PlaceholderExpansion {
    private final KillCash plugin;

    public PAPIExpansion(KillCash plugin) {
        this.plugin = plugin;
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public @NotNull String getIdentifier() {
        return plugin.getPluginMeta().getName().replace(' ', '_').toLowerCase();
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getPluginMeta().getAuthors());
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true; // This needs to be true, or PlaceholderAPI will unregister the expansion during a plugin reload.
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer p, @NotNull String params) {
        if (p == null) {
            return null;
        }

        org.bukkit.entity.Player player = p.getPlayer();
        if (player == null) {
            return switch (params) {
                case "kills", "deaths", "streak" -> "0";
                case "balance" -> String.format("%.2f", plugin.getHookManager().getEconomyProvider().getBalance(p));
                default -> null;
            };
        }

        return switch (params) {
            case "kills" -> String.valueOf(com.lunatech.killcash.pdc.PDCUtil.getInt(player, com.lunatech.killcash.constant.PDCKeys.KILLS, 0));
            case "deaths" -> String.valueOf(com.lunatech.killcash.pdc.PDCUtil.getInt(player, com.lunatech.killcash.constant.PDCKeys.DEATHS, 0));
            case "streak" -> String.valueOf(com.lunatech.killcash.pdc.PDCUtil.getInt(player, com.lunatech.killcash.constant.PDCKeys.STREAK, 0));
            case "balance" -> String.format("%.2f", plugin.getHookManager().getEconomyProvider().getBalance(p));
            default -> null;
        };
    }
}
