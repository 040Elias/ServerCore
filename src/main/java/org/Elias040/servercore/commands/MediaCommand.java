package org.Elias040.servercore.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.Elias040.servercore.Main;
import org.Elias040.servercore.media.MediaGui;
import org.Elias040.servercore.utils.SoundUtil;
import org.Elias040.servercore.utils.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;

public class MediaCommand implements CommandExecutor {

    private static final int ACTION_SLOT = 13;
    private final Main plugin;

    public MediaCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(plugin.messages().raw("only-players"));
            return true;
        }

        if (!p.hasPermission("servercore.media")) {
            p.sendMessage(plugin.messages().component("no-permission", Map.of()));
            SoundUtil.playError(plugin, p);
            return true;
        }

        Inventory gui = buildGui(p);
        p.getScheduler().run(plugin, t -> p.openInventory(gui), null);
        return true;
    }

    public Inventory buildGui(Player player) {
        var cfg = plugin.getConfig();

        String rawTitle = cfg.getString("media.title", "&7Media");
        Component title = TextUtil.toComponent(rawTitle);

        MediaGui holder = new MediaGui();
        Inventory inv = Bukkit.createInventory(holder, 27, title);
        holder.setInventory(inv);

        if (cfg.getBoolean("media.filler.enabled", true)) {
            String rawMat = cfg.getString("media.filler.material", "GRAY_STAINED_GLASS_PANE");
            Material mat = parseMaterial(rawMat, Material.GRAY_STAINED_GLASS_PANE);
            String rawName = cfg.getString("media.filler.name", " ");
            List<String> rawLore = cfg.getStringList("media.filler.lore");

            ItemStack filler = buildItem(mat, rawName, rawLore);

            for (int i = 0; i < 27; i++) {
                if (i != ACTION_SLOT) {
                    inv.setItem(i, filler);
                }
            }
        }

        String rawMat = cfg.getString("media.item.material", "PAPER");
        Material mat = parseMaterial(rawMat, Material.PAPER);
        String rawName = cfg.getString("media.item.name", "&fClick me!");
        List<String> rawLore = cfg.getStringList("media.item.lore");

        inv.setItem(ACTION_SLOT, buildItem(mat, rawName, rawLore));
        return inv;
    }

    private ItemStack buildItem(Material mat, String rawName, List<String> rawLore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.displayName(
                TextUtil.toComponent(rawName)
                        .decoration(TextDecoration.ITALIC, false)
        );

        if (!rawLore.isEmpty()) {
            List<Component> lore = rawLore.stream()
                    .map(TextUtil::toComponent)
                    .map(c -> c.decoration(TextDecoration.ITALIC, false))
                    .toList();
            meta.lore(lore);
        }

        if (meta instanceof org.bukkit.inventory.meta.LeatherArmorMeta leatherMeta) {
            String rawColor = plugin.getConfig().getString("media.item.leather-color", "");
            if (rawColor != null && !rawColor.isBlank()) {
                org.bukkit.Color color = parseHexColor(rawColor);
                if (color != null) {
                    leatherMeta.setColor(color);
                }
            }
        }

        meta.addItemFlags(
                org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES,
                org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS
        );

        item.setItemMeta(meta);
        return item;
    }

    private static org.bukkit.Color parseHexColor(String input) {
        String s = input.trim();
        if (s.startsWith("&#")) s = s.substring(2);
        else if (s.startsWith("#")) s = s.substring(1);
        else if (s.startsWith("&")) s = s.substring(1);

        if (s.length() != 6) return null;
        try {
            int r = Integer.parseInt(s.substring(0, 2), 16);
            int g = Integer.parseInt(s.substring(2, 4), 16);
            int b = Integer.parseInt(s.substring(4, 6), 16);
            return org.bukkit.Color.fromRGB(r, g, b);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Material parseMaterial(String name, Material fallback) {
        if (name == null || name.isBlank()) return fallback;
        try {
            Material m = Material.valueOf(name.trim().toUpperCase(java.util.Locale.ROOT));
            return m == Material.AIR ? fallback : m;
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }
}