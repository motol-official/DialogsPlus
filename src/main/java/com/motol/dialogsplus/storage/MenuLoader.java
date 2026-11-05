package com.motol.dialogsplus.storage;

import com.motol.dialogsplus.model.Menu;
import com.motol.dialogsplus.model.MenuAction;
import com.motol.dialogsplus.model.MenuComponent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MenuLoader {

    public static Menu load(File file) {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        Menu menu = new Menu();
        menu.setId(yaml.getString("id"));
        menu.setTitle(yaml.getString("title"));
        menu.setExternalTitle(yaml.getString("external-title"));
        menu.setCanCloseWithEscape(yaml.getBoolean("can-close-with-escape", true));
        menu.setBody(yaml.getStringList("body"));
        menu.setPermission(yaml.getString("permission"));

        List<MenuComponent> components = new ArrayList<>();
        List<?> rawComponents = yaml.getList("components");
        if (rawComponents != null) {
            for (Object obj : rawComponents) {
                if (obj instanceof ConfigurationSection section) {
                    components.add(loadComponent(section));
                } else if (obj instanceof java.util.Map<?, ?> map) {
                    YamlConfiguration temp = new YamlConfiguration();
                    for (var entry : map.entrySet()) {
                        temp.set(String.valueOf(entry.getKey()), entry.getValue());
                    }
                    components.add(loadComponent(temp));
                }
            }
        }
        menu.setComponents(components);

        return menu;
    }

    private static MenuComponent loadComponent(ConfigurationSection section) {
        MenuComponent component = new MenuComponent();
        component.setId(section.getString("id"));
        component.setLabel(section.getString("label"));
        component.setIcon(section.getString("icon"));
        component.setTooltip(section.getString("tooltip"));
        component.setPlaceholder(section.getString("placeholder"));
        component.setPermission(section.getString("permission"));

        if (section.contains("min")) {
            component.setMin(section.getDouble("min"));
        }
        if (section.contains("max")) {
            component.setMax(section.getDouble("max"));
        }
        if (section.contains("step")) {
            component.setStep(section.getDouble("step"));
        }
        if (section.contains("initial")) {
            component.setInitial(section.getDouble("initial"));
        }
        component.setOptions(section.getStringList("options"));

        String typeStr = section.getString("type");
        if (typeStr != null) {
            component.setType(MenuComponent.Type.valueOf(typeStr.toUpperCase()));
        }

        List<MenuAction> actions = new ArrayList<>();
        List<?> rawActions = section.getList("actions");
        if (rawActions != null) {
            for (Object obj : rawActions) {
                if (obj instanceof ConfigurationSection actionSection) {
                    actions.add(loadAction(actionSection));
                } else if (obj instanceof java.util.Map<?, ?> map) {
                    YamlConfiguration temp = new YamlConfiguration();
                    for (var entry : map.entrySet()) {
                        temp.set(String.valueOf(entry.getKey()), entry.getValue());
                    }
                    actions.add(loadAction(temp));
                }
            }
        }
        component.setActions(actions);

        return component;
    }

    private static MenuAction loadAction(ConfigurationSection section) {
        MenuAction action = new MenuAction();

        String typeStr = section.getString("type");
        if (typeStr != null) {
            action.setType(MenuAction.Type.valueOf(typeStr.toUpperCase()));
        }

        action.setCommand(section.getString("command"));
        action.setTarget(section.getString("target"));
        action.setMessage(section.getString("message"));
        action.setUrl(section.getString("url"));

        return action;
    }
}