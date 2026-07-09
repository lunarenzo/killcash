package com.lunatech.killcash.hook.impl;

import com.lunatech.killcash.AbstractKillCash;
import com.lunatech.killcash.constant.PDCKeys;
import com.lunatech.killcash.hook.EconomyProvider;
import com.lunatech.killcash.pdc.PDCUtil;
import com.lunatech.killcash.utility.DB;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * An implementation of {@link EconomyProvider} that stores balance in the player's PersistentDataContainer.
 * Includes a self-healing, asynchronous lazy migration layer to import balances from the SQL database when switching backends.
 */
public class PdcEconomyProvider implements EconomyProvider, Listener {
    private final AbstractKillCash plugin;

    public PdcEconomyProvider(AbstractKillCash plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // If the player does not have a PDC balance record, attempt to migrate from SQL database asynchronously
        if (!player.getPersistentDataContainer().has(PDCKeys.BALANCE, org.bukkit.persistence.PersistentDataType.DOUBLE)) {
            plugin.getServer().getAsyncScheduler().runNow(plugin, task -> {
                double dbBalance = loadAndClearDatabaseBalance(player.getUniqueId());
                plugin.getServer().getGlobalRegionScheduler().execute(plugin, () -> {
                    if (player.isOnline()) {
                        PDCUtil.setDouble(player, PDCKeys.BALANCE, dbBalance);
                        if (dbBalance > 0.0) {
                            plugin.getSLF4JLogger().info("[KillCash] Migrated " + player.getName() + "'s balance of " + dbBalance + " from DATABASE to PDC.");
                        }
                    }
                });
            });
        }
    }

    private double loadAndClearDatabaseBalance(UUID uuid) {
        if (!DB.isStarted()) return 0.0;
        String tableName = DB.getHandler().getDatabaseConfig().getTablePrefix() + "balances";
        double balance = 0.0;
        try (Connection conn = DB.getConnection()) {
            // 1. Read balance
            try (PreparedStatement ps = conn.prepareStatement("SELECT balance FROM " + tableName + " WHERE uuid = ?")) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        balance = rs.getDouble("balance");
                    }
                }
            }
            // 2. Delete balance if found
            if (balance > 0.0) {
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM " + tableName + " WHERE uuid = ?")) {
                    ps.setString(1, uuid.toString());
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            // Table might not exist yet if SQL was never used, which is completely fine
        }
        return balance;
    }

    @Override
    public boolean isLoaded() {
        return true;
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        if (player != null && player.isOnline() && player.getPlayer() != null) {
            return PDCUtil.getDouble(player.getPlayer(), PDCKeys.BALANCE, 0.0);
        }
        return 0.0;
    }

    @Override
    public boolean deposit(OfflinePlayer player, double amount) {
        if (amount <= 0) return false;
        if (player != null && player.isOnline() && player.getPlayer() != null) {
            Player onlinePlayer = player.getPlayer();
            double current = getBalance(onlinePlayer);
            PDCUtil.setDouble(onlinePlayer, PDCKeys.BALANCE, Math.round((current + amount) * 100.0) / 100.0);
            return true;
        }
        return false;
    }

    @Override
    public boolean withdraw(OfflinePlayer player, double amount) {
        if (amount <= 0) return false;
        if (player != null && player.isOnline() && player.getPlayer() != null) {
            Player onlinePlayer = player.getPlayer();
            double current = getBalance(onlinePlayer);
            if (current + 1E-5 < amount) return false;
            PDCUtil.setDouble(onlinePlayer, PDCKeys.BALANCE, Math.round((current - amount) * 100.0) / 100.0);
            return true;
        }
        return false;
    }
}
