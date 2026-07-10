package com.lunatech.killcash.hook;

import org.bukkit.entity.Player;

/**
 * Robust, reflection-based hook for PvPManager to handle combat log events.
 * Using reflection keeps it compile-safe and runtime-safe regardless of whether PvPManager is installed.
 */
public final class PvPManagerHook {
    private static boolean initialized = false;
    private static boolean available = false;
    private static java.lang.reflect.Method getPlayerManagerMethod;
    private static java.lang.reflect.Method getCombatPlayerMethod;
    private static java.lang.reflect.Method getEnemyMethod;
    private static java.lang.reflect.Method getPlayerMethod;
    private static Object pvpManagerInstance;

    private static void init() {
        if (initialized) return;
        initialized = true;
        try {
            org.bukkit.plugin.Plugin plugin = org.bukkit.Bukkit.getPluginManager().getPlugin("PvPManager");
            if (plugin != null && plugin.isEnabled()) {
                Class<?> pvpManagerClass = Class.forName("me.chancesd.pvpmanager.PvPManager");
                pvpManagerInstance = pvpManagerClass.getMethod("getInstance").invoke(null);
                if (pvpManagerInstance != null) {
                    getPlayerManagerMethod = pvpManagerClass.getMethod("getPlayerManager");
                    Object playerManager = getPlayerManagerMethod.invoke(pvpManagerInstance);
                    if (playerManager != null) {
                        getCombatPlayerMethod = playerManager.getClass().getMethod("get", Player.class);
                        Class<?> combatPlayerClass = Class.forName("me.chancesd.pvpmanager.player.CombatPlayer");
                        getEnemyMethod = combatPlayerClass.getMethod("getEnemy");
                        getPlayerMethod = combatPlayerClass.getMethod("getPlayer");
                        available = true;
                    }
                }
            }
        } catch (Exception ignored) {
            available = false;
        }
    }

    /**
     * Resolves the combat opponent (enemy) that tagged the victim player prior to logout.
     *
     * @param victim the player who died or logged out
     * @return the Player opponent if found, or null
     */
    public static Player getCombatOpponent(Player victim) {
        init();
        if (!available) return null;
        try {
            Object playerManager = getPlayerManagerMethod.invoke(pvpManagerInstance);
            if (playerManager != null) {
                Object combatPlayer = getCombatPlayerMethod.invoke(playerManager, victim);
                if (combatPlayer != null) {
                    Object enemyCombatPlayer = getEnemyMethod.invoke(combatPlayer);
                    if (enemyCombatPlayer != null) {
                        return (Player) getPlayerMethod.invoke(enemyCombatPlayer);
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
