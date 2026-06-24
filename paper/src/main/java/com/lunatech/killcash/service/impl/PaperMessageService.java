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
}
