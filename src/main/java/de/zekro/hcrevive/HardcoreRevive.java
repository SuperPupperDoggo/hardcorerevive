package de.zekro.hcrevive;

import com.superpupperdoggo.hcrevive.generator.PurgatoryGenerator;
import de.zekro.hcrevive.commands.FlushRegister;
import de.zekro.hcrevive.deathregister.DeathRegister;
import de.zekro.hcrevive.listeners.DeathListener;
import de.zekro.hcrevive.listeners.QuitListener;
import de.zekro.hcrevive.listeners.SneakListener;
import com.superpupperdoggo.hcrevive.listeners.PluginEnableListener;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServiceRegisterEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.mvplugins.multiverse.core.MultiverseCoreApi;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.logging.Level;

public final class HardcoreRevive extends JavaPlugin implements Listener {

    private DeathRegister deathRegister;
    private MultiverseCoreApi multiverse;

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
        // Try immediately (covers reloads)
        hookMultiverse();

        // If not ready yet, listen for registration
        if (multiverse == null) {
            Bukkit.getPluginManager().registerEvents(this, this);
        }
        this.registerCommands();
    }

    /*private void hookMultiverse() {
        RegisteredServiceProvider<MultiverseCoreApi> provider =
                Bukkit.getServicesManager().getRegistration(MultiverseCoreApi.class);

        if (provider != null) {
            multiverse = provider.getProvider();
            getLogger().info("Hooked into Multiverse-Core API");

            registerListeners();

            // Optional: unregister this listener now
            HandlerList.unregisterAll((Listener) this);
        }
    }*/

    @EventHandler
    public void onServiceRegister(ServiceRegisterEvent event) {
        /*if (event.getProvider().getService() == MultiverseCoreApi.class) {
            hookMultiverse();
        }*/
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
        return new PurgatoryGenerator();
    }
}
