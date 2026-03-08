package org.Elias040.servercore.commands;

import org.Elias040.servercore.Main;
import org.Elias040.servercore.utils.SchedulerCompat;
import org.Elias040.servercore.utils.SoundUtil;
import org.Elias040.servercore.utils.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class HealCommand implements CommandExecutor, TabCompleter {

    private final Main plugin;

    public HealCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player p)) {
                sender.sendMessage(plugin.messages().raw("only-players"));
                return true;
            }
            heal(p, p);
            return true;
        }

        if (args.length == 1) {
            if (!sender.hasPermission("servercore.heal.other")) {
                if (sender instanceof Player p) SoundUtil.playError(plugin, p);
                sender.sendMessage(plugin.messages().component("no-permission", Map.of()));
                return true;
            }

            Player target = Bukkit.getPlayerExact(args[0]);
            if (target == null || !target.isOnline()) {
                if (sender instanceof Player p) SoundUtil.playError(plugin, p);
                sender.sendMessage(plugin.messages().component("player-not-found", Map.of()));
                return true;
            }

            heal(sender instanceof Player p ? p : null, target);
            return true;
        }

        if (sender instanceof Player p) SoundUtil.playError(plugin, p);
        sender.sendMessage(TextUtil.toComponent("&cUsage: /heal [player]"));
        return true;
    }

    private void heal(Player executor, Player target) {
        SchedulerCompat.runForEntity(plugin, target, () -> {
            AttributeInstance maxHealth = target.getAttribute(Attribute.MAX_HEALTH);
            double max = maxHealth != null ? maxHealth.getValue() : 20.0;
            target.setHealth(max);
            target.setFoodLevel(20);
            target.setSaturation(20f);

            if (executor != null && !executor.getUniqueId().equals(target.getUniqueId())) {
                SchedulerCompat.runForEntity(plugin, executor, () ->
                        executor.sendMessage(plugin.messages().component("heal-other",
                                Map.of("player", target.getName()))));
                target.sendMessage(plugin.messages().component("heal-by-other",
                        Map.of("player", executor.getName())));
            } else {
                target.sendMessage(plugin.messages().component("heal-self", Map.of()));
            }
        });
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1 && sender.hasPermission("servercore.heal.other")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}