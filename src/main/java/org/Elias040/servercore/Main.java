package org.Elias040.servercore;

import org.Elias040.servercore.commands.BroadcastCommand;
import org.Elias040.servercore.commands.DelSpawnCommand;
import org.Elias040.servercore.commands.FreezeCommand;
import org.Elias040.servercore.commands.InvSeeCommand;
import org.Elias040.servercore.commands.MsgCommand;
import org.Elias040.servercore.commands.NightVisionCommand;
import org.Elias040.servercore.commands.PingCommand;
import org.Elias040.servercore.commands.ReloadCommand;
import org.Elias040.servercore.commands.ReplyCommand;
import org.Elias040.servercore.commands.SetSpawnCommand;
import org.Elias040.servercore.commands.SpawnCommand;
import org.Elias040.servercore.commands.UnfreezeCommand;
import org.Elias040.servercore.commands.WhoisCommand;
import org.Elias040.servercore.freeze.FreezeListener;
import org.Elias040.servercore.invsee.InvSeeListener;
import org.Elias040.servercore.listeners.JoinLeaveListener;
import org.Elias040.servercore.listeners.MsgQuitListener;
import org.Elias040.servercore.nightvision.NightVisionListener;
import org.Elias040.servercore.spawn.SpawnManager;
import org.Elias040.servercore.utils.MessageManager;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private MessageManager messageManager;
    private SpawnManager spawnManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.messageManager = new MessageManager(this);
        this.messageManager.loadMessages();

        this.spawnManager = new SpawnManager(this);

        FreezeListener freezeListener = new FreezeListener(this);
        MsgCommand msgCommand = new MsgCommand(this);
        ReplyCommand replyCommand = new ReplyCommand(this);
        SpawnCommand spawnCommand = new SpawnCommand(this);

        registerCommand("spawn",       spawnCommand);
        registerCommand("setspawn",    new SetSpawnCommand(this));
        registerCommand("delspawn",    new DelSpawnCommand(this));
        registerCommand("invsee",      new InvSeeCommand(this));
        registerCommand("broadcast",   new BroadcastCommand(this));
        registerCommand("nightvision", new NightVisionCommand(this));
        registerCommand("ping",        new PingCommand(this));
        registerCommand("freeze",      new FreezeCommand(this, freezeListener));
        registerCommand("unfreeze",    new UnfreezeCommand(this));
        registerCommand("whois",       new WhoisCommand(this));
        registerCommand("msg",         msgCommand);
        registerCommand("reply",       replyCommand);
        registerCommand("servercore",  new ReloadCommand(this));

        getServer().getPluginManager().registerEvents(new InvSeeListener(), this);
        getServer().getPluginManager().registerEvents(new NightVisionListener(), this);
        getServer().getPluginManager().registerEvents(freezeListener, this);
        getServer().getPluginManager().registerEvents(new JoinLeaveListener(this), this);
        getServer().getPluginManager().registerEvents(new MsgQuitListener(), this);
        getServer().getPluginManager().registerEvents(new org.Elias040.servercore.listeners.SpawnQuitListener(spawnCommand), this);
    }

    private void registerCommand(String name, Object executor) {
        var cmd = getCommand(name);
        if (cmd == null) return;
        if (executor instanceof CommandExecutor ce) cmd.setExecutor(ce);
        if (executor instanceof TabCompleter tc) cmd.setTabCompleter(tc);
    }

    public MessageManager messages() { return messageManager; }
    public SpawnManager spawns() { return spawnManager; }
}