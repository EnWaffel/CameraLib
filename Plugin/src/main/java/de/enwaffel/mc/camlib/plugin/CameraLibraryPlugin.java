package de.enwaffel.mc.camlib.plugin;

import de.enwaffel.mc.camlib.api.CameraLibrary;
import de.enwaffel.mc.camlib.plugin.command.CamLibCommand;
import de.enwaffel.mc.camlib.plugin.command.CamLockCommand;
import de.enwaffel.mc.camlib.plugin.listener.ChatEventListener;
import de.enwaffel.mc.dlib.impl.DLib;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class CameraLibraryPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        CameraLibrary.getInstance();

        saveDefaultConfig();

        if (getConfig().getBoolean("gui", true)) {
            DLib.attach(this);
        }

        getServer().getPluginManager().registerEvents(new ChatEventListener(), this);

        getCommand("camlib").setExecutor(new CamLibCommand());
        getCommand("camlock").setExecutor(new CamLockCommand());

        AnimationManager.reloadCache();
    }

    @Override
    public void onDisable() {
        CameraLibrary.disable();
        if (getConfig().getBoolean("gui", true)) {
            DLib.detach();
        }
    }

    public static void clearChat(Player player) {
        for (int i = 0; i < 1000; i++) {
            player.sendMessage("\n");
        }
    }

}
