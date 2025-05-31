package de.zekro.hcrevive.deathregister;

import de.zekro.hcrevive.HardcoreRevive;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

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

    /**
     * Load deaths from deaths.yml, re-schedule expiry.
     */
    private void load() {
        if (!dataFile.exists()) return;
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(dataFile);
        if (!cfg.isConfigurationSection("deaths")) return;

        cfg.getConfigurationSection("deaths").getKeys(false).forEach(key -> {
            String base = "deaths." + key + ".";
            UUID   uuid    = UUID.fromString(cfg.getString(base + "uuid"));
            String world   = cfg.getString(base + "world");
            double x       = cfg.getDouble(base + "x");
            double y       = cfg.getDouble(base + "y");
            double z       = cfg.getDouble(base + "z");
            float  pitch   = (float) cfg.getDouble(base + "pitch");
            float  yaw     = (float) cfg.getDouble(base + "yaw");
            long   expireAt= cfg.getLong(base + "expireAt", 0L);

            Location loc = new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
            Entry e = new Entry(uuid, loc, null, expireAt);
            register.add(e);
            scheduleExpiry(e);
        });
    }
}
