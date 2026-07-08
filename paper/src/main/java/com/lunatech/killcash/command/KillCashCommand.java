package com.lunatech.killcash.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import com.lunatech.killcash.AbstractKillCash;
import com.lunatech.killcash.KillCash;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import io.github.milkdrinkers.wordweaver.Translation;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import dev.jorel.commandapi.arguments.DoubleArgument;

import static com.lunatech.killcash.command.CommandHandler.BASE_PERM;

/**
 * Base command handler for the /killcash command and its subcommands.
 */
final class KillCashCommand extends Command {
    private final KillCash plugin;

    /**
     * Instantiates the killcash command.
     */
    KillCashCommand(AbstractKillCash plugin) {
        this.plugin = (KillCash) plugin;
    }

    @Override
    public CommandAPICommand command() {
        return new CommandAPICommand("killcash")
            .withHelp("Base KillCash command.", "Base KillCash command.")
            .withPermission(BASE_PERM)
            .withSubcommands(
                commandStats(),
                commandBalance(),
                commandPay(),
                commandBaltop(),
                commandGive(),
                commandTake(),
                commandConvert(),
                commandReload(),
                commandReloadConfig(),
                commandReloadLang(),
                commandReloadAll(),
                commandHelp()
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
            .withArguments(new StringArgument("target")
                .replaceSuggestions(ArgumentSuggestions.stringCollection(unused ->
                    plugin.getServer().getOnlinePlayers().stream().map(Player::getName).toList()
                ))
                .setOptional(true)
            )
            .executes((sender, args) -> {
                String targetName = args.getByClassOrDefault("target", String.class, null);
                if (targetName == null) {
                    if (sender instanceof Player player) {
                        showStats(player, player);
                    } else {
                        sender.sendMessage(Translation.as("commands.killcash.only-players"));
                    }
                } else {
                    OfflinePlayer target = resolvePlayer(targetName);
                    showStats(sender, target);
                }
            });
    }

    private CommandAPICommand commandBalance() {
        return new CommandAPICommand("balance")
            .withHelp("View player economy balance.", "View player economy balance.")
            .withPermission(BASE_PERM + ".balance")
            .withArguments(new StringArgument("target")
                .replaceSuggestions(ArgumentSuggestions.stringCollection(unused ->
                    plugin.getServer().getOnlinePlayers().stream().map(Player::getName).toList()
                ))
                .setOptional(true)
            )
            .executes((sender, args) -> {
                String targetName = args.getByClassOrDefault("target", String.class, null);
                if (targetName == null) {
                    if (sender instanceof Player player) {
                        showBalance(player, player);
                    } else {
                        sender.sendMessage(Translation.as("commands.killcash.only-players"));
                    }
                } else {
                    OfflinePlayer target = resolvePlayer(targetName);
                    showBalance(sender, target);
                }
            });
    }

    private CommandAPICommand commandReload() {
        return new CommandAPICommand("reload")
            .withHelp("Reload plugin configuration, translations, or everything.", "Reload plugin configuration, translations, or everything.")
            .withPermission(BASE_PERM + ".reload")
            .withSubcommands(
                new CommandAPICommand("config")
                    .withHelp("Reload config and database configurations only.", "Reload config and database configurations only.")
                    .executes((sender, args) -> {
                        plugin.reloadConfigOnly();
                        sender.sendMessage(Translation.as("commands.killcash.reload.config.success"));
                    }),
                new CommandAPICommand("lang")
                    .withHelp("Reload translation files only.", "Reload translation files only.")
                    .executes((sender, args) -> {
                        plugin.reloadLangOnly();
                        sender.sendMessage(Translation.as("commands.killcash.reload.lang.success"));
                    }),
                new CommandAPICommand("all")
                    .withHelp("Reload the entirety of the plugin.", "Reload the entirety of the plugin.")
                    .executes((sender, args) -> {
                        plugin.onReload();
                        sender.sendMessage(Translation.as("commands.killcash.reload.success"));
                    })
            )
            .executes((sender, args) -> {
                plugin.onReload();
                sender.sendMessage(Translation.as("commands.killcash.reload.success"));
            });
    }

    private CommandAPICommand commandReloadConfig() {
        return new CommandAPICommand("reloadconfig")
            .withHelp("Reload config and database configurations only.", "Reload config and database configurations only.")
            .withPermission(BASE_PERM + ".reload")
            .executes((sender, args) -> {
                plugin.reloadConfigOnly();
                sender.sendMessage(Translation.as("commands.killcash.reload.config.success"));
            });
    }

    private CommandAPICommand commandReloadLang() {
        return new CommandAPICommand("reloadlang")
            .withHelp("Reload translation files only.", "Reload translation files only.")
            .withPermission(BASE_PERM + ".reload")
            .executes((sender, args) -> {
                plugin.reloadLangOnly();
                sender.sendMessage(Translation.as("commands.killcash.reload.lang.success"));
            });
    }

    private CommandAPICommand commandReloadAll() {
        return new CommandAPICommand("reloadall")
            .withHelp("Reload the entirety of the plugin.", "Reload the entirety of the plugin.")
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
                .papi(onlineTarget)
                .with("player", target.getName() != null ? target.getName() : "")
                .with("balance", String.format("%.2f", balance))
                .with("kills", String.valueOf(kills))
                .with("deaths", String.valueOf(deaths))
                .with("streak", String.valueOf(streak))
                .build());
        } else {
            String name = target.getName();
            if (name == null || (!target.hasPlayedBefore() && plugin.getServer().getPlayer(name) == null)) {
                sender.sendMessage(Translation.as("commands.killcash.stats.player-not-found"));
                return;
            }
            ColorParser parser = ColorParser.of(Translation.of("commands.killcash.stats.other-offline"));
            if (sender instanceof Player) {
                parser.papi((Player) sender);
            }
            sender.sendMessage(parser
                .with("player", name)
                .with("balance", String.format("%.2f", balance))
                .build());
        }
    }

    private void showBalance(CommandSender sender, OfflinePlayer target) {
        double balance = plugin.getHookManager().getEconomyProvider().getBalance(target);

        if (sender instanceof Player && ((Player) sender).getUniqueId().equals(target.getUniqueId())) {
            sender.sendMessage(ColorParser.of(Translation.of("commands.killcash.balance.self"))
                .papi((Player) sender)
                .with("balance", String.format("%.2f", balance))
                .build());
        } else {
            String name = target.getName();
            if (name == null || (!target.hasPlayedBefore() && plugin.getServer().getPlayer(name) == null)) {
                sender.sendMessage(Translation.as("commands.killcash.stats.player-not-found"));
                return;
            }
            ColorParser parser = ColorParser.of(Translation.of("commands.killcash.balance.other"));
            if (target.isOnline() && target.getPlayer() != null) {
                parser.papi(target.getPlayer());
            } else if (sender instanceof Player) {
                parser.papi((Player) sender);
            }
            sender.sendMessage(parser
                .with("player", name)
                .with("balance", String.format("%.2f", balance))
                .build());
        }
    }

    private OfflinePlayer resolvePlayer(String targetName) {
        Player onlinePlayer = plugin.getServer().getPlayer(targetName);
        if (onlinePlayer != null) {
            return onlinePlayer;
        }
        if (com.lunatech.killcash.hook.Hook.Floodgate.isLoaded()) {
            com.lunatech.killcash.hook.floodgate.FloodgateHook floodgate = com.lunatech.killcash.hook.Hook.getFloodgateHook();
            String prefix = floodgate.getPlayerPrefix();
            if (targetName.startsWith(prefix)) {
                UUID bedrockUuid = floodgate.resolveBedrockUuid(targetName);
                if (bedrockUuid != null) {
                    return plugin.getServer().getOfflinePlayer(bedrockUuid);
                }
            }
        }
        @SuppressWarnings("deprecation")
        OfflinePlayer target = plugin.getServer().getOfflinePlayer(targetName);
        return target;
    }

    private static final long LEADERBOARD_CACHE_DURATION_MS = 5 * 60 * 1000;
    private long lastLeaderboardUpdate = 0;
    private List<LeaderboardEntry> cachedLeaderboard = new ArrayList<>();
    private boolean isUpdatingLeaderboard = false;

    private record LeaderboardEntry(String name, double balance) {}

    private void updateLeaderboardAsync() {
        if (isUpdatingLeaderboard) {
            return;
        }
        isUpdatingLeaderboard = true;

        io.github.milkdrinkers.threadutil.Scheduler.async(() -> {
            try {
                OfflinePlayer[] offlinePlayers = plugin.getServer().getOfflinePlayers();
                List<LeaderboardEntry> entries = new ArrayList<>();
                var economy = plugin.getHookManager().getEconomyProvider();

                for (OfflinePlayer player : offlinePlayers) {
                    if (player.getName() == null) continue;
                    double balance = economy.getBalance(player);
                    entries.add(new LeaderboardEntry(player.getName(), balance));
                }

                entries.sort((a, b) -> Double.compare(b.balance(), a.balance()));

                List<LeaderboardEntry> top10 = entries.stream().limit(10).toList();

                io.github.milkdrinkers.threadutil.Scheduler.sync(() -> {
                    cachedLeaderboard = top10;
                    lastLeaderboardUpdate = System.currentTimeMillis();
                    isUpdatingLeaderboard = false;
                }).execute();
            } catch (Throwable t) {
                isUpdatingLeaderboard = false;
            }
        }).execute();
    }

    private CommandAPICommand commandPay() {
        return new CommandAPICommand("pay")
            .withHelp("Pay another player with your killcash balance.", "Pay another player with your killcash balance.")
            .withPermission(BASE_PERM + ".pay")
            .withArguments(
                new StringArgument("target")
                    .replaceSuggestions(ArgumentSuggestions.stringCollection(unused ->
                        plugin.getServer().getOnlinePlayers().stream().map(Player::getName).toList()
                    )),
                new DoubleArgument("amount", 0.01)
            )
            .executes((sender, args) -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(Translation.as("commands.killcash.only-players"));
                    return;
                }

                String targetName = args.getByClassOrDefault("target", String.class, null);
                Double amount = args.getByClassOrDefault("amount", Double.class, null);

                if (targetName == null || amount == null) {
                    return;
                }

                if (amount <= 0) {
                    player.sendMessage(ColorParser.of(Translation.of("commands.killcash.pay.invalid-amount")).build());
                    return;
                }

                OfflinePlayer target = resolvePlayer(targetName);
                if (!target.isOnline() || target.getPlayer() == null) {
                    player.sendMessage(ColorParser.of(Translation.of("commands.killcash.pay.player-offline"))
                        .with("player", target.getName() != null ? target.getName() : targetName)
                        .build());
                    return;
                }

                if (target.getUniqueId().equals(player.getUniqueId())) {
                    player.sendMessage(ColorParser.of(Translation.of("commands.killcash.pay.self-payment")).build());
                    return;
                }

                double senderBalance = plugin.getHookManager().getEconomyProvider().getBalance(player);
                if (senderBalance < amount) {
                    player.sendMessage(ColorParser.of(Translation.of("commands.killcash.pay.insufficient-funds"))
                        .with("amount", String.format("%.2f", amount))
                        .with("balance", String.format("%.2f", senderBalance))
                        .build());
                    return;
                }

                boolean withdrawSuccess = plugin.getHookManager().getEconomyProvider().withdraw(player, amount);
                if (withdrawSuccess) {
                    boolean depositSuccess = plugin.getHookManager().getEconomyProvider().deposit(target, amount);
                    if (depositSuccess) {
                        player.sendMessage(ColorParser.of(Translation.of("commands.killcash.pay.success-sender"))
                            .with("amount", String.format("%.2f", amount))
                            .with("receiver", target.getName() != null ? target.getName() : targetName)
                            .build());

                        if (target.isOnline() && target.getPlayer() != null) {
                            target.getPlayer().sendMessage(ColorParser.of(Translation.of("commands.killcash.pay.success-receiver"))
                                .with("amount", String.format("%.2f", amount))
                                .with("sender", player.getName())
                                .build());
                        }
                    } else {
                        plugin.getHookManager().getEconomyProvider().deposit(player, amount);
                    }
                }
            });
    }

    private CommandAPICommand commandBaltop() {
        return new CommandAPICommand("baltop")
            .withAliases("balancetop")
            .withHelp("View top 10 players with the highest balance.", "View top 10 players with the highest balance.")
            .withPermission(BASE_PERM + ".baltop")
            .executes((sender, args) -> {
                long now = System.currentTimeMillis();
                if (cachedLeaderboard.isEmpty() || (now - lastLeaderboardUpdate > LEADERBOARD_CACHE_DURATION_MS)) {
                    updateLeaderboardAsync();
                }

                if (cachedLeaderboard.isEmpty()) {
                    sender.sendMessage(ColorParser.of("<gray>Leaderboard is currently generating, please wait...</gray>").build());
                    return;
                }

                sender.sendMessage(ColorParser.of(Translation.of("commands.killcash.baltop.header")).build());
                for (int i = 0; i < cachedLeaderboard.size(); i++) {
                    LeaderboardEntry entry = cachedLeaderboard.get(i);
                    sender.sendMessage(ColorParser.of(Translation.of("commands.killcash.baltop.entry"))
                        .with("pos", String.valueOf(i + 1))
                        .with("player", entry.name())
                        .with("balance", String.format("%.2f", entry.balance()))
                        .build());
                }
            });
    }

    private CommandAPICommand commandGive() {
        return new CommandAPICommand("give")
            .withHelp("Give killcash currency to players.", "Give killcash currency to players.")
            .withPermission(BASE_PERM + ".give")
            .withArguments(
                new StringArgument("target")
                    .replaceSuggestions(ArgumentSuggestions.stringCollection(unused -> {
                        List<String> suggestions = new ArrayList<>();
                        suggestions.add("all");
                        suggestions.add("*");
                        plugin.getServer().getOnlinePlayers().forEach(p -> suggestions.add(p.getName()));
                        return suggestions;
                    })),
                new DoubleArgument("amount", 0.01)
            )
            .executes((sender, args) -> {
                String targetStr = args.getByClassOrDefault("target", String.class, null);
                Double amount = args.getByClassOrDefault("amount", Double.class, null);
                if (targetStr == null || amount == null) return;

                if (amount <= 0) {
                    sender.sendMessage(ColorParser.of(Translation.of("commands.killcash.give.invalid-amount")).build());
                    return;
                }

                var economy = plugin.getHookManager().getEconomyProvider();
                if (targetStr.equalsIgnoreCase("all") || targetStr.equals("*")) {
                    var onlinePlayers = plugin.getServer().getOnlinePlayers();
                    if (onlinePlayers.isEmpty()) {
                        sender.sendMessage(ColorParser.of(Translation.of("commands.killcash.give.no-players")).build());
                        return;
                    }
                    for (Player onlinePlayer : onlinePlayers) {
                        economy.deposit(onlinePlayer, amount);
                        onlinePlayer.sendMessage(ColorParser.of(Translation.of("commands.killcash.give.success-receiver"))
                            .with("amount", String.format("%.2f", amount))
                            .build());
                    }
                    sender.sendMessage(ColorParser.of(Translation.of("commands.killcash.give.success-sender"))
                        .with("amount", String.format("%.2f", amount))
                        .with("target", "all online players")
                        .build());
                } else if (targetStr.contains(",")) {
                    String[] names = targetStr.split(",");
                    List<String> processedNames = new ArrayList<>();
                    for (String name : names) {
                        name = name.trim();
                        if (name.isEmpty()) continue;
                        OfflinePlayer target = resolvePlayer(name);
                        String resolvedName = target.getName();
                        if (resolvedName == null || (!target.hasPlayedBefore() && plugin.getServer().getPlayer(resolvedName) == null)) {
                            sender.sendMessage(ColorParser.of(Translation.of("commands.killcash.stats.player-not-found"))
                                .with("player", name)
                                .build());
                            continue;
                        }
                        economy.deposit(target, amount);
                        processedNames.add(resolvedName);
                        if (target.isOnline() && target.getPlayer() != null) {
                            target.getPlayer().sendMessage(ColorParser.of(Translation.of("commands.killcash.give.success-receiver"))
                                .with("amount", String.format("%.2f", amount))
                                .build());
                        }
                    }
                    if (!processedNames.isEmpty()) {
                        sender.sendMessage(ColorParser.of(Translation.of("commands.killcash.give.success-sender"))
                            .with("amount", String.format("%.2f", amount))
                            .with("target", String.join(", ", processedNames))
                            .build());
                    }
                } else {
                    OfflinePlayer target = resolvePlayer(targetStr);
                    String resolvedName = target.getName();
                    if (resolvedName == null || (!target.hasPlayedBefore() && plugin.getServer().getPlayer(resolvedName) == null)) {
                        sender.sendMessage(ColorParser.of(Translation.of("commands.killcash.stats.player-not-found"))
                            .with("player", targetStr)
                            .build());
                        return;
                    }
                    economy.deposit(target, amount);
                    if (target.isOnline() && target.getPlayer() != null) {
                        target.getPlayer().sendMessage(ColorParser.of(Translation.of("commands.killcash.give.success-receiver"))
                            .with("amount", String.format("%.2f", amount))
                            .build());
                    }
                    sender.sendMessage(ColorParser.of(Translation.of("commands.killcash.give.success-sender"))
                        .with("amount", String.format("%.2f", amount))
                        .with("target", resolvedName)
                        .build());
                }
            });
    }

    private CommandAPICommand commandTake() {
        return new CommandAPICommand("take")
            .withHelp("Take killcash currency from players.", "Take killcash currency from players.")
            .withPermission(BASE_PERM + ".take")
            .withArguments(
                new StringArgument("target")
                    .replaceSuggestions(ArgumentSuggestions.stringCollection(unused -> {
                        List<String> suggestions = new ArrayList<>();
                        suggestions.add("all");
                        suggestions.add("*");
                        plugin.getServer().getOnlinePlayers().forEach(p -> suggestions.add(p.getName()));
                        return suggestions;
                    })),
                new DoubleArgument("amount", 0.01)
            )
            .executes((sender, args) -> {
                String targetStr = args.getByClassOrDefault("target", String.class, null);
                Double amount = args.getByClassOrDefault("amount", Double.class, null);
                if (targetStr == null || amount == null) return;

                if (amount <= 0) {
                    sender.sendMessage(ColorParser.of(Translation.of("commands.killcash.take.invalid-amount")).build());
                    return;
                }

                var economy = plugin.getHookManager().getEconomyProvider();
                if (targetStr.equalsIgnoreCase("all") || targetStr.equals("*")) {
                    var onlinePlayers = plugin.getServer().getOnlinePlayers();
                    if (onlinePlayers.isEmpty()) {
                        sender.sendMessage(ColorParser.of(Translation.of("commands.killcash.take.no-players")).build());
                        return;
                    }
                    for (Player onlinePlayer : onlinePlayers) {
                        economy.withdraw(onlinePlayer, amount);
                        onlinePlayer.sendMessage(ColorParser.of(Translation.of("commands.killcash.take.success-receiver"))
                            .with("amount", String.format("%.2f", amount))
                            .build());
                    }
                    sender.sendMessage(ColorParser.of(Translation.of("commands.killcash.take.success-sender"))
                        .with("amount", String.format("%.2f", amount))
                        .with("target", "all online players")
                        .build());
                } else if (targetStr.contains(",")) {
                    String[] names = targetStr.split(",");
                    List<String> processedNames = new ArrayList<>();
                    for (String name : names) {
                        name = name.trim();
                        if (name.isEmpty()) continue;
                        OfflinePlayer target = resolvePlayer(name);
                        String resolvedName = target.getName();
                        if (resolvedName == null || (!target.hasPlayedBefore() && plugin.getServer().getPlayer(resolvedName) == null)) {
                            sender.sendMessage(ColorParser.of(Translation.of("commands.killcash.stats.player-not-found"))
                                .with("player", name)
                                .build());
                            continue;
                        }
                        economy.withdraw(target, amount);
                        processedNames.add(resolvedName);
                        if (target.isOnline() && target.getPlayer() != null) {
                            target.getPlayer().sendMessage(ColorParser.of(Translation.of("commands.killcash.take.success-receiver"))
                                .with("amount", String.format("%.2f", amount))
                                .build());
                        }
                    }
                    if (!processedNames.isEmpty()) {
                        sender.sendMessage(ColorParser.of(Translation.of("commands.killcash.take.success-sender"))
                            .with("amount", String.format("%.2f", amount))
                            .with("target", String.join(", ", processedNames))
                            .build());
                    }
                } else {
                    OfflinePlayer target = resolvePlayer(targetStr);
                    String resolvedName = target.getName();
                    if (resolvedName == null || (!target.hasPlayedBefore() && plugin.getServer().getPlayer(resolvedName) == null)) {
                        sender.sendMessage(ColorParser.of(Translation.of("commands.killcash.stats.player-not-found"))
                            .with("player", targetStr)
                            .build());
                        return;
                    }
                    economy.withdraw(target, amount);
                    if (target.isOnline() && target.getPlayer() != null) {
                        target.getPlayer().sendMessage(ColorParser.of(Translation.of("commands.killcash.take.success-receiver"))
                            .with("amount", String.format("%.2f", amount))
                            .build());
                    }
                    sender.sendMessage(ColorParser.of(Translation.of("commands.killcash.take.success-sender"))
                        .with("amount", String.format("%.2f", amount))
                        .with("target", resolvedName)
                        .build());
                }
            });
    }

    private CommandAPICommand commandConvert() {
        return new CommandAPICommand("convert")
            .withAliases("exchange")
            .withHelp("Convert KillCash tokens to main server economy currency.", "Convert KillCash tokens to main server economy currency.")
            .withPermission(BASE_PERM + ".convert")
            .withArguments(new DoubleArgument("amount", 0.01))
            .executes((sender, args) -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(Translation.as("commands.killcash.only-players"));
                    return;
                }

                var config = plugin.getConfigHandler().getConfig();
                if (config == null || config.conversionSettings == null || !config.conversionSettings.enabled) {
                    player.sendMessage(ColorParser.of(Translation.of("commands.killcash.convert.disabled")).build());
                    return;
                }

                Double amountToConvert = args.getByClassOrDefault("amount", Double.class, null);
                if (amountToConvert == null || amountToConvert <= 0) {
                    player.sendMessage(ColorParser.of(Translation.of("commands.killcash.convert.invalid-amount")).build());
                    return;
                }

                double minConvert = config.conversionSettings.minimumConversion;
                if (amountToConvert < minConvert) {
                    player.sendMessage(ColorParser.of(Translation.of("commands.killcash.convert.min-amount"))
                        .with("min", String.format("%.2f", minConvert))
                        .build());
                    return;
                }

                // 1. Check if player has enough internal KillCash balance
                var economy = plugin.getHookManager().getEconomyProvider();
                double currentKillCash = economy.getBalance(player);
                if (currentKillCash < amountToConvert) {
                    player.sendMessage(ColorParser.of(Translation.of("commands.killcash.convert.insufficient-funds"))
                        .with("amount", String.format("%.2f", amountToConvert))
                        .with("balance", String.format("%.2f", currentKillCash))
                        .build());
                    return;
                }

                // 2. Check if Vault is loaded/available
                if (!com.lunatech.killcash.hook.Hook.Vault.isLoaded()) {
                    player.sendMessage(ColorParser.of(Translation.of("commands.killcash.convert.vault-error")).build());
                    return;
                }
                var vaultHook = com.lunatech.killcash.hook.Hook.getVaultHook();
                if (!vaultHook.isEconomyLoaded()) {
                    player.sendMessage(ColorParser.of(Translation.of("commands.killcash.convert.vault-error")).build());
                    return;
                }

                // 3. Calculate EssentialsX payout
                double rate = config.conversionSettings.exchangeRate;
                double essentialsPayout = amountToConvert * rate;

                // 4. Execute transaction (deduct KillCash first, then deposit Vault)
                boolean withdrawSuccess = economy.withdraw(player, amountToConvert);
                if (withdrawSuccess) {
                    boolean depositSuccess = vaultHook.deposit(player, essentialsPayout);
                    if (depositSuccess) {
                        player.sendMessage(ColorParser.of(Translation.of("commands.killcash.convert.success"))
                            .with("amount", String.format("%.2f", amountToConvert))
                            .with("payout", String.format("%.2f", essentialsPayout))
                            .build());
                    } else {
                        // Rollback on failure
                        economy.deposit(player, amountToConvert);
                        player.sendMessage(ColorParser.of(Translation.of("commands.killcash.convert.error-processing")).build());
                    }
                } else {
                    player.sendMessage(ColorParser.of(Translation.of("commands.killcash.convert.error-processing")).build());
                }
            });
    }
}
