package de.enwaffel.mc.camlib.plugin;

import de.enwaffel.mc.camlib.api.tween.Easing;
import org.bukkit.Location;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public final class AnimationManager {

    public static final HashMap<Player, HashMap<String, Object>> CREATING_PLAYERS = new HashMap<>();
    public static final HashMap<String, AnimationData> CACHED_ANIMATIONS = new LinkedHashMap<>();

    public static void setupAnimationCreation(Player player) {
        CREATING_PLAYERS.put(player, new HashMap<>());

        CameraLibraryPlugin.clearChat(player);
        player.sendMessage("§aAnimation creation started!\n");
        player.sendMessage("§7Type the name of the new animation (or c to cancel):");
    }

    public static void reloadCache() {
        CACHED_ANIMATIONS.clear();

        File animDir = new File("plugins/CameraLibrary/animations");
        if (!animDir.exists()) animDir.mkdirs();

        File[] files = animDir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) continue;
            if (!file.getName().endsWith(".yml")) continue;
            Configuration configuration = YamlConfiguration.loadConfiguration(file);

            String name = configuration.getString("name");
            Location start = configuration.getLocation("data.start");
            Location end = configuration.getLocation("data.end");
            int pointsCount = configuration.getInt("data.points.count");
            List<Location> points = new ArrayList<>();
            for (int i = 0; i < pointsCount; i++) {
                points.add(configuration.getLocation("data.points." + i));
            }
            Easing easing = Easing.valueOf(configuration.getString("data.ease"));
            int time = configuration.getInt("data.time");

            CACHED_ANIMATIONS.put(name, new AnimationData(start, end, points, time, easing));
        }
    }

    public static void deleteAnimation(String name) {
        AnimationManager.CACHED_ANIMATIONS.remove(name);
        File file = new File("plugins/CameraLibrary/animations/" + name + ".yml");
        file.delete();
    }

}
