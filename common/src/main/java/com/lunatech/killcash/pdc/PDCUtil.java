package com.lunatech.killcash.pdc;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;

/**
 * Utility class providing stateless helpers for interacting with {@link PersistentDataContainer}.
 */
public final class PDCUtil {
    private PDCUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Gets an integer value from a holder's container.
     *
     * @param holder       the data holder (e.g. Player)
     * @param key          the namespaced key
     * @param defaultValue the default value if not set
     * @return the integer value, or default
     */
    public static int getInt(PersistentDataHolder holder, NamespacedKey key, int defaultValue) {
        PersistentDataContainer pdc = holder.getPersistentDataContainer();
        Integer val = pdc.get(key, PersistentDataType.INTEGER);
        return val != null ? val : defaultValue;
    }

    /**
     * Sets an integer value in a holder's container.
     *
     * @param holder the data holder
     * @param key    the namespaced key
     * @param value  the value to set
     */
    public static void setInt(PersistentDataHolder holder, NamespacedKey key, int value) {
        PersistentDataContainer pdc = holder.getPersistentDataContainer();
        pdc.set(key, PersistentDataType.INTEGER, value);
    }

    /**
     * Increments an integer value in a holder's container by a specified amount.
     *
     * @param holder the data holder
     * @param key    the namespaced key
     * @param amount the amount to increment by
     */
    public static void incrementInt(PersistentDataHolder holder, NamespacedKey key, int amount) {
        int current = getInt(holder, key, 0);
        setInt(holder, key, current + amount);
    }

    /**
     * Gets a double value from a holder's container.
     *
     * @param holder       the data holder (e.g. Player)
     * @param key          the namespaced key
     * @param defaultValue the default value if not set
     * @return the double value, or default
     */
    public static double getDouble(PersistentDataHolder holder, NamespacedKey key, double defaultValue) {
        PersistentDataContainer pdc = holder.getPersistentDataContainer();
        Double val = pdc.get(key, PersistentDataType.DOUBLE);
        return val != null ? val : defaultValue;
    }

    /**
     * Sets a double value in a holder's container.
     *
     * @param holder the data holder
     * @param key    the namespaced key
     * @param value  the value to set
     */
    public static void setDouble(PersistentDataHolder holder, NamespacedKey key, double value) {
        PersistentDataContainer pdc = holder.getPersistentDataContainer();
        pdc.set(key, PersistentDataType.DOUBLE, value);
    }
}
