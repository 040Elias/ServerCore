package org.Elias040.servercore.features.moderation;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.Elias040.servercore.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ChatModerationListener implements Listener {

    private final Main plugin;
    private final ChatModerationService service;
    private final Set<UUID> commandPassthrough = ConcurrentHashMap.newKeySet();

    public ChatModerationListener(Main plugin, ChatModerationService service) {
        this.plugin = plugin;
        this.service = service;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        String message = PlainTextComponentSerializer.plainText().serialize(event.message());

        ViolationResult result = service.checkChat(player, message);
        if (result == null) return;

        event.setCancelled(true);
        service.sendFeedback(player, result);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (commandPassthrough.remove(uuid)) return;

        String rawMessage = event.getMessage();
        String command = normalizeCommand(rawMessage);

        event.setCancelled(true);

        player.getScheduler().run(plugin, t -> {
            ViolationResult result = service.checkCommand(player, command);
            if (result != null) {
                service.sendFeedback(player, result);
                return;
            }
            commandPassthrough.add(uuid);
            player.chat(rawMessage);
        }, null);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        commandPassthrough.remove(uuid);
        service.evictPlayer(uuid);
    }

    private static String normalizeCommand(String rawMessage) {
        String stripped = rawMessage.startsWith("/") ? rawMessage.substring(1) : rawMessage;
        int spaceIndex = stripped.indexOf(' ');
        String commandName = spaceIndex == -1 ? stripped : stripped.substring(0, spaceIndex);
        int colonIndex = commandName.indexOf(':');
        if (colonIndex != -1) commandName = commandName.substring(colonIndex + 1);
        return commandName.toLowerCase(Locale.ROOT);
    }
}