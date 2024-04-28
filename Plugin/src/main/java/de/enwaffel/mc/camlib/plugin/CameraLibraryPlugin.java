package de.enwaffel.mc.camlib.plugin;

import de.enwaffel.mc.camlib.api.CameraLibrary;
import org.bukkit.plugin.java.JavaPlugin;

public class CameraLibraryPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        CameraLibrary.getInstance();
    }

    @Override
    public void onDisable() {
        CameraLibrary.disable();
    }

}
