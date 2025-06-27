package com.superpupperdoggo.hcrevive.generator;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.ChunkGenerator.ChunkData;
import org.bukkit.generator.WorldInfo;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.util.noise.SimplexOctaveGenerator;

import java.util.Random;
import java.util.Collections;
import java.util.List;

public class PurgatoryGenerator extends ChunkGenerator {
    private static final int MIN_Y          = 0;
    private static final int HEIGHT         = 128;
    private static final int BEDROCK_LAYERS = 5;

    private SimplexOctaveGenerator noise;
    public PurgatoryGenerator() {}
    @Override
    public void generateNoise(WorldInfo worldInfo,
                              Random random,
                              int chunkX,
                              int chunkZ,
                              ChunkData chunk) {
        // one-time noise init per world
        if (noise == null) {
            noise = new SimplexOctaveGenerator(worldInfo.getSeed(), 8);
            noise.setScale(1 / 16.0);
        }

        int baseX = chunkX << 4;
        int baseZ = chunkZ << 4;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = baseX + x;
                int worldZ = baseZ + z;

                for (int y = MIN_Y; y < MIN_Y + HEIGHT; y++) {
                    // bedrock floor & ceiling
                    if (y < MIN_Y + BEDROCK_LAYERS
                     || y >= MIN_Y + HEIGHT - BEDROCK_LAYERS) {
                        chunk.setBlock(x, y, z, Material.BEDROCK);
                    } else {
                        // carve caves: if noise > 0, fill concrete; else leave air
                        if (noise.noise(worldX, y, worldZ, 0.5, 0.5) > 0) {
                            chunk.setBlock(x, y, z, Material.BLACK_CONCRETE);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void generateSurface(WorldInfo worldInfo,
                                Random random,
                                int chunkX,
                                int chunkZ,
                                ChunkData chunk) {
        // no-op
    }

    @Override
    public void generateBedrock(WorldInfo worldInfo,
                                Random random,
                                int chunkX,
                                int chunkZ,
                                ChunkData chunk) {
        // no-op
    }

    @Override
    public void generateCaves(WorldInfo worldInfo,
                              Random random,
                              int chunkX,
                              int chunkZ,
                              ChunkData chunk) {
        // no-op
    }

    // disable all vanilla-populators:
    @Override public boolean shouldGenerateNoise(WorldInfo info, Random r, int x, int z)       { return true; }
    @Override public boolean shouldGenerateSurface(WorldInfo info, Random r, int x, int z)     { return false; }
    @Override public boolean shouldGenerateCaves(WorldInfo info, Random r, int x, int z)       { return false; }
    @Override public boolean shouldGenerateDecorations(WorldInfo info, Random r, int x, int z) { return false; }
    @Override public boolean shouldGenerateMobs(WorldInfo info, Random r, int x, int z)        { return false; }

    @Override
    public BiomeProvider getDefaultBiomeProvider(WorldInfo worldInfo) {
        return new BiomeProvider() {
            @Override
            public List<Biome> getBiomes(WorldInfo info) {
                return Collections.singletonList(Biome.BASALT_DELTAS);
            }
            @Override
            public Biome getBiome(WorldInfo info, int x, int y, int z) {
                return Biome.BASALT_DELTAS;
            }
        };
    }
}
