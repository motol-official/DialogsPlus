package com.motol.dialogsplus;

import com.motol.dialogsplus.config.ConfigManager;
import com.motol.dialogsplus.dialog.ActionExecutor;
import com.motol.dialogsplus.editor.MenuEditor;
import com.motol.dialogsplus.storage.MenuManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ReloadCommand implements CommandExecutor {

    private final MenuManager menuManager;
    private final ConfigManager config;
    private final JavaPlugin plugin;
    private final MenuEditor editor;

    public ReloadCommand(MenuManager menuManager, ConfigManager config, JavaPlugin plugin) {
        this.menuManager = menuManager;
        this.config = config;
        this.plugin = plugin;
        this.editor = new MenuEditor(menuManager, new ActionExecutor(menuManager));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("edit")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("The editor can only be used in-game.");
                return true;
            }
            if (!player.hasPermission(MenuEditor.PERMISSION)) {
                player.sendMessage(config.getMessage("no-permission"));
                return true;
            }
            if (args.length >= 2) {
                editor.open(player, args[1]);
            } else {
                editor.open(player);
            }
            return true;
        }

        if (!sender.hasPermission(config.getReloadPermission())) {
            sender.sendMessage(config.getMessage("no-permission"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("Usage: /dialogsplus <reload|export|import|edit> ...");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                menuManager.loadAll();
                sender.sendMessage(config.getMessage("reload-success"));
            }
            case "export" -> {
                if (args.length < 2) {
                    sender.sendMessage("Usage: /dialogsplus export <menu-id>");
                    return true;
                }
                exportMenu(sender, args[1]);
            }
            case "import" -> {
                if (args.length < 2) {
                    sender.sendMessage("Usage: /dialogsplus import <filename.yml>");
                    return true;
                }
                importMenu(sender, args[1]);
            }
            default -> sender.sendMessage("Usage: /dialogsplus <reload|export|import|edit> ...");
        }

        return true;
    }

    private void exportMenu(CommandSender sender, String menuId) {
        File source = new File(plugin.getDataFolder(), "menus/" + menuId + ".yml");
        if (!source.exists()) {
            sender.sendMessage("No menu file found for id: " + menuId);
            return;
        }

        File exportsFolder = new File(plugin.getDataFolder(), "exports");
        if (!exportsFolder.exists()) {
            exportsFolder.mkdirs();
        }

        File target = new File(exportsFolder, menuId + ".yml");
        try {
            Files.copy(source.toPath(), target.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            sender.sendMessage("Exported to plugins/DialogsPlus/exports/" + menuId + ".yml");
        } catch (IOException e) {
            sender.sendMessage("Export failed: " + e.getMessage());
        }
    }

    private void importMenu(CommandSender sender, String fileName) {
        File importsFolder = new File(plugin.getDataFolder(), "imports");
        if (!importsFolder.exists()) {
            importsFolder.mkdirs();
        }

        File source = new File(importsFolder, fileName);
        if (!source.exists()) {
            sender.sendMessage("Place the file in plugins/DialogsPlus/imports/ first: " + fileName);
            return;
        }

        File target = new File(plugin.getDataFolder(), "menus/" + fileName);
        try {
            Files.copy(source.toPath(), target.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            menuManager.loadAll();
            sender.sendMessage("Imported and loaded: " + fileName);
        } catch (IOException e) {
            sender.sendMessage("Import failed: " + e.getMessage());
        }
    }
}