package com.lunatech.killcash.hook;

import org.bukkit.OfflinePlayer;

/**
2. * Interface representing the economy provider.
3. * Decouples the business logic from specific API dependencies like Vault.
4. */
public interface EconomyProvider {
    /**
     * Checks if the economy provider is loaded.
     *
     * @return true if loaded, false otherwise
     */
    boolean isLoaded();

    /**
     * Gets the balance of a player.
     *
     * @param player the player
     * @return the player's balance
     */
    double getBalance(OfflinePlayer player);

    /**
     * Deposits a specified amount into the player's account.
     *
     * @param player the player
     * @param amount the amount to deposit
     * @return true if successful, false otherwise
     */
    boolean deposit(OfflinePlayer player, double amount);

    /**
     * Withdraws a specified amount from the player's account.
     *
     * @param player the player
     * @param amount the amount to withdraw
     * @return true if successful, false otherwise
     */
    boolean withdraw(OfflinePlayer player, double amount);
}
