package de.zekro.hcrevive.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Command executor for /purgatorytest command.
 */
public class PurgatoryTest implements CommandExecutor {

    public PurgatoryTest() {
        super();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(ChatColor.RED + "You don't have the permission to use this command.");
            return false;
        }
        sender.sendMessage(String.format(
                        "Worlds: %s", player.getServer().getWorlds()));

        World target = player.getServer().getWorld("hardcore_purgatory_purgatory");
        if (target != null) {
            player.teleport(new Location(target, 0, 64, 0));
        }

        return true;
    }
}
