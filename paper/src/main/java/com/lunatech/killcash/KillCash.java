package com.lunatech.killcash;

import com.lunatech.killcash.api.KillCashAPI;
import com.lunatech.killcash.command.CommandHandler;
import com.lunatech.killcash.config.ConfigHandler;
import com.lunatech.killcash.cooldown.CooldownHandler;
import com.lunatech.killcash.database.handler.DatabaseHandler;
import com.lunatech.killcash.hook.HookManager;
import com.lunatech.killcash.listener.ListenerHandler;
import com.lunatech.killcash.messaging.MessagingHandler;
import com.lunatech.killcash.threadutil.SchedulerHandler;
import com.lunatech.killcash.translation.TranslationHandler;
import com.lunatech.killcash.updatechecker.UpdateHandler;
import com.lunatech.killcash.utility.DB;
import com.lunatech.killcash.utility.Logger;
import com.lunatech.killcash.utility.Messaging;
import com.lunatech.killcash.cache.KillCooldownCache;
import com.lunatech.killcash.service.KillRewardService;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Main class.
 */
@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class KillCash extends AbstractKillCash {
    private static KillCash instance;

    // Handlers/Managers
    private ConfigHandler configHandler;
    private TranslationHandler translationHandler;
    private DatabaseHandler databaseHandler;
    private MessagingHandler messagingHandler;
    private HookManager hookManager;
    private CommandHandler commandHandler;
    private ListenerHandler listenerHandler;
    private UpdateHandler updateHandler;
    private SchedulerHandler schedulerHandler;
    private CooldownHandler cooldownHandler;
    private KillCashAPIProvider apiHandler;
    private KillCooldownCache cooldownCache;
    private KillRewardService killRewardService;

    // Handlers list (defines order of load/enable/disable)
    private List<? extends Reloadable> handlers;

    @Override
    public void onLoad() {
        instance = this;

        configHandler = new ConfigHandler(this);
        translationHandler = new TranslationHandler(configHandler);
        databaseHandler = DatabaseHandler.builder()
            .withConfigHandler(configHandler)
            .withLogger(getComponentLogger())
            .withMigrate(true)
            .build();
        messagingHandler = MessagingHandler.builder()
            .withLogger(getComponentLogger())
            .withName(getName())
            .build();
        hookManager = new HookManager(this);
        cooldownCache = new com.lunatech.killcash.cache.impl.ConcurrentKillCooldownCache();
        killRewardService = new com.lunatech.killcash.service.impl.DefaultKillRewardService(configHandler, hookManager.getEconomyProvider(), cooldownCache);
        commandHandler = new CommandHandler(this);
        listenerHandler = new ListenerHandler(this);
        updateHandler = new UpdateHandler(this);
        schedulerHandler = new SchedulerHandler();
        cooldownHandler = new CooldownHandler();
        apiHandler = new KillCashAPIProvider(this);

        handlers = List.of(
            configHandler,
            translationHandler,
            databaseHandler,
            messagingHandler,
            hookManager,
            commandHandler,
            listenerHandler,
            updateHandler,
            schedulerHandler,
            cooldownHandler,
            cooldownHandler,
            apiHandler
        );

        DB.init(databaseHandler);
        Messaging.init(messagingHandler);
        for (Reloadable handler : handlers)
            handler.onLoad(instance);
    }

    @Override
    public void onEnable() {
        for (Reloadable handler : handlers)
            handler.onEnable(instance);

        if (!DB.isStarted()) {
            Logger.get().warn(ColorParser.of("<yellow>Database handler failed to start. Database support has been disabled.").build());
            Bukkit.getPluginManager().disablePlugin(this);
        }

        if (!Messaging.isReady() && configHandler.getDatabaseConfig().messaging.enabled) {
            Logger.get().warn(ColorParser.of("<yellow>Messaging handler failed to start. Messaging support has been disabled.").build());
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        for (Reloadable handler : handlers.reversed()) // If reverse doesn't work implement a new List with your desired disable order
            handler.onDisable(instance);
    }

    /**
     * Use to reload the entire plugin.
     */
    public void onReload() {
        onDisable();
        onLoad();
        onEnable();
    }

    @Override
    public @NotNull ConfigHandler getConfigHandler() {
        return configHandler;
    }

    public @NotNull HookManager getHookManager() {
        return hookManager;
    }

    public @NotNull UpdateHandler getUpdateHandler() {
        return updateHandler;
    }

    public @NotNull KillCashAPI getApiHandler() {
        return apiHandler;
    }

    public @NotNull KillCooldownCache getCooldownCache() {
        return cooldownCache;
    }

    public @NotNull KillRewardService getKillRewardService() {
        return killRewardService;
    }
}
