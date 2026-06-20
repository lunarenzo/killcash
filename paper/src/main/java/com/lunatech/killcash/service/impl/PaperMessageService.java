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
        ColorParser parser = ColorParser.of(Translation.of(translationKey));
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            parser.with(entry.getKey(), entry.getValue());
        }
        player.sendMessage(parser.build());
    }
}
