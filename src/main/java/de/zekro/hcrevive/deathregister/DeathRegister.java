package de.zekro.hcrevive.deathregister;

import de.zekro.hcrevive.HardcoreRevive;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.Particle;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class DeathRegister {

    private final HardcoreRevive plugin;
    private final File         dataFile;
    private final List<Entry>  register;

    public DeathRegister(HardcoreRevive plugin) {
        this.plugin    = plugin;
        this.register  = new ArrayList<>();
        this.dataFile  = new File(plugin.getDataFolder(), "deaths.yml");
        load();  // load from disk on startup
    }

    public void register(Player player, long expiresInTicks) {
        register(player, expiresInTicks, null);
    }

    public void register(Player player, long expiresInTicks, Runnable removeCallback) {
        Entry e = new Entry(player, removeCallback, expiresInTicks);
        register.add(e);
        scheduleExpiry(e);
    }

    private void scheduleExpiry(Entry e) {
        if (e.getExpireAtMillis() > 0) {
            long delayMs = e.getExpireAtMillis() - System.currentTimeMillis();
            if (delayMs <= 0) {
                // already expired
                remove(e);
            } else {
                long ticks = Math.round(delayMs / 50.0);
                plugin.getServer().getScheduler()
                      .runTaskLater(plugin, () -> remove(e), ticks);
            }
        }
    }

    // … get(), remove(Entry), remove(Player), flush() as before …

    /**
     * Save all pending deaths into deaths.yml.
     */
    public void save() {
        YamlConfiguration cfg = new YamlConfiguration();

        for (int i = 0; i < register.size(); i++) {
            Entry e = register.get(i);
            String path = "deaths." + i;
            cfg.set(path + ".uuid",         e.getPlayerId().toString());
            cfg.set(path + ".world",        e.getLocation().getWorld().getName());
            cfg.set(path + ".x",            e.getLocation().getX());
            cfg.set(path + ".y",            e.getLocation().getY());
            cfg.set(path + ".z",            e.getLocation().getZ());
            cfg.set(path + ".pitch",        e.getLocation().getPitch());
            cfg.set(path + ".yaw",          e.getLocation().getYaw());
            cfg.set(path + ".expireAt",     e.getExpireAtMillis());
        }

        try {
            plugin.getDataFolder().mkdirs();
            cfg.save(dataFile);
        } catch (IOException ex) {
            plugin.getLogger().severe("Could not save death register: " + ex.getMessage());
        }
    }

    private void load() {
    if (!dataFile.exists()) return;

    YamlConfiguration cfg = YamlConfiguration.loadConfiguration(dataFile);

    cfg.getKeys(false).forEach(key -> {
        String base     = "deaths." + key + ".";
        UUID   uuid     = UUID.fromString(cfg.getString(base + "uuid"));
        String worldName= cfg.getString(base + "world");
        double x        = cfg.getDouble(base + "x");
        double y        = cfg.getDouble(base + "y");
        double z        = cfg.getDouble(base + "z");
        float  pitch    = (float) cfg.getDouble(base + "pitch");
        float  yaw      = (float) cfg.getDouble(base + "yaw");
        long   expireAt = cfg.getLong(base + "expireAt", 0L);

        // 1) Reconstitute the Location where the player died
        World  world = Bukkit.getWorld(worldName);
        if (world == null) {
            // If the world was renamed or missing, skip re-scheduling this entry
            plugin.getLogger().warning("World \"" + worldName + "\" not found while loading pending deaths for " + uuid);
            return;
        }
        Location loc = new Location(world, x, y, z, yaw, pitch);

        // 2) Start a new repeating task to spawn the same "death beacon" particles at loc
        //    (exactly the same pattern as DeathListener.spawnDeathLocationParticles).
        BukkitTask particleTask = plugin.getServer().getScheduler()
            .runTaskTimer(plugin, () -> {
                // CLOUD at the exact deathpoint
                world.spawnParticle(Particle.CLOUD, loc, 10, 2, 2, 2, 0);
                // Beam of END_ROD from y=0 up to y=250
                for (int i = 0; i < 25; i++) {
                    world.spawnParticle(Particle.END_ROD, loc.getX(), i * 10, loc.getZ(), 10, 0, 10, 0, 0);
                }
            }, 0L, 5L);

        // 3) When this entry is removed (either by revival or expire), cancel the task:
        Runnable onRemove = particleTask::cancel;

        // 4) Rebuild the Entry with the exact same expire timestamp and the `onRemove` callback
        Entry e = new Entry(uuid, loc, onRemove, expireAt);
        register.add(e);

        // 5) Re-schedule its expiry (so remove(e) fires at the same absolute time)
        scheduleExpiry(e);
    });
}

}
