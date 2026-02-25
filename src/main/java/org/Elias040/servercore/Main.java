package org.Elias040.servercore;

import org.Elias040.servercore.commands.BroadcastCommand;
import org.Elias040.servercore.commands.DelSpawnCommand;
import org.Elias040.servercore.commands.InvSeeCommand;
import org.Elias040.servercore.commands.SetSpawnCommand;
import org.Elias040.servercore.commands.SpawnCommand;
import org.Elias040.servercore.spawn.SpawnManager;
import org.Elias040.servercore.utils.MessageManager;
import org.Elias040.servercore.invsee.InvSeeListener;
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

        var spawnCmd = getCommand("spawn");
        if (spawnCmd != null) spawnCmd.setExecutor(new SpawnCommand(this));

        var setSpawnCmd = getCommand("setspawn");
        if (setSpawnCmd != null) setSpawnCmd.setExecutor(new SetSpawnCommand(this));

        var delSpawnCmd = getCommand("delspawn");
        if (delSpawnCmd != null) delSpawnCmd.setExecutor(new DelSpawnCommand(this));

        var invseeCmd = getCommand("invsee");
        if (invseeCmd != null) invseeCmd.setExecutor(new InvSeeCommand(this));

        var broadcastCmd = getCommand("broadcast");
        if (broadcastCmd != null) broadcastCmd.setExecutor(new BroadcastCommand(this));

        getServer().getPluginManager().registerEvents(new InvSeeListener(), this);
    }

    public MessageManager messages() { return messageManager; }
    public SpawnManager spawns() { return spawnManager; }
}