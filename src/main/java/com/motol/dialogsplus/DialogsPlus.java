package com.motol.dialogsplus;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.motol.dialogsplus.config.ConfigManager;
import com.motol.dialogsplus.dialog.ActionExecutor;
import com.motol.dialogsplus.storage.MenuManager;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class DialogsPlus extends JavaPlugin {

    private MenuManager menuManager;
    private ActionExecutor actionExecutor;
    private ConfigManager configManager;
    private MenuOpener menuOpener;

    @Override
    public void onEnable() {
        getLogger().info("Dialogs+ enabled");

        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        File readmeFile = new File(getDataFolder(), "README.txt");
        if (!readmeFile.exists()) {
            saveResource("README.txt", false);
        }

        saveDefaultConfig();
        configManager = new ConfigManager(getConfig());

        menuManager = new MenuManager(getDataFolder(), getLogger());
        menuManager.loadAll();

        actionExecutor = new ActionExecutor(menuManager);
        menuOpener = new MenuOpener(menuManager, actionExecutor, configManager);

        getCommand("dialogs").setExecutor(new DialogCommand(menuOpener));
        getCommand("dialogsplus").setExecutor(new ReloadCommand(menuManager, configManager, this));

        registerDynamicCommands();
    }

    private void registerDynamicCommands() {
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            Commands commands = event.registrar();

            for (var entry : configManager.getCommandMappings().entrySet()) {
                String menuId = entry.getKey();
                String rawCommand = entry.getValue();
                if (rawCommand == null || rawCommand.isBlank()) {
                    continue;
                }

                String label = rawCommand.startsWith("/") ? rawCommand.substring(1) : rawCommand;


                Command<CommandSourceStack> opener = ctx -> {
                    if (ctx.getSource().getSender() instanceof Player player) {
                        menuOpener.open(player, menuId);
                    } else {
                        ctx.getSource().getSender().sendPlainMessage("Players only.");
                    }
                    return Command.SINGLE_SUCCESS;
                };

                commands.register(
                        Commands.literal(label)
                                .executes(opener)
                                .then(Commands.argument("args", StringArgumentType.greedyString())
                                        .executes(opener))
                                .build(),
                        "Opens the " + menuId + " menu"
                );
            }
        });
    }

    @Override
    public void onDisable() {
        getLogger().info("Dialogs+ disabled");
    }

    public MenuManager getMenuManager() {
        return menuManager;
    }
}