package de.zekro.hcrevive.listeners;

import de.zekro.hcrevive.HardcoreRevive;
import de.zekro.hcrevive.util.TimeUtil;
import de.zekro.hcrevive.util.WorldUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.entity.Player;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.lang.Math;

/**
 * Listener class binding the {@link PlayerRespawnEvent}.
 */
public class RespawnListener implements Listener {

    private final HardcoreRevive pluginInstance;
    private final Logger logger;
    

    // --- CONFIG VALUES -----------------------
    private final int reviveTimeout;
    private final boolean registerWhenAlone;
    private String levelName;
    private int purgatoryX;
    private int purgatoryY;
    private int purgatoryZ;
    // -----------------------------------------

    /**
     * Initializes a new instance of {@link RespawnListener}.
     * @param pluginInstance The plugin instance
     * @param logger The logger instance.
     */
    public RespawnListener(HardcoreRevive pluginInstance, Logger logger) {
        this.pluginInstance = pluginInstance;
        this.logger = logger;

        this.reviveTimeout = this.pluginInstance.getConfig().getInt("reviveTimeout", 0);
        this.registerWhenAlone = this.pluginInstance.getConfig().getBoolean("registerWhenAlone", true);
        this.levelName = this.pluginInstance.getConfig().getString("levelName", "hardcore");
        this.purgatoryX = this.pluginInstance.getConfig().getInt("purgatoryX", 0);
        this.purgatoryY = this.pluginInstance.getConfig().getInt("purgatoryY", 64);
        this.purgatoryZ = this.pluginInstance.getConfig().getInt("purgatoryZ", 0);
    }

    /**
     * {@link PlayerDeathEvent} event listener.
     * @param event player death event
     */
    @EventHandler
    void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();

        player.setGameMode(GameMode.SURVIVAL);
        World target = player.getServer().getWorld(this.levelName + "_purgatory_purgatory");
        //terrible console command hack
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), String.format("execute in purgatory:purgatory run tp %s %s %s %s",player.getName(),this.purgatoryX, this.purgatoryY, this.purgatoryZ);
        if (target != null) {
            event.setRespawnLocation(new Location(target, this.purgatoryX, this.purgatoryY, this.purgatoryZ));
        } else {
            this.logger.log(Level.SEVERE, String.format(
                "Failed to teleport player %s to purgatory!", player.getName()));
        }

        this.logger.log(Level.INFO, String.format(
                "Player %s respawned in %s", player.getName(), WorldUtil.getName(world)));
        }
}
