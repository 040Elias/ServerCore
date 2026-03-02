package org.Elias040.servercore.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.Elias040.servercore.Main;
import org.Elias040.servercore.utils.SoundUtil;
import org.Elias040.servercore.utils.TextUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class ServerCoreCommand implements CommandExecutor, TabCompleter {

    private static final String GITHUB_URL = "https://github.com/040Elias/ServerCore";
    private static final TextColor ACCENT = TextColor.fromHexString("#38c1fc");

    private final Main plugin;

    public ServerCoreCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0 || (!args[0].equalsIgnoreCase("reload") && !args[0].equalsIgnoreCase("version"))) {
            sender.sendMessage(TextUtil.toComponent("&cUsage: /servercore <reload|version>"));
            if (sender instanceof Player p) SoundUtil.playError(plugin, p);
            return true;
        }

        if (!sender.hasPermission("servercore.use")) {
            if (sender instanceof Player p) {
                p.sendMessage(plugin.messages().component("no-permission", Map.of()));
                SoundUtil.playError(plugin, p);
            } else {
                sender.sendMessage(plugin.messages().component("no-permission", Map.of()));
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            plugin.messages().loadMessages();
            sender.sendMessage(TextUtil.toComponent("&7Successfully &#38c1fcreloaded&7 the configs."));
            return true;
        }

        sendVersion(sender);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && sender.hasPermission("servercore.use")) {
            return List.of("reload", "version").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return List.of();
    }

    private void sendVersion(CommandSender sender) {
        String version   = plugin.getPluginMeta().getVersion();
        String mcVersion = plugin.getServer().getMinecraftVersion();
        String software  = plugin.getServer().getName();

        Component accent  = Component.text("■ ", ACCENT);
        Component divider = Component.text("  ─────────────────────────", NamedTextColor.DARK_GRAY);

        Component title = Component.text("  ")
                .append(Component.text("ServerCore ", ACCENT).decorate(TextDecoration.BOLD))
                .append(Component.text("v" + version, NamedTextColor.DARK_GRAY).decoration(TextDecoration.BOLD, false));

        Component softwareLine = Component.text("  ").append(accent)
                .append(Component.text("Software  ", NamedTextColor.GRAY))
                .append(Component.text(software + " ", NamedTextColor.WHITE))
                .append(Component.text("(" + mcVersion + ")", NamedTextColor.DARK_GRAY));

        Component githubLine = Component.text("  ").append(accent)
                .append(Component.text("GitHub    ", NamedTextColor.GRAY))
                .append(Component.text("github.com/040Elias/ServerCore", ACCENT)
                        .clickEvent(ClickEvent.openUrl(GITHUB_URL))
                        .hoverEvent(HoverEvent.showText(Component.text("Click to open", NamedTextColor.GRAY)
                                .append(Component.newline())
                                .append(Component.text(GITHUB_URL, NamedTextColor.DARK_GRAY)))));

        sender.sendMessage(Component.empty());
        sender.sendMessage(divider);
        sender.sendMessage(title);
        sender.sendMessage(divider);
        sender.sendMessage(softwareLine);
        sender.sendMessage(githubLine);
        sender.sendMessage(divider);
        sender.sendMessage(Component.empty());
    }
}