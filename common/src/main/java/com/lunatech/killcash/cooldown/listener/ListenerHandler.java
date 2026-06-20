package com.lunatech.killcash.cooldown.listener;

import com.lunatech.killcash.AbstractKillCash;
import com.lunatech.killcash.Reloadable;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

/**
 * A class to handle registration of event listeners.
 */
@SuppressWarnings("FieldCanBeLocal")
public class ListenerHandler implements Reloadable {
    private final AbstractKillCash plugin;
    private final List<Listener> listeners = new ArrayList<>();

    public ListenerHandler(AbstractKillCash plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onLoad(AbstractKillCash plugin) {
    }

    @Override
    public void onEnable(AbstractKillCash plugin) {
        listeners.clear();
        listeners.add(new CooldownListener(plugin));

        for (Listener listener : listeners) {
            plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        }
    }

    @Override
    public void onDisable(AbstractKillCash plugin) {
    }
}
