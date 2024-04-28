package de.enwaffel.mc.camlib.nms;

import org.bukkit.Bukkit;

public final class NMSVersion {

    public static final String VERSION;

    static {
        VERSION = Bukkit.getServer().getClass().getPackage().getName().split( "\\." )[3];
    }

}
