package com.lunatech.killcash.command;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIPaperConfig;
import com.lunatech.killcash.AbstractKillCash;
import com.lunatech.killcash.KillCash;
import com.lunatech.killcash.Reloadable;

/**
 * A class to handle registration of commands.
 */
public class CommandHandler implements Reloadable {
    public static final String BASE_PERM = "killcash.command";
    private final KillCash plugin;

    /**
     * Instantiates the Command handler.
     *
     * @param plugin the plugin
     */
    public CommandHandler(KillCash plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onLoad(AbstractKillCash plugin) {
        CommandAPI.onLoad(
            new CommandAPIPaperConfig(plugin)
                .silentLogs(true)
        );
    }

    @Override
    public void onEnable(AbstractKillCash plugin) {
        if (!CommandAPI.isLoaded())
            return;

        CommandAPI.onEnable();

        // Register commands here
        new KillCashCommand(plugin)
            .command()
            .withAliases()
            .register();
    }

    @Override
    public void onDisable(AbstractKillCash plugin) {
        if (!CommandAPI.isLoaded())
            return;

        CommandAPI.onDisable();
    }
}