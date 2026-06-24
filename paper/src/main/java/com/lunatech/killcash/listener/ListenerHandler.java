package com.lunatech.killcash.listener;

import com.lunatech.killcash.AbstractKillCash;
import com.lunatech.killcash.KillCash;
import com.lunatech.killcash.Reloadable;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

/**
 * A class to handle registration of event listeners.
 */
public class ListenerHandler implements Reloadable {
    private final KillCash plugin;
    private final List<Listener> listeners = new ArrayList<>();

    /**
     * Instantiates a the Listener handler.
     *
     * @param plugin the plugin instance
     */
    public ListenerHandler(KillCash plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEnable(AbstractKillCash plugin) {
        listeners.clear(); // Clear the list to avoid duplicate listeners when reloading the plugin
        listeners.add(new com.lunatech.killcash.listener.player.PlayerDeathListener(this.plugin.getKillRewardService()));
        listeners.add(new com.lunatech.killcash.listener.player.PlayerJoinListener(this.plugin.getKillRewardService()));
        listeners.add(new com.lunatech.killcash.listener.player.PlayerQuitListener(this.plugin.getKillRewardService()));

        // Register listeners here
        for (Listener listener : listeners) {
            plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        }
    }
}
