package com.superpupperdoggo.hcrevive.generator;

// Core Bukkit types
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;

// World-gen API
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.ChunkGenerator.ChunkData;
import org.bukkit.generator.WorldInfo;
import org.bukkit.generator.BiomeProvider;

// Noise for cave carving
import org.bukkit.util.noise.SimplexOctaveGenerator;

// Java utilities
import java.util.Random;
import java.util.Collections;
import java.util.List;

public class PurgatoryGenerator extends ChunkGenerator {
    private static final int MIN_Y          = 0;
    private static final int HEIGHT         = 128;
    private static final int BEDROCK_LAYERS = 5;

    private SimplexOctaveGenerator noise;

    @Override
    public ChunkData generateNoise(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunk) {
        // initialize noise once per world
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
                    // bedrock floor and ceiling
                    if (y < MIN_Y + BEDROCK_LAYERS
                     || y >= MIN_Y + HEIGHT - BEDROCK_LAYERS) {
                        chunk.setBlock(x, y, z, Material.BEDROCK);
                    } else {
                        // carve caves by noise > 0: fill with black concrete; else leave air
                        if (noise.noise(worldX, y, worldZ, 0.5, 0.5) > 0) {
                            chunk.setBlock(x, y, z, Material.BLACK_CONCRETE);
                        }
                    }
                }
            }
        }

        return chunk;
    }

    @Override
    public void generateSurface(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunk) {
        // no-op (we handled blocks in generateNoise)
    }

    @Override
    public void generateBedrock(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunk) {
        // no-op (we placed bedrock in generateNoise)
    }

    @Override
    public void generateCaves(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunk) {
        // no-op (we carved caves in generateNoise)
    }

    // disable all vanilla features (structures, mobs, decorations, ores)
    @Override
    public boolean shouldGenerateNoise(WorldInfo worldInfo, Random random, int x, int z)         { return false; }
    @Override
    public boolean shouldGenerateSurface(WorldInfo worldInfo, Random random, int x, int z)       { return false; }
    @Override
    public boolean shouldGenerateCaves(WorldInfo worldInfo, Random random, int x, int z)         { return false; }
    @Override
    public boolean shouldGenerateDecorations(WorldInfo worldInfo, Random random, int x, int z)   { return false; }
    @Override
    public boolean shouldGenerateMobs(WorldInfo worldInfo, Random random, int x, int z)          { return false; }

    // force every chunk to use Basalt Deltas for ambience
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
