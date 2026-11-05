package com.motol.dialogsplus.storage;

import com.motol.dialogsplus.model.Menu;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class MenuManager {

    private final File menusFolder;
    private final File backupsFolder;
    private final Logger logger;
    private final Map<String, Menu> menus = new HashMap<>();
    private final Map<String, File> files = new HashMap<>();

    public MenuManager(File dataFolder, Logger logger) {
        this.menusFolder = new File(dataFolder, "menus");
        this.backupsFolder = new File(dataFolder, "backups");
        this.logger = logger;
    }

    public void loadAll() {
        menus.clear();
        files.clear();

        if (!menusFolder.exists()) {
            menusFolder.mkdirs();
            return;
        }

        File[] found = menusFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (found == null) {
            return;
        }

        for (File file : found) {
            try {
                Menu menu = MenuLoader.load(file);
                if (menu.getId() == null || menu.getId().isBlank()) {
                    logger.warning("Skipping " + file.getName() + ": missing 'id' field.");
                    continue;
                }
                if (menus.containsKey(menu.getId())) {
                    logger.warning("Duplicate menu id '" + menu.getId() + "' in " + file.getName()
                            + " (already loaded from " + files.get(menu.getId()).getName() + "), overwriting.");
                }
                menus.put(menu.getId(), menu);
                files.put(menu.getId(), file);
                logger.info("Loaded menu: " + menu.getId());
            } catch (Exception e) {
                logger.warning("Failed to load menu from " + file.getName() + ": " + e.getMessage());
            }
        }
    }

    public Menu getMenu(String id) {
        return menus.get(id);
    }

    public Map<String, Menu> getAllMenus() {
        return menus;
    }

    public File getFile(String id) {
        return files.get(id);
    }


    public void save(Menu menu) throws IOException {
        if (!menusFolder.exists()) {
            menusFolder.mkdirs();
        }
        File file = files.get(menu.getId());
        if (file == null) {
            file = new File(menusFolder, menu.getId() + ".yml");
        }
        MenuWriter.save(menu, file, backupsFolder);
        menus.put(menu.getId(), menu);
        files.put(menu.getId(), file);
    }

    public void delete(String id) throws IOException {
        File file = files.get(id);
        if (file != null && file.exists()) {
            backupsFolder.mkdirs();
            Files.copy(file.toPath(), new File(backupsFolder, id + ".deleted.yml").toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            Files.delete(file.toPath());
        }
        menus.remove(id);
        files.remove(id);
    }
}