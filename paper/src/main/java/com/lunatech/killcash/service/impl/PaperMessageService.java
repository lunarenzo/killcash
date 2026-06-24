package com.lunatech.killcash.service.impl;

import com.lunatech.killcash.service.MessageService;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import io.github.milkdrinkers.wordweaver.Translation;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * Paper implementation of {@link MessageService}.
 * Uses Paper-specific {@link ColorParser} to process translation keys and colors.
 */
public class PaperMessageService implements MessageService {

    @Override
    public void sendMessage(Player player, String translationKey) {
        player.sendMessage(ColorParser.of(Translation.of(translationKey)).build());
    }

    @Override
    public void sendMessage(Player player, String translationKey, Map<String, String> placeholders) {
        var parser = ColorParser.of(Translation.of(translationKey));
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            parser.with(entry.getKey(), entry.getValue());
        }
        player.sendMessage(parser.build());
    }

    @Override
    public void sendActionBar(Player player, String translationKey, Map<String, String> placeholders) {
        var parser = ColorParser.of(Translation.of(translationKey));
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            parser.with(entry.getKey(), entry.getValue());
        }
        player.sendActionBar(parser.build());
    }

    @Override
    public void broadcast(String translationKey, Map<String, String> placeholders) {
        var parser = ColorParser.of(Translation.of(translationKey));
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            parser.with(entry.getKey(), entry.getValue());
        }
        org.bukkit.Bukkit.broadcast(parser.build());
    }

    @Override
    public void broadcastRaw(String message, Map<String, String> placeholders) {
        var parser = ColorParser.of(message);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            parser.with(entry.getKey(), entry.getValue());
        }
        org.bukkit.Bukkit.broadcast(parser.build());
    }

    @Override
    public void playSound(Player player, org.bukkit.Sound sound, float volume, float pitch) {
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    @Override
    public void broadcastSound(org.bukkit.Sound sound, float volume, float pitch) {
        for (Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), sound, volume, pitch);
        }
    }

    private Class<?> getCraftClass(String path) throws ClassNotFoundException {
        String serverPkg = org.bukkit.Bukkit.getServer().getClass().getPackage().getName();
        try {
            return Class.forName("org.bukkit.craftbukkit." + path);
        } catch (ClassNotFoundException e) {
            String version = serverPkg.substring(serverPkg.lastIndexOf('.') + 1);
            return Class.forName("org.bukkit.craftbukkit." + version + "." + path);
        }
    }

    @Override
    public void playLightningEffect(Player player, org.bukkit.Location location) {
        try {
            Class<?> craftWorldClass = getCraftClass("CraftWorld");
            if (!craftWorldClass.isInstance(location.getWorld())) return;

            Object craftWorld = craftWorldClass.cast(location.getWorld());
            Object nmsLevel = craftWorld.getClass().getMethod("getHandle").invoke(craftWorld);

            Class<?> entityTypeClass = Class.forName("net.minecraft.world.entity.EntityType");
            Object lightningType = entityTypeClass.getField("LIGHTNING_BOLT").get(null);

            Class<?> lightningClass = Class.forName("net.minecraft.world.entity.LightningBolt");
            Class<?> levelClass = Class.forName("net.minecraft.world.level.Level");
            java.lang.reflect.Constructor<?> lightningConstructor = lightningClass.getConstructor(entityTypeClass, levelClass);
            Object lightningEntity = lightningConstructor.newInstance(lightningType, nmsLevel);

            lightningClass.getMethod("setPos", double.class, double.class, double.class)
                .invoke(lightningEntity, location.getX(), location.getY(), location.getZ());

            Class<?> entityClass = Class.forName("net.minecraft.world.entity.Entity");
            Class<?> packetClass = Class.forName("net.minecraft.network.protocol.game.ClientboundAddEntityPacket");
            java.lang.reflect.Constructor<?> packetConstructor = packetClass.getConstructor(entityClass);
            Object packet = packetConstructor.newInstance(lightningEntity);

            Class<?> craftPlayerClass = getCraftClass("entity.CraftPlayer");
            Object craftPlayer = craftPlayerClass.cast(player);
            Object serverPlayer = craftPlayerClass.getMethod("getHandle").invoke(craftPlayer);

            java.lang.reflect.Field connectionField;
            try {
                connectionField = serverPlayer.getClass().getField("connection");
            } catch (NoSuchFieldException e) {
                connectionField = serverPlayer.getClass().getField("b");
            }
            Object connection = connectionField.get(serverPlayer);

            Class<?> basePacketClass = Class.forName("net.minecraft.network.protocol.Packet");
            connection.getClass().getMethod("send", basePacketClass).invoke(connection, packet);
        } catch (Throwable t) {
            // Failsafe: Play flash particle and thunder sound
            player.spawnParticle(org.bukkit.Particle.FLASH, location, 1);
            player.playSound(location, org.bukkit.Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
        }
    }
}
