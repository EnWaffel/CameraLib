package de.enwaffel.mc.camlib.plugin.command;

import de.enwaffel.mc.camlib.api.CameraLibrary;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CamLockCommand implements CommandExecutor {

    private final String prefix = "§8[§aCamLib§8] §7";

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player player) {
            if (!player.hasPermission("camlib.command.lock")) {
                player.sendMessage(prefix + "§cYou don't have permissions to use this command!");
                return false;
            }

            Player plr;

            if (args.length < 1) {
                plr = player;
            } else {
                plr = Bukkit.getPlayer(args[0]);
                if (plr == null) {
                    player.sendMessage(prefix + "§cThat player is not online!");
                    return false;
                }
            }

            if (CameraLibrary.getInstance().isPlayerLocked(plr)) {
                CameraLibrary.getInstance().unlockPlayer(plr);
                player.sendMessage(prefix + "§a" + plr.getName() + " is no longer locked!");
            } else {
                CameraLibrary.getInstance().lockPlayer(plr);
                player.sendMessage(prefix + "§c" + plr.getName() + " is now locked!");
            }
        } else {
            sender.sendMessage("This command can only be executed by a player!");
            return false;
        }
        return true;
    }

}
