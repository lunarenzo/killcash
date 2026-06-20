package com.lunatech.killcash.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.OfflinePlayerArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import com.lunatech.killcash.AbstractKillCash;
import com.lunatech.killcash.KillCash;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import io.github.milkdrinkers.wordweaver.Translation;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

import static com.lunatech.killcash.command.CommandHandler.BASE_PERM;

/**
 * Base command handler for the /killcash command and its subcommands.
 */
final class KillCashCommand extends Command {
    private final KillCash plugin;

    /**
     * Instantiates the killcash command.
     */
    KillCashCommand(KillCash plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandAPICommand command() {
        return new CommandAPICommand("killcash")
            .withHelp("Base KillCash command.", "Base KillCash command.")
            .withPermission(BASE_PERM)
            .withSubcommands(
                commandStats(),
                commandBalance(),
                commandReload(),
                commandHelp(),
                new DumpCommand().command()
            )
            .executes(this::executorKillCash);
    }

    private void executorKillCash(CommandSender sender, CommandArguments args) {
        if (sender instanceof Player player) {
            showStats(player, player);
        } else {
            sender.sendMessage(Translation.as("commands.killcash.help"));
        }
    }

    private CommandAPICommand commandStats() {
        return new CommandAPICommand("stats")
            .withHelp("View player PvP statistics.", "View player PvP statistics.")
            .withPermission(BASE_PERM + ".stats")
            .withArguments(new OfflinePlayerArgument("target").setOptional(true))
            .executes((sender, args) -> {
                OfflinePlayer target = args.getByClassOrDefault("target", OfflinePlayer.class, null);
                if (target == null) {
                    if (sender instanceof Player player) {
                        showStats(player, player);
                    } else {
                        sender.sendMessage(Translation.as("commands.killcash.only-players"));
                    }
                } else {
                    showStats(sender, target);
                }
            });
    }

    private CommandAPICommand commandBalance() {
        return new CommandAPICommand("balance")
            .withHelp("View player economy balance.", "View player economy balance.")
            .withPermission(BASE_PERM + ".balance")
            .withArguments(new OfflinePlayerArgument("target").setOptional(true))
            .executes((sender, args) -> {
                OfflinePlayer target = args.getByClassOrDefault("target", OfflinePlayer.class, null);
                if (target == null) {
                    if (sender instanceof Player player) {
                        showBalance(player, player);
                    } else {
                        sender.sendMessage(Translation.as("commands.killcash.only-players"));
                    }
                } else {
                    showBalance(sender, target);
                }
            });
    }

    private CommandAPICommand commandReload() {
        return new CommandAPICommand("reload")
            .withHelp("Reload config and translations.", "Reload config and translations.")
            .withPermission(BASE_PERM + ".reload")
            .executes((sender, args) -> {
                plugin.onReload();
                sender.sendMessage(Translation.as("commands.killcash.reload.success"));
            });
    }

    private CommandAPICommand commandHelp() {
        return new CommandAPICommand("help")
            .withHelp("Display help information.", "Display help information.")
            .withPermission(BASE_PERM)
            .executes((sender, args) -> {
                sender.sendMessage(Translation.as("commands.killcash.help"));
            });
    }

    private void showStats(CommandSender sender, OfflinePlayer target) {
        double balance = plugin.getHookManager().getEconomyProvider().getBalance(target);

        if (target.isOnline() && target.getPlayer() != null) {
            Player onlineTarget = target.getPlayer();
            int kills = com.lunatech.killcash.pdc.PDCUtil.getInt(onlineTarget, com.lunatech.killcash.constant.PDCKeys.KILLS, 0);
            int deaths = com.lunatech.killcash.pdc.PDCUtil.getInt(onlineTarget, com.lunatech.killcash.constant.PDCKeys.DEATHS, 0);
            int streak = com.lunatech.killcash.pdc.PDCUtil.getInt(onlineTarget, com.lunatech.killcash.constant.PDCKeys.STREAK, 0);

            String node = (sender instanceof Player && ((Player) sender).getUniqueId().equals(target.getUniqueId()))
                ? "commands.killcash.stats.self"
                : "commands.killcash.stats.other-online";

            sender.sendMessage(ColorParser.of(Translation.of(node))
                .with("player", target.getName() != null ? target.getName() : "")
                .with("balance", String.format("%.2f", balance))
                .with("kills", String.valueOf(kills))
                .with("deaths", String.valueOf(deaths))
                .with("streak", String.valueOf(streak))
                .build());
        } else {
            String name = target.getName();
            if (name == null) {
                sender.sendMessage(Translation.as("commands.killcash.stats.player-not-found"));
                return;
            }
            sender.sendMessage(ColorParser.of(Translation.of("commands.killcash.stats.other-offline"))
                .with("player", name)
                .with("balance", String.format("%.2f", balance))
                .build());
        }
    }

    private void showBalance(CommandSender sender, OfflinePlayer target) {
        double balance = plugin.getHookManager().getEconomyProvider().getBalance(target);

        if (sender instanceof Player && ((Player) sender).getUniqueId().equals(target.getUniqueId())) {
            sender.sendMessage(ColorParser.of(Translation.of("commands.killcash.balance.self"))
                .with("balance", String.format("%.2f", balance))
                .build());
        } else {
            String name = target.getName();
            if (name == null) {
                sender.sendMessage(Translation.as("commands.killcash.stats.player-not-found"));
                return;
            }
            sender.sendMessage(ColorParser.of(Translation.of("commands.killcash.balance.other"))
                .with("player", name)
                .with("balance", String.format("%.2f", balance))
                .build());
        }
    }
}
