package com.motol.dialogsplus.dialog;

import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.object.ObjectContents;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class IconParser {

    private static final String NAMESPACE = "minecraft";
    private static final Key BLOCKS_ATLAS = Key.key(NAMESPACE, "blocks");
    private static final Key ITEMS_ATLAS = Key.key(NAMESPACE, "items");
    private static final Map<Material, String> SPRITE_OVERRIDES = Map.ofEntries(
            Map.entry(Material.GRASS_BLOCK, "block/grass_block_side"),
            Map.entry(Material.PODZOL, "block/podzol_side"),
            Map.entry(Material.MYCELIUM, "block/mycelium_side"),
            Map.entry(Material.DIRT_PATH, "block/dirt_path_side"),
            Map.entry(Material.CRAFTING_TABLE, "block/crafting_table_front"),
            Map.entry(Material.FURNACE, "block/furnace_front"),
            Map.entry(Material.TNT, "block/tnt_side"),
            Map.entry(Material.PUMPKIN, "block/pumpkin_side"),
            Map.entry(Material.MELON, "block/melon_side"),
            Map.entry(Material.HAY_BLOCK, "block/hay_block_side"),
            Map.entry(Material.BARRIER, "item/barrier")
    );

    public static Component toComponent(String icon, Player viewer) {
        if (icon == null || icon.isBlank()) {
            return null;
        }
        icon = icon.trim();
        String lower = icon.toLowerCase();

        if (lower.equals("player_head:{self}")) {
            return Component.object(ObjectContents.playerHead(viewer.getUniqueId()));
        }
        if (lower.startsWith("player_head:")) {
            String name = icon.substring("player_head:".length());
            UUID id = resolveUuid(name);
            return id == null ? null : Component.object(ObjectContents.playerHead(id));
        }

        if (lower.startsWith("sprite:")) {
            return spriteComponent(lower.substring("sprite:".length()));
        }

        Material material = Material.matchMaterial(icon);
        if (material == null) {
            return null;
        }
        String defaultPath = (material.isBlock() ? "block/" : "item/") + material.getKey().getKey();
        String path = SPRITE_OVERRIDES.getOrDefault(material, defaultPath);
        return spriteComponent(path);
    }

    private static Component spriteComponent(String raw) {
        try {
            int colon = raw.indexOf(':');
            String namespace = colon >= 0 ? raw.substring(0, colon) : NAMESPACE;
            String path = colon >= 0 ? raw.substring(colon + 1) : raw;
            Key sprite = Key.key(namespace, path);
            Key atlas = path.startsWith("item/") ? ITEMS_ATLAS : BLOCKS_ATLAS;
            return Component.object(ObjectContents.sprite(atlas, sprite));
        } catch (InvalidKeyException e) {
            return null;
        }
    }

    private static UUID resolveUuid(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        Player online = Bukkit.getPlayerExact(name);
        if (online != null) {
            return online.getUniqueId();
        }
        OfflinePlayer cached = Bukkit.getOfflinePlayerIfCached(name);
        if (cached != null) {
            return cached.getUniqueId();
        }
        return Bukkit.getOfflinePlayer(name).getUniqueId();
    }
}