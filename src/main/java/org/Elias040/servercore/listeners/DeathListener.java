package org.Elias040.servercore.listeners;

import net.kyori.adventure.text.Component;
import org.Elias040.servercore.Main;
import org.Elias040.servercore.utils.TextUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathListener implements Listener {

    private final Main plugin;

    public DeathListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent e) {
        Component vanilla = e.deathMessage();
        if (vanilla == null) return;

        String prefixRaw      = plugin.getConfig().getString("death.prefix", "");
        String msgColorRaw    = plugin.getConfig().getString("death.message-color", "");
        String suffixRaw      = plugin.getConfig().getString("death.suffix", "");

        Component vanillaColored = vanilla;
        if (!msgColorRaw.isEmpty()) {
            vanillaColored = TextUtil.toComponent(msgColorRaw).append(vanilla);
        }

        Component wrapped = Component.empty();
        if (!prefixRaw.isEmpty())  wrapped = wrapped.append(TextUtil.toComponent(prefixRaw));
        wrapped = wrapped.append(vanillaColored);
        if (!suffixRaw.isEmpty())  wrapped = wrapped.append(TextUtil.toComponent(suffixRaw));

        e.deathMessage(wrapped);
    }
}