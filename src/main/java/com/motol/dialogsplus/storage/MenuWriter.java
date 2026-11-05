package com.motol.dialogsplus.storage;

import com.motol.dialogsplus.model.Menu;
import com.motol.dialogsplus.model.MenuAction;
import com.motol.dialogsplus.model.MenuComponent;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MenuWriter {

    public static void save(Menu menu, File target, File backupsFolder) throws IOException {
        if (target.exists()) {
            File original = new File(backupsFolder, menu.getId() + ".original.yml");
            if (!original.exists()) {
                backupsFolder.mkdirs();
                Files.copy(target.toPath(), original.toPath());
            }
        }

        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("id", menu.getId());
        yaml.set("title", menu.getTitle());
        if (notBlank(menu.getExternalTitle())) {
            yaml.set("external-title", menu.getExternalTitle());
        }
        yaml.set("can-close-with-escape", menu.isCanCloseWithEscape());
        if (notBlank(menu.getPermission())) {
            yaml.set("permission", menu.getPermission());
        }
        yaml.set("body", new ArrayList<>(menu.getBody()));

        List<Map<String, Object>> components = new ArrayList<>();
        for (MenuComponent c : menu.getComponents()) {
            components.add(componentToMap(c));
        }
        yaml.set("components", components);

        Path targetPath = target.toPath();
        Path tmp = targetPath.resolveSibling(target.getName() + ".tmp");
        Files.writeString(tmp, yaml.saveToString(), StandardCharsets.UTF_8);
        try {
            Files.move(tmp, targetPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(tmp, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static Map<String, Object> componentToMap(MenuComponent c) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", c.getId());
        if (c.getType() != null) {
            map.put("type", c.getType().name());
        }
        if (notBlank(c.getLabel())) map.put("label", c.getLabel());
        if (notBlank(c.getIcon())) map.put("icon", c.getIcon());
        if (notBlank(c.getTooltip())) map.put("tooltip", c.getTooltip());
        if (notBlank(c.getPlaceholder())) map.put("placeholder", c.getPlaceholder());
        if (notBlank(c.getPermission())) map.put("permission", c.getPermission());
        if (c.getMin() != null) map.put("min", c.getMin());
        if (c.getMax() != null) map.put("max", c.getMax());
        if (c.getStep() != null) map.put("step", c.getStep());
        if (c.getInitial() != null) map.put("initial", c.getInitial());
        if (c.getOptions() != null && !c.getOptions().isEmpty()) {
            map.put("options", new ArrayList<>(c.getOptions()));
        }
        if (c.getActions() != null && !c.getActions().isEmpty()) {
            List<Map<String, Object>> actions = new ArrayList<>();
            for (MenuAction a : c.getActions()) {
                actions.add(actionToMap(a));
            }
            map.put("actions", actions);
        }
        return map;
    }

    private static Map<String, Object> actionToMap(MenuAction a) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (a.getType() != null) map.put("type", a.getType().name());
        if (notBlank(a.getCommand())) map.put("command", a.getCommand());
        if (notBlank(a.getTarget())) map.put("target", a.getTarget());
        if (notBlank(a.getMessage())) map.put("message", a.getMessage());
        if (notBlank(a.getUrl())) map.put("url", a.getUrl());
        return map;
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }
}