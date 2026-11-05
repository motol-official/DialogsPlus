package com.motol.dialogsplus.dialog;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlaceholderSupport {

    private static final boolean ENABLED = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");

    public static String apply(Player player, String text) {
        if (!ENABLED || text == null) {
            return text;
        }
        try {
            return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, text);
        } catch (Throwable t) {
            return text;
        }
    }
}