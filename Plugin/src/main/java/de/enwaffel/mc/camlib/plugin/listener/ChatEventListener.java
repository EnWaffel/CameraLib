package de.enwaffel.mc.camlib.plugin.listener;

import de.enwaffel.mc.camlib.api.tween.Easing;
import de.enwaffel.mc.camlib.plugin.AnimationManager;
import de.enwaffel.mc.camlib.plugin.CameraLibraryPlugin;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ChatEventListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!AnimationManager.CREATING_PLAYERS.containsKey(player)) return;

        HashMap<String, Object> data = AnimationManager.CREATING_PLAYERS.get(player);
        int state = (int) data.getOrDefault("state", 0);
        if (state == 0) {
            data.put("state", 0);
        }

        String message = event.getMessage();

        if (message.equalsIgnoreCase("c")) {
            AnimationManager.CREATING_PLAYERS.remove(player);
            event.setCancelled(true);
            CameraLibraryPlugin.clearChat(player);
            player.sendMessage("§cAnimation creation cancelled!");
            return;
        }

        switch (state) {
            case 0 -> {
                data.put("name", message);
                data.put("points", new ArrayList<Location>());

                player.sendMessage("§aName successfully set to \"" + message + "\"");
                player.sendMessage("\n");
                player.sendMessage("§7Go to the starting location and type anything (or c to cancel):");
            }
            case 1 -> {
                data.put("start", player.getLocation());

                player.sendMessage("§aStarting location successfully set!");
                player.sendMessage("\n");
                player.sendMessage("§7Go to a location and type anything to add a point or type 'done' to continue to the next step (or c to cancel):");
            }
            case 3 -> {
                data.put("end", player.getLocation());

                player.sendMessage("§aEnding location successfully set!");
                player.sendMessage("\n");
                player.sendMessage("§7Type the time in milliseconds (or c to cancel):");
            }
            case 2 -> {
                if (message.equalsIgnoreCase("done")) {
                    player.sendMessage("§aPoints successfully set!");
                    player.sendMessage("\n");
                    player.sendMessage("§7Go to the ending location and type anything (or c to cancel):");
                    break;
                }

                ((ArrayList<Location>)data.get("points")).add(player.getLocation());

                player.sendMessage("§aSuccessfully added a point!");
                player.sendMessage("\n");
                player.sendMessage("§7Go to a location and type anything to add a point or type 'done' to continue to the next step (or c to cancel):");

                event.setCancelled(true);
                return;
            }
            case 4 -> {
                int num;
                try {
                    num = Integer.parseInt(message);
                } catch (NumberFormatException e) {
                    player.sendMessage("§cThat is not a valid number!");
                    event.setCancelled(true);
                    return;
                }

                data.put("time", num);

                player.sendMessage("§aSuccessfully set time!");
                player.sendMessage("\n");
                player.sendMessage("§7Type the easing (or c to cancel):");
                for (Easing easing : Easing.values()) {
                    player.sendMessage("§7- §e" + easing.name());
                }
            }
            case 5 -> {
                if (Arrays.stream(Easing.values()).noneMatch(easing -> easing.name().equalsIgnoreCase(message))) {
                    player.sendMessage("§cThat is not a valid easing!");
                    event.setCancelled(true);
                    return;
                }

                player.sendMessage("§aSuccessfully set easing!");
                player.sendMessage("\n");
                player.sendMessage("\n");
                player.sendMessage("\n");

                File file = new File("plugins/CameraLibrary/animations/" + data.get("name") + ".yml");
                YamlConfiguration configuration = new YamlConfiguration();

                configuration.set("name", data.get("name"));
                configuration.set("data.start", data.get("start"));
                configuration.set("data.end", data.get("end"));

                configuration.set("data.points.count",((List<Location>) data.get("points")).size());
                int i = 0;
                for (Location location : (List<Location>) data.get("points")) {
                    configuration.set("data.points." + i, location);
                    i++;
                }

                configuration.set("data.time", data.get("time"));
                configuration.set("data.ease", message);

                try {
                    configuration.save(file);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                player.sendMessage("§a§lAnimation creation completed!");
                player.sendMessage("§7Your animation has been saved at: /plugins/CameraLibrary/animations/" + data.get("name") + ".yml");

                AnimationManager.reloadCache();

                AnimationManager.CREATING_PLAYERS.remove(player);
                event.setCancelled(true);
                return;
            }
        }

        state++;
        data.replace("state", state);
        event.setCancelled(true);
    }

}
