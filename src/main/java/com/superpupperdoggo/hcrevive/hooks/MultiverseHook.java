package com.superpupperdoggo.hooks;

import org.mvplugins.multiverse.core.world.generators.GeneratorPlugin;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.external.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class MultiverseGeneratorPluginHook implements GeneratorPlugin {
    @Override
    public @NotNull String getPluginName() {
        return "HardcoreRevive";
    }

    @Override
    public @Nullable String getInfoLink() {
        return "https://github.com/SuperPupperDoggo/hardcorerevive/";
    }  

    @Override
    public @NotNull Collection<String> suggestIds(@Nullable String currentIdInput) {
        return List.of();
    }

    @Override
    public @Nullable Collection<String> getExampleUsages() {
        return List.of();
    }
}
