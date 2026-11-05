package com.motol.dialogsplus.config;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.LinkedHashMap;
import java.util.Map;

public class ConfigManager {

    private final FileConfiguration config;

    public ConfigManager(FileConfiguration config) {
        this.config = config;
    }

    public boolean isDebug() {
        return config.getBoolean("settings.debug", false);
    }

    public String getDefaultMenu() {
        return config.getString("settings.default-menu", "main");
    }

    public boolean isIconsEnabled() {
        return config.getBoolean("icons.enabled", true);
    }

    public boolean isIconTooltipShown() {
        return config.getBoolean("icons.show-tooltip", true);
    }

    public String getReloadPermission() {
        return config.getString("permissions.reload", "dialogsplus.reload");
    }

    public String getUseDialogsPermission() {
        return config.getString("permissions.use-dialogs-command", "dialogsplus.use");
    }

    public String getMessage(String key) {
        String prefix = config.getString("messages.prefix", "");
        String message = config.getString("messages." + key, "");
        return ChatColor.translateAlternateColorCodes('&', prefix + message);
    }

    public Map<String, String> getCommandMappings() {
        Map<String, String> mappings = new LinkedHashMap<>();
        var section = config.getConfigurationSection("commands");
        if (section != null) {
            for (String menuId : section.getKeys(false)) {
                mappings.put(menuId, section.getString(menuId));
            }
        }
        return mappings;
    }
}