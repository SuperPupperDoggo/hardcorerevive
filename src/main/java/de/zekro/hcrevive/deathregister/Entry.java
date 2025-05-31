package de.zekro.hcrevive.deathregister;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * {@link DeathRegister} entry element wrapping the
 * died {@link Player}, the death {@link Location},
 * the {@link World} the player died in and an
 * optional {@link Runnable} which is called when
 * the entry was removed or has expired.
 */
public class Entry {
    private final UUID   playerId;
    private final Location location;
    private final Runnable removeCallback;

    /** 0 = never expires; otherwise epoch millis when it should expire */
    private final long   expireAtMillis;

    public Entry(Player player, Runnable removeCallback, long expiresInTicks) {
        this.playerId        = player.getUniqueId();
        this.location        = player.getLocation();
        this.removeCallback  = removeCallback;
        if (expiresInTicks <= 0) {
            this.expireAtMillis = 0L;
        } else {
            this.expireAtMillis = System.currentTimeMillis() + expiresInTicks * 50;
        }
    }

    /** Constructor for deserialization */
    public Entry(UUID playerId, Location loc, Runnable removeCallback, long expireAtMillis) {
        this.playerId        = playerId;
        this.location        = loc;
        this.removeCallback  = removeCallback;
        this.expireAtMillis  = expireAtMillis;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(playerId);
    }
    public UUID getPlayerId() { return playerId; }
    public Location getLocation() { return location; }
    public long getExpireAtMillis() { return expireAtMillis; }

    public void runRemoveCallback() {
        if (removeCallback != null) removeCallback.run();
    }
}
