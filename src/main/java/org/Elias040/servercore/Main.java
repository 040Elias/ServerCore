package org.Elias040.servercore;

import org.Elias040.servercore.commands.SetSpawnCommand;
import org.Elias040.servercore.commands.SpawnCommand;
import org.Elias040.servercore.spawn.SpawnManager;
import org.Elias040.servercore.utils.MessageManager;
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
    }

    public MessageManager messages() {
        return messageManager;
    }

    public SpawnManager spawns() {
        return spawnManager;
    }
}