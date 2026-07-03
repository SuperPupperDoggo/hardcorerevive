package de.zekro.hcrevive.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Command executor for /purgatorytest command.
 */
public class PurgatoryTest implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(ChatColor.RED + "You don't have the permission to use this command.");
            return false;
        }
        this.logger.log(Level.INFO, String.format(
                        "Worlds: %s", player.getServer().getWorlds()));

        World target = player.getServer().getWorld(this.levelName + "_purgatory_purgatory");
        if (target != null) {
            player.teleport(new Location(target, this.purgatoryX, this.purgatoryY, this.purgatoryZ));
        } else {
            this.logger.log(Level.SEVERE, String.format(
                "Failed to teleport player %s to purgatory!", player.getName()));
        }

        return true;
    }
}
