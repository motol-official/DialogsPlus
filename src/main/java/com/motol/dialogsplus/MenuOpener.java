package com.motol.dialogsplus;

import com.motol.dialogsplus.config.ConfigManager;
import com.motol.dialogsplus.dialog.ActionExecutor;
import com.motol.dialogsplus.dialog.DialogBuilder;
import com.motol.dialogsplus.model.Menu;
import com.motol.dialogsplus.storage.MenuManager;
import org.bukkit.entity.Player;

public class MenuOpener {

    private final MenuManager menuManager;
    private final ActionExecutor actionExecutor;
    private final ConfigManager config;

    public MenuOpener(MenuManager menuManager, ActionExecutor actionExecutor, ConfigManager config) {
        this.menuManager = menuManager;
        this.actionExecutor = actionExecutor;
        this.config = config;
    }

    public void open(Player player, String menuId) {
        if (!player.hasPermission(config.getUseDialogsPermission())) {
            player.sendMessage(config.getMessage("no-permission"));
            return;
        }

        Menu menu = menuManager.getMenu(menuId);
        if (menu == null) {
            player.sendMessage(config.getMessage("menu-not-found"));
            return;
        }

        if (menu.getPermission() != null
                && !menu.getPermission().isBlank()
                && !player.hasPermission(menu.getPermission())) {
            player.sendMessage(config.getMessage("no-permission"));
            return;
        }

        player.showDialog(DialogBuilder.build(menu, player, actionExecutor::execute));
    }
}