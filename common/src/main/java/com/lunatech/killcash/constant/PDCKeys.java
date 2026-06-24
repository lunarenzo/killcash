package com.lunatech.killcash.constant;

import org.bukkit.NamespacedKey;

/**
 * Constant class holding all Bukkit {@link NamespacedKey} instances used for PersistentDataContainer storage.
 */
public final class PDCKeys {
    public static final NamespacedKey KILLS = new NamespacedKey("killcash", "kills");
    public static final NamespacedKey DEATHS = new NamespacedKey("killcash", "deaths");
    public static final NamespacedKey STREAK = new NamespacedKey("killcash", "streak");
    public static final NamespacedKey BALANCE = new NamespacedKey("killcash", "balance");

    private PDCKeys() {
        throw new UnsupportedOperationException("Constant class");
    }
}
