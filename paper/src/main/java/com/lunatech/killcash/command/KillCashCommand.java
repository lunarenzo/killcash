package com.lunatech.killcash.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.executors.CommandArguments;
import com.lunatech.killcash.AbstractKillCash;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import org.bukkit.command.CommandSender;

import static com.lunatech.killcash.command.CommandHandler.BASE_PERM;

/**
 * Class containing the code for the killcash command.
 */
final class KillCashCommand extends Command {
    private final AbstractKillCash plugin;

    /**
     * Instantiates and registers a new command.
     */
    KillCashCommand(AbstractKillCash plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandAPICommand command() {
        return new CommandAPICommand("killcash")
            .withHelp("Base command.", "Base command.")
            .withPermission(BASE_PERM)
            .withSubcommands(
                new TranslationCommand().command(),
                new DumpCommand().command()
            )
            .executes(this::executorKillCash);
    }

    private void executorKillCash(CommandSender sender, CommandArguments args) {
        sender.sendMessage(
            ColorParser.of("<white>Read more about CommandAPI &9<click:open_url:'https://commandapi.jorel.dev/9.0.3/'>here</click><white>.")
                .legacy() // Parse legacy color codes
                .build()
        );
    }
}
