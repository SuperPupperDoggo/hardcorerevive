package com.superpupperdoggo.hcrevive.generator;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeGrid;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.util.noise.SimplexOctaveGenerator;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class PurgatoryGenerator extends ChunkGenerator {
    private static final int MIN_Y          = 0;
    private static final int HEIGHT         = 128;
    private static final int BEDROCK_LAYERS = 5;

    private SimplexOctaveGenerator noise;

    public PurgatoryGenerator() {
    }

    @Override
    public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {
        // Initialize noise generator once per world
        if (noise == null) {
            noise = new SimplexOctaveGenerator(world.getSeed(), 8);
            noise.setScale(1 / 16.0);
        }

        ChunkData chunk = createChunkData(world);
        int baseX = chunkX << 4;
        int baseZ = chunkZ << 4;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = baseX + x;
                int worldZ = baseZ + z;

                for (int y = MIN_Y; y < MIN_Y + HEIGHT; y++) {
                    // Bedrock floor and ceiling
                    if (y < MIN_Y + BEDROCK_LAYERS || y >= MIN_Y + HEIGHT - BEDROCK_LAYERS) {
                        chunk.setBlock(x, y, z, Material.BEDROCK);
                    } else {
                        // Carve caves: fill with black concrete where noise positive
                        double value = noise.noise(worldX, y, worldZ, 0.5, 0.5);
                        if (value > 0) {
                            chunk.setBlock(x, y, z, Material.BLACK_CONCRETE);
                        }
                        // Else leave as air (default)
                    }
                }

                // Set biome for column
                biome.setBiome(x, z, Biome.BASALT_DELTAS);
            }
        }

        return chunk;
    }

    @Override
    public List<Biome> getDefaultBiomes(WorldInfo worldInfo) {
        return Collections.singletonList(Biome.BASALT_DELTAS);
    }

    @Override
    public Biome getDefaultBiome(WorldInfo worldInfo, int x, int y, int z) {
        return Biome.BASALT_DELTAS;
    }

    // Disable all vanilla noise/surface/caves/decorations/mobs
    @Override public boolean shouldGenerateNoise(WorldInfo worldInfo, Random random, int x, int z)       { return true;  }
    @Override public boolean shouldGenerateSurface(WorldInfo worldInfo, Random random, int x, int z)     { return false; }
    @Override public boolean shouldGenerateCaves(WorldInfo worldInfo, Random random, int x, int z)       { return false; }
    @Override public boolean shouldGenerateDecorations(WorldInfo worldInfo, Random random, int x, int z) { return false; }
    @Override public boolean shouldGenerateMobs(WorldInfo worldInfo, Random random, int x, int z)        { return false; }
}
