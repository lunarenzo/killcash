package com.lunatech.killcash.hook.packetevents;

import com.github.retrooper.packetevents.PacketEvents;
import com.lunatech.killcash.AbstractKillCash;
import com.lunatech.killcash.KillCash;
import com.lunatech.killcash.hook.AbstractHook;
import com.lunatech.killcash.hook.Hook;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;

/**
 * A hook that enables API for PacketEvents.
 */
public class PacketEventsHook extends AbstractHook {
    /**
     * Instantiates a new PacketEvents hook.
     *
     * @param plugin the plugin instance
     */
    public PacketEventsHook(KillCash plugin) {
        super(plugin);
    }

    @Override
    public void onLoad(AbstractKillCash plugin) {
        if (!isPluginPresent(Hook.PacketEvents.getPluginName()))
            return;

        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(getPlugin()));
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable(AbstractKillCash plugin) {
        if (!isPluginEnabled(Hook.PacketEvents.getPluginName()))
            return;

        PacketEvents.getAPI().init();
    }

    @Override
    public void onDisable(AbstractKillCash plugin) {
        if (!isPluginEnabled(Hook.PacketEvents.getPluginName()))
            return;

        PacketEvents.getAPI().terminate();
    }

    @Override
    public boolean isHookLoaded() {
        return isPluginPresent(Hook.PacketEvents.getPluginName()) && PacketEvents.getAPI().isLoaded();
    }
}
