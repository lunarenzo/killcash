package com.lunatech.killcash.hook.impl;

import com.lunatech.killcash.hook.EconomyProvider;
import org.bukkit.OfflinePlayer;

/**
 * A fallback, no-op implementation of {@link EconomyProvider}.
 * Prevents NullPointerExceptions when no economy system is found on the server.
 */
public class NoOpEconomyHook implements EconomyProvider {
    @Override
    public boolean isLoaded() {
        return false;
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        return 0.0;
    }

    @Override
    public boolean deposit(OfflinePlayer player, double amount) {
        return false;
    }

    @Override
    public boolean withdraw(OfflinePlayer player, double amount) {
        return false;
    }
}
