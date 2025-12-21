package de.zekro.hcrevive.listeners;

import de.zekro.hcrevive.HardcoreRevive;
import de.zekro.hcrevive.deathregister.DeathRegister;
import de.zekro.hcrevive.util.TimeUtil;
import de.zekro.hcrevive.util.WorldUtil;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.entity.Player;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitTask;
import org.mvplugins.multiverse.core.MultiverseCoreApi;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.lang.Math;

/**
 * Listener class binding the {@link PlayerDeathEvent}.
 *
 * When a player dies and another player is on the server,
 * the death is registered with the death location to be
 * revived by another player.
 */
public class DeathListener implements Listener {

    private final HardcoreRevive pluginInstance;
    private final DeathRegister deathRegister;
    private final Logger logger;
    private final LuckPerms luckPerms = LuckPermsProvider.get();
    private final MultiverseCoreApi coreApi = MultiverseCoreApi.get();
    

    // --- CONFIG VALUES -----------------------
    private final int reviveTimeout;
    private final boolean registerWhenAlone;
    private String reviveWorld;
    private String permadeathWorld;
    private String permadeathAliveGroup;
    private final boolean permadeathDoEject;
    private String permadeathEjectDestination;
    // -----------------------------------------

    /**
     * Initializes a new instance of {@link DeathListener}.
     * @param pluginInstance The plugin instance
     * @param deathRegister The death register instance.
     * @param logger The logger instance.
     */
    public DeathListener(HardcoreRevive pluginInstance, DeathRegister deathRegister, Logger logger) {
        this.pluginInstance = pluginInstance;
        this.deathRegister = deathRegister;
        this.logger = logger;

        this.reviveTimeout = this.pluginInstance.getConfig().getInt("reviveTimeout", 0);
        this.registerWhenAlone = this.pluginInstance.getConfig().getBoolean("registerWhenAlone", true);
        this.reviveWorld = this.pluginInstance.getConfig().getString("reviveWorld", "hardcore");
        this.permadeathWorld = this.pluginInstance.getConfig().getString("permadeathWorld", "purehardcore");
        this.permadeathAliveGroup = this.pluginInstance.getConfig().getString("permadeathAliveGroup", "hardcore_alive");
        this.permadeathDoEject = this.pluginInstance.getConfig().getBoolean("permadeathDoEject", false);
        this.permadeathEjectDestination = this.pluginInstance.getConfig().getString("permadeathEjectLocation", "none");
    }

    /**
     * {@link PlayerDeathEvent} event listener.
     * @param event player death event
     */
    @EventHandler
    void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        World world = player.getWorld();

        if (world.getName().equals(this.reviveWorld)) {

        // Do not run if player is already a spectator
        if (player.getGameMode() == GameMode.SPECTATOR) return;

        // When 'registerWhenAlone' is set to false and
        // less than 2 players are on the server, disable death
        // recording for reviving.
        if (!registerWhenAlone && world.getPlayers().size() < 2) {
            player.sendMessage("Sorry, you are the only player on the server, so you can not be revived. :(");
            return;
        }

        // Get location and schedule loop to spawn death location particles.
        Location deathLocation = player.getLocation();
        World.Environment environment = deathLocation.getWorld().getEnvironment();

        double minY;
        double maxY;
        
        switch (environment) {
            case NETHER:
                minY = 0.0;
                maxY = 127.0;
                break;
            case THE_END:
                minY = 0.0;
                maxY = 255.0;
                break;
            case NORMAL:
            default:
                minY = -64.0;
                maxY = 319.0;
                break;
        }

        deathLocation.setY(Math.clamp(deathLocation.getY(),minY,maxY));
        BukkitTask particleTask = this.pluginInstance.getServer().getScheduler()
                .runTaskTimer(this.pluginInstance, () ->
                        this.spawnDeathLocationParticles(world, deathLocation),0, 5);

        // Register the death in the death register with the particleTask
        // cancel function as remove/expire callback.
        this.deathRegister.register(player, reviveTimeout * 20, particleTask::cancel);

        // Broadcast a message to all players which are not the death victim
        // to signal the victims player death.
        world.getPlayers().stream()
                .filter(p -> p != player)
                .forEach(p -> p.sendMessage(this.getDeathBroadcastMessage(player)));

        // Send a message to the death victim.
        player.sendMessage(this.getDeathVictimMessage());
        } else {
            if (world.getName().equals(this.permadeathWorld)) {
                player.sendMessage("You have died in Pure Hardcore, you cannot rejoin until next reset!");
                // Modify user on the main thread (okay because user is online)
                luckPerms.getUserManager().modifyUser(player.getUniqueId(), user -> {
                    InheritanceNode node = InheritanceNode.builder(this.pluginInstance.getConfig().getString("permadeathAliveGroup", "hardcore_alive")).build();
                    user.data().remove(node);
                });
                if (this.permadeathDoEject) {
                coreApi.getDestinationsProvider().parseDestination(this.permadeathEjectDestination)
                .peek(destination -> {
                    coreApi.getSafetyTeleporter().to(destination)
                            .checkSafety(false)
                            .teleport(player);
                });
                }
            }
        }

        this.logger.log(Level.INFO, String.format(
                "Player %s died in %s", player.getName(), WorldUtil.getName(world)));
    }

    /**
     * Returns a message sent to all other players on the server to indicate
     * where the player died to be revived.
     * If a timeout is specified, this will be given as well in the message.
     * @param victim dead player
     * @return formatted message
     */
    private String getDeathBroadcastMessage(Player victim) {
        StringBuilder res = new StringBuilder();
        Location loc = victim.getLocation();

        res.append(String.format("%s died at %d/%d/%d in %s!",
                victim.getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(),
                WorldUtil.getName(victim.getWorld())));

        if (this.reviveTimeout > 0) {
            res.append(String.format(" You have %s to reach this location before they're dead forever!",
                    TimeUtil.getFormattedTimeSpan(this.reviveTimeout)));
        }

        res.append(" Go and revive them!");

        return res.toString();
    }

    /**
     * Message which is sent to the player died.
     * If a timeout is specified, the time until that will be
     * displayed as well.
     * @return Formatted message.
     */
    private String getDeathVictimMessage() {
        StringBuilder res = new StringBuilder();

        res.append("You died! But don't worry, you can be revived by another player on the server!");

        if (this.reviveTimeout > 0) {
            res.append(String.format(" You must be revived in %s, or you will be dead forever!",
                    TimeUtil.getFormattedTimeSpan(this.reviveTimeout)));
        }

        return res.toString();
    }

    /**
     * Spawn particles at the death location.
     * This spawns a cloud at the exact death point and a beam of
     * particles from Y: 0 to Y: 250 at the death location.
     * @param world the world object
     * @param location the location where the particles are spawned
     */
    private void spawnDeathLocationParticles(World world, Location location) {
        world.spawnParticle(Particle.CLOUD, location,10, 2, 2, 2, 0);
        for (int i = 0; i < 25; i++) {
            world.spawnParticle(Particle.END_ROD, location.getX(), i * 10, location.getZ(),
                    10, 0, 10, 0, 0);
        }
    }
}
