package de.zekro.hcrevive;

import com.superpupperdoggo.hcrevive.generator.PurgatoryGenerator;
import de.zekro.hcrevive.commands.FlushRegister;
import de.zekro.hcrevive.deathregister.DeathRegister;
import de.zekro.hcrevive.listeners.DeathListener;
import de.zekro.hcrevive.listeners.QuitListener;
import de.zekro.hcrevive.listeners.SneakListener;
import com.superpupperdoggo.hcrevive.listeners.PluginEnableListener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.logging.Level;

public final class HardcoreRevive extends JavaPlugin {

    private DeathRegister deathRegister;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        if (!this.getConfig().getBoolean("enable", true)) {
            this.getLogger().log(Level.WARNING, "disabled by config");
            return;
        }

        if (!this.getServer().isHardcore()) {
            this.getLogger().log(Level.WARNING, "disabled when server is not in hardcore mode");
            return;
        }

        this.deathRegister = new DeathRegister(this);

        this.registerListeners();
        this.registerCommands();
    }

    @Override
    public void onDisable() {
        // If we have a DeathRegister, write out all pending deaths (with their locations) to disk
        if (this.deathRegister != null) {
            this.deathRegister.save();
        }
    }


    private void registerListeners() {
        PluginManager pm = getServer().getPluginManager();

        pm.registerEvents(new DeathListener(this, this.deathRegister, this.getLogger()), this);
        pm.registerEvents(new SneakListener(this, this.deathRegister), this);
        pm.registerEvents(new QuitListener(this.deathRegister), this);
        pm.registerEvents(new QuitListener(this.deathRegister), this);
        pm.registerEvents(new PluginEnableListener(this,this.getLogger()), this);
    }

    private void registerCommands() {
        Objects.requireNonNull(this.getCommand("hcrvFlushRegister"))
                .setExecutor(new FlushRegister(this.deathRegister));
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(@NotNull String worldName, String id) {
        return new PurgatoryGenerator(this,id);
    }
}
