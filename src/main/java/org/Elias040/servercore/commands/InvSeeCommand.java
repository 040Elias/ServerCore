package org.Elias040.servercore.commands;

import org.Elias040.servercore.Main;
import org.Elias040.servercore.invsee.InvSeeSessions;
import org.Elias040.servercore.utils.SoundUtil;
import org.Elias040.servercore.utils.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;

public class InvSeeCommand implements CommandExecutor {

    private final Main plugin;

    public InvSeeCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player viewer)) {
            sender.sendMessage(plugin.messages().raw("only-players"));
            return true;
        }

        if (!viewer.hasPermission("servercore.invsee")) {
            viewer.sendMessage(plugin.messages().component("no-permission", Map.of()));
            SoundUtil.playError(plugin, viewer);
            return true;
        }

        if (args.length != 1) {
            viewer.sendMessage(TextUtil.toComponent("&cUsage: /invsee <player>"));
            SoundUtil.playError(plugin, viewer);
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null || !target.isOnline()) {
            viewer.sendMessage(plugin.messages().component("player-not-found", Map.of()));
            SoundUtil.playError(plugin, viewer);
            return true;
        }

        UUID targetId = target.getUniqueId();
        UUID viewerId = viewer.getUniqueId();

        target.getScheduler().run(plugin, task -> {
            ItemStack[] snapshot = snapshotTargetInventory(target);

            viewer.getScheduler().run(plugin, t2 -> {
                String rawTitle = plugin.getConfig().getString("invsee.title", "&7InvSee: %target%");
                rawTitle = rawTitle.replace("%target%", target.getName());

                var title = TextUtil.toComponent(rawTitle);
                Inventory gui = Bukkit.createInventory(null, 45, title);

                for (int i = 0; i <= 40; i++) {
                    gui.setItem(i, snapshot[i]);
                }

                InvSeeSessions.open(viewerId, targetId, gui);
                viewer.openInventory(gui);
            }, null);
        }, null);

        return true;
    }

    private ItemStack[] snapshotTargetInventory(Player target) {
        ItemStack[] out = new ItemStack[41];
        var inv = target.getInventory();

        for (int i = 0; i < 36; i++) {
            ItemStack it = inv.getItem(i);
            out[i] = (it == null ? null : it.clone());
        }

        for (int i = 36; i <= 39; i++) {
            ItemStack it = inv.getItem(i);
            out[i] = (it == null ? null : it.clone());
        }

        ItemStack off = inv.getItem(40);
        out[40] = (off == null ? null : off.clone());

        return out;
    }
}