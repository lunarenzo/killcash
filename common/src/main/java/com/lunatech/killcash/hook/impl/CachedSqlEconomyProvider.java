package com.lunatech.killcash.hook.impl;

import com.lunatech.killcash.AbstractKillCash;
import com.lunatech.killcash.hook.EconomyProvider;
import com.lunatech.killcash.utility.DB;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

/**
 * A robust database-backed economy provider featuring an in-memory Write-Behind Cache,
 * crash safety (WAL mode integration), and thread-safe batch transaction flushing.
 */
public class CachedSqlEconomyProvider implements EconomyProvider, Listener {
    private final AbstractKillCash plugin;
    private final Map<UUID, Double> tokenCache = new ConcurrentHashMap<>();
    private final Set<UUID> dirtyKeys = ConcurrentHashMap.newKeySet();
    private final String tableName;
    private ScheduledTask flushTask;

    public CachedSqlEconomyProvider(AbstractKillCash plugin) {
        this.plugin = plugin;
        this.tableName = DB.getHandler().getDatabaseConfig().getTablePrefix() + "balances";
    }

    public void start() {
        setupTable();
        startFlushTask();

        // Pre-load online players if plugin is reloaded
        for (org.bukkit.entity.Player player : plugin.getServer().getOnlinePlayers()) {
            loadPlayerBalanceAsync(player.getUniqueId(), player);
        }
    }

    private void setupTable() {
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                 "uuid VARCHAR(36) PRIMARY KEY NOT NULL, " +
                 "balance DOUBLE NOT NULL DEFAULT 0.0" +
                 ")"
             )) {
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getSLF4JLogger().error("[KillCash] Failed to initialize balances table", e);
        }
    }

    private void startFlushTask() {
        flushTask = plugin.getServer().getAsyncScheduler().runAtFixedRate(
            plugin,
            task -> flushDirtyBalances(),
            30L, 30L, TimeUnit.SECONDS
        );
    }

    private void loadPlayerBalanceAsync(UUID uuid, Player player) {
        plugin.getServer().getAsyncScheduler().runNow(plugin, task -> {
            double balance = loadPlayerBalanceSync(uuid);
            
            // If database has no record, check if we can migrate from PDC
            if (balance == 0.0 && player != null && player.getPersistentDataContainer().has(com.lunatech.killcash.constant.PDCKeys.BALANCE, org.bukkit.persistence.PersistentDataType.DOUBLE)) {
                double pdcBalance = com.lunatech.killcash.pdc.PDCUtil.getDouble(player, com.lunatech.killcash.constant.PDCKeys.BALANCE, 0.0);
                if (pdcBalance > 0.0) {
                    balance = pdcBalance;
                    savePlayerBalanceSync(uuid, balance);
                    
                    plugin.getServer().getGlobalRegionScheduler().execute(plugin, () -> {
                        if (player.isOnline()) {
                            player.getPersistentDataContainer().remove(com.lunatech.killcash.constant.PDCKeys.BALANCE);
                            plugin.getSLF4JLogger().info("[KillCash] Migrated " + player.getName() + "'s balance of " + pdcBalance + " from PDC to DATABASE.");
                        }
                    });
                }
            }
            
            tokenCache.put(uuid, balance);
        });
    }

    private double loadPlayerBalanceSync(UUID uuid) {
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT balance FROM " + tableName + " WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("balance");
                }
            }
        } catch (SQLException e) {
            plugin.getSLF4JLogger().error("[KillCash] Failed to load balance for " + uuid, e);
        }
        return 0.0;
    }

    private void savePlayerBalanceSync(UUID uuid, double balance) {
        String query;
        if (DB.getHandler().getDB().equals(com.lunatech.killcash.database.handler.DatabaseType.SQLITE)) {
            query = "INSERT INTO " + tableName + " (uuid, balance) VALUES (?, ?) " +
                    "ON CONFLICT(uuid) DO UPDATE SET balance = excluded.balance";
        } else {
            query = "INSERT INTO " + tableName + " (uuid, balance) VALUES (?, ?) " +
                    "ON DUPLICATE KEY UPDATE balance = VALUES(balance)";
        }

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, uuid.toString());
            ps.setDouble(2, balance);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getSLF4JLogger().error("[KillCash] Failed to save balance for " + uuid, e);
        }
    }

    public void flushDirtyBalances() {
        Set<UUID> toFlush = new HashSet<>(dirtyKeys);
        dirtyKeys.removeAll(toFlush);

        if (toFlush.isEmpty()) return;

        String query;
        if (DB.getHandler().getDB().equals(com.lunatech.killcash.database.handler.DatabaseType.SQLITE)) {
            query = "INSERT INTO " + tableName + " (uuid, balance) VALUES (?, ?) " +
                    "ON CONFLICT(uuid) DO UPDATE SET balance = excluded.balance";
        } else {
            query = "INSERT INTO " + tableName + " (uuid, balance) VALUES (?, ?) " +
                    "ON DUPLICATE KEY UPDATE balance = VALUES(balance)";
        }

        try (Connection conn = DB.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                for (UUID uuid : toFlush) {
                    Double balance = tokenCache.get(uuid);
                    if (balance == null) continue;
                    ps.setString(1, uuid.toString());
                    ps.setDouble(2, balance);
                    ps.addBatch();
                }
                ps.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                plugin.getSLF4JLogger().error("[KillCash] Failed to batch flush balances", e);
                dirtyKeys.addAll(toFlush);
            }
        } catch (SQLException e) {
            plugin.getSLF4JLogger().error("[KillCash] Failed to get database connection for batch flush", e);
            dirtyKeys.addAll(toFlush);
        }
    }

    public void shutdown() {
        if (flushTask != null) {
            flushTask.cancel();
        }
        if (!dirtyKeys.isEmpty()) {
            flushDirtyBalances();
        }
        tokenCache.clear();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        loadPlayerBalanceAsync(event.getPlayer().getUniqueId(), event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (!dirtyKeys.remove(uuid)) {
            tokenCache.remove(uuid);
            return;
        }

        Double balance = tokenCache.get(uuid);
        if (balance == null) return;

        plugin.getServer().getAsyncScheduler().runNow(plugin, task -> {
            savePlayerBalanceSync(uuid, balance);
            tokenCache.remove(uuid);
        });
    }

    @Override
    public boolean isLoaded() {
        return true;
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        if (player == null) return 0.0;
        UUID uuid = player.getUniqueId();

        Double cached = tokenCache.get(uuid);
        if (cached != null) {
            return cached;
        }

        if (player.isOnline()) {
            double balance = loadPlayerBalanceSync(uuid);
            tokenCache.put(uuid, balance);
            return balance;
        }

        return loadPlayerBalanceSync(uuid);
    }

    @Override
    public boolean deposit(OfflinePlayer player, double amount) {
        if (player == null || amount <= 0) return false;
        UUID uuid = player.getUniqueId();

        double current = getBalance(player);
        tokenCache.put(uuid, Math.round((current + amount) * 100.0) / 100.0);
        dirtyKeys.add(uuid);
        return true;
    }

    @Override
    public boolean withdraw(OfflinePlayer player, double amount) {
        if (player == null || amount <= 0) return false;
        UUID uuid = player.getUniqueId();

        double current = getBalance(player);
        if (current + 1E-5 < amount) return false;

        tokenCache.put(uuid, Math.round((current - amount) * 100.0) / 100.0);
        dirtyKeys.add(uuid);
        return true;
    }
}
