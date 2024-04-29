package de.enwaffel.mc.camlib.plugin.command;

import de.enwaffel.mc.camlib.plugin.CamLibGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class CamLibCommand implements CommandExecutor {

    private final String prefix = "§8[§aCamLib§8] §7";

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            if (args.length < 1) {
                sender.sendMessage(prefix + "§cUsage: /camlib <play>");
                return false;
            }

            switch (args[0].toLowerCase()) {
                case "play" -> {
                    if (args.length < 2) {
                        sender.sendMessage(prefix + "§cUsage: /camlib play [Player] [Animation]");
                        return false;
                    }
                }
            }
        } else if (sender instanceof Player player) {
            if (!player.hasPermission("camlib.command.gui")) {
                player.sendMessage(prefix + "§cYou don't have permissions to use this command!");
                return false;
            }
            CamLibGUI.openGUI(player);
        } else {
            sender.sendMessage("This command can only be executed by the console or a player!");
            return false;
        }
        return true;
    }

}
