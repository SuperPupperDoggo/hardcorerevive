package com.superpupperdoggo.hcrevive.listeners;

import de.zekro.hcrevive.HardcoreRevive;
import com.superpupperdoggo.hcrevive.hooks.MultiverseHook;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.mvplugins.multiverse.core.MultiverseCoreApi;

import java.lang.reflect.InvocationTargetException;

public class PluginEnableListener implements Listener {
    final Logger logger;
    public PluginEnableListener(HardcoreRevive hcrevive, Logger logger) {
        this.logger = logger;
    }
    @EventHandler(priority = EventPriority.LOW)
    public void onPluginLoad(PluginEnableEvent event) {
        if (event.getPlugin().getName().equals("Multiverse-Core")) {
            try {
                Class.forName("org.mvplugins.multiverse.core.MultiverseCoreApi");
                MultiverseCoreApi.get().getGeneratorProvider().registerGeneratorPlugin(MultiverseHook.class.getDeclaredConstructor().newInstance());
                this.logger.info("Multiverse integration success!");
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException | ClassNotFoundException ex) {
                this.logger.warning("Failed to register generator with Multiverse!");
            }
        }
    }
}
