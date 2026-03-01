package org.Elias040.servercore.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.Elias040.servercore.Main;
import org.Elias040.servercore.utils.TextUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public final class ServerCoreCommand implements CommandExecutor, TabCompleter {

    private static final String GITHUB_URL = "https://github.com/040Elias/ServerCore";
    private static final TextColor ACCENT = TextColor.fromHexString("#38c1fc");

    private static final SubCommand RELOAD  = new SubCommand("reload",  "servercore.reload");
    private static final SubCommand VERSION = new SubCommand("version", "servercore.version");
    private static final List<SubCommand> SUBS = List.of(RELOAD, VERSION);

    private final Main plugin;

    public ServerCoreCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        final String sub = args[0].toLowerCase(Locale.ROOT);

        if (sub.equals(RELOAD.name())) {
            if (!has(sender, RELOAD)) return true;
            plugin.reloadConfig();
            plugin.messages().loadMessages();
            sender.sendMessage(success("reloaded", "the configs."));
            return true;
        }

        if (sub.equals(VERSION.name())) {
            if (!has(sender, VERSION)) return true;
            sendVersion(sender);
            return true;
        }

        sendUnknown(sender);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 1) return List.of();

        final String input = args[0].toLowerCase(Locale.ROOT);
        return SUBS.stream()
                .filter(s -> s.name().startsWith(input))
                .filter(s -> sender.hasPermission(s.permission()))
                .map(SubCommand::name)
                .collect(Collectors.toList());
    }

    private boolean has(CommandSender sender, SubCommand sub) {
        if (sender.hasPermission(sub.permission())) return true;

        sender.sendMessage(TextUtil.toComponent("&cYou don't have permission."));
        return false;
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(TextUtil.toComponent("&cUsage: /servercore <reload|version>"));
    }

    private void sendUnknown(CommandSender sender) {
        sender.sendMessage(TextUtil.toComponent("&cUnknown subcommand. Usage: /servercore <reload|version>"));
    }

    private Component success(String highlightWord, String rest) {
        return Component.text("Successfully ", NamedTextColor.GRAY)
                .append(Component.text(highlightWord, ACCENT))
                .append(Component.space())
                .append(Component.text(rest, NamedTextColor.GRAY));
    }

    private void sendVersion(CommandSender sender) {
        final String pluginVersion = plugin.getPluginMeta().getVersion();
        final String mcVersion = plugin.getServer().getMinecraftVersion();
        final String software = plugin.getServer().getName();

        final Component divider = Component.text("  --------------------------------", NamedTextColor.DARK_GRAY);

        final Component title = Component.text("  ")
                .append(Component.text("ServerCore ", ACCENT).decorate(TextDecoration.BOLD))
                .append(Component.text("v" + pluginVersion, ACCENT).decoration(TextDecoration.BOLD, false));

        final Component softwareLine = Component.text("  ")
                .append(Component.text("Software", NamedTextColor.GRAY))
                .append(Component.text(" » ", NamedTextColor.DARK_GRAY))
                .append(Component.text(software, NamedTextColor.WHITE))
                .append(Component.text("  MC " + mcVersion, NamedTextColor.DARK_GRAY));

        final Component githubLine = Component.text("  ")
                .append(Component.text("GitHub", NamedTextColor.GRAY))
                .append(Component.text(" » ", NamedTextColor.DARK_GRAY))
                .append(Component.text(GITHUB_URL, ACCENT)
                        .clickEvent(ClickEvent.openUrl(GITHUB_URL))
                        .hoverEvent(HoverEvent.showText(Component.text("Click to open", NamedTextColor.GRAY))));

        sender.sendMessage(divider);
        sender.sendMessage(title);
        sender.sendMessage(softwareLine);
        sender.sendMessage(githubLine);
        sender.sendMessage(divider);
    }

    private record SubCommand(String name, String permission) {}
}