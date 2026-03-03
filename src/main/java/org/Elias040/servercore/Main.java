package org.Elias040.servercore;

import org.Elias040.servercore.commands.BroadcastCommand;
import org.Elias040.servercore.features.spawn.*;
import org.Elias040.servercore.features.warp.*;
import org.Elias040.servercore.features.invsee.InvSeeSessions;
import org.Elias040.servercore.features.moderation.ChatModerationListener;
import org.Elias040.servercore.features.moderation.ChatModerationService;
import org.Elias040.servercore.commands.DiscordCommand;
import org.Elias040.servercore.features.freeze.FreezeCommand;
import org.Elias040.servercore.commands.GamemodeCommand;
import org.Elias040.servercore.features.invsee.InvSeeCommand;
import org.Elias040.servercore.commands.LiveCommand;
import org.Elias040.servercore.features.media.MediaCommand;
import org.Elias040.servercore.features.msg.MsgCommand;
import org.Elias040.servercore.features.nightvision.NightVisionCommand;
import org.Elias040.servercore.commands.PingCommand;
import org.Elias040.servercore.commands.ServerCoreCommand;
import org.Elias040.servercore.features.msg.ReplyCommand;
import org.Elias040.servercore.features.freeze.UnfreezeCommand;
import org.Elias040.servercore.commands.WhoisCommand;
import org.Elias040.servercore.features.freeze.FreezeListener;
import org.Elias040.servercore.features.invsee.InvSeeListener;
import org.Elias040.servercore.listeners.DeathListener;
import org.Elias040.servercore.listeners.JoinLeaveListener;
import org.Elias040.servercore.features.msg.MsgQuitListener;
import org.Elias040.servercore.features.media.MediaListener;
import org.Elias040.servercore.features.nightvision.NightVisionListener;
import org.Elias040.servercore.utils.MessageManager;
import org.bukkit.GameMode;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private MessageManager messageManager;
    private SpawnManager spawnManager;
    private WarpManager warpManager;
    private ChatModerationService moderationService;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.messageManager = new MessageManager(this);
        this.messageManager.loadMessages();

        this.spawnManager = new SpawnManager(this);
        this.warpManager = new WarpManager(this);
        this.moderationService = new ChatModerationService(this);

        FreezeListener freezeListener = new FreezeListener(this);
        MsgCommand msgCommand = new MsgCommand(this);
        ReplyCommand replyCommand = new ReplyCommand(this);
        SpawnCommand spawnCommand = new SpawnCommand(this);

        registerCommand("spawn",       spawnCommand);
        registerCommand("setspawn",    new SetSpawnCommand(this));
        registerCommand("delspawn",    new DelSpawnCommand(this));

        WarpCommand warpCommand = new WarpCommand(this);
        registerCommand("warp",        warpCommand);
        registerCommand("setwarp",     new SetWarpCommand(this));
        registerCommand("delwarp",     new DelWarpCommand(this));
        registerCommand("invsee",      new InvSeeCommand(this));
        registerCommand("broadcast",   new BroadcastCommand(this));
        registerCommand("nightvision", new NightVisionCommand(this));
        registerCommand("ping",        new PingCommand(this));
        registerCommand("freeze",      new FreezeCommand(this, freezeListener));
        registerCommand("unfreeze",    new UnfreezeCommand(this));
        registerCommand("whois",       new WhoisCommand(this));
        registerCommand("msg",         msgCommand);
        registerCommand("reply",       replyCommand);
        registerCommand("servercore",  new ServerCoreCommand(this));
        LiveCommand liveCommand = new LiveCommand(this);
        registerCommand("discord",     new DiscordCommand(this));
        registerCommand("live",        liveCommand);

        GamemodeCommand gmCmd = new GamemodeCommand(this, null);
        registerCommand("gm",   gmCmd);
        registerCommand("gmc",  new GamemodeCommand(this, GameMode.CREATIVE));
        registerCommand("gms",  new GamemodeCommand(this, GameMode.SURVIVAL));
        registerCommand("gma",  new GamemodeCommand(this, GameMode.ADVENTURE));
        registerCommand("gmsp", new GamemodeCommand(this, GameMode.SPECTATOR));

        registerCommand("media", new MediaCommand(this));

        getServer().getPluginManager().registerEvents(new InvSeeListener(), this);
        getServer().getPluginManager().registerEvents(new NightVisionListener(this), this);
        getServer().getPluginManager().registerEvents(freezeListener, this);
        getServer().getPluginManager().registerEvents(new JoinLeaveListener(this), this);
        getServer().getPluginManager().registerEvents(new MsgQuitListener(), this);
        getServer().getPluginManager().registerEvents(new SpawnQuitListener(spawnCommand), this);
        getServer().getPluginManager().registerEvents(new WarpQuitListener(warpCommand), this);
        getServer().getPluginManager().registerEvents(new org.Elias040.servercore.listeners.LiveQuitListener(liveCommand), this);
        getServer().getPluginManager().registerEvents(new DeathListener(this), this);
        getServer().getPluginManager().registerEvents(new MediaListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatModerationListener(this, moderationService), this);
    }

    private void registerCommand(String name, Object executor) {
        var cmd = getCommand(name);
        if (cmd == null) return;
        if (executor instanceof CommandExecutor ce) cmd.setExecutor(ce);
        if (executor instanceof TabCompleter tc) cmd.setTabCompleter(tc);
    }

    @Override
    public void onDisable() {
        if (spawnManager != null) spawnManager.shutdown();
        if (warpManager  != null) warpManager.shutdown();
        InvSeeSessions.clear();
    }

    public MessageManager messages() { return messageManager; }
    public SpawnManager spawns()     { return spawnManager; }
    public WarpManager  warps()      { return warpManager; }
    public ChatModerationService moderation() { return moderationService; }
}