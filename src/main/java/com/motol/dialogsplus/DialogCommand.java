package com.motol.dialogsplus;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DialogCommand implements CommandExecutor {

    private final MenuOpener menuOpener;

    public DialogCommand(MenuOpener menuOpener) {
        this.menuOpener = menuOpener;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }

        String menuId = args.length > 0 ? args[0] : "main";
        menuOpener.open(player, menuId);
        return true;
    }
}