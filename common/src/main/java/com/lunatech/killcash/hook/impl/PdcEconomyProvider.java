package com.lunatech.killcash.hook.impl;

import com.lunatech.killcash.constant.PDCKeys;
import com.lunatech.killcash.hook.EconomyProvider;
import com.lunatech.killcash.pdc.PDCUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * An implementation of {@link EconomyProvider} that stores balance in the player's PersistentDataContainer.
 * This is the standard, zero-dependency storage backend that writes to vanilla player NBT files.
 */
public class PdcEconomyProvider implements EconomyProvider {
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
            double current = PDCUtil.getDouble(onlinePlayer, PDCKeys.BALANCE, 0.0);
            PDCUtil.setDouble(onlinePlayer, PDCKeys.BALANCE, current + amount);
            return true;
        }
        return false;
    }

    @Override
    public boolean withdraw(OfflinePlayer player, double amount) {
        if (amount <= 0) return false;
        if (player != null && player.isOnline() && player.getPlayer() != null) {
            Player onlinePlayer = player.getPlayer();
            double current = PDCUtil.getDouble(onlinePlayer, PDCKeys.BALANCE, 0.0);
            if (current < amount) return false;
            PDCUtil.setDouble(onlinePlayer, PDCKeys.BALANCE, current - amount);
            return true;
        }
        return false;
    }
}
