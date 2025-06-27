package com.superpupperdoggo.hcrevive.generator;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.ChunkGenerator.BiomeGrid;
import org.bukkit.generator.ChunkGenerator.ChunkData;
import org.bukkit.util.noise.SimplexOctaveGenerator;

public class PurgatoryGenerator extends ChunkGenerator {
    private static final int MIN_Y           = 0;
    private static final int HEIGHT          = 128;
    private static final int BEDROCK_LAYERS  = 5;

    // noise for caves
    private SimplexOctaveGenerator noise;

    @Override
    public ChunkData generateChunkData(World world,
                                       Random random,
                                       int chunkX,
                                       int chunkZ,
                                       BiomeGrid biome) {
        // initialize noise on first call
        if (noise == null) {
            noise = new SimplexOctaveGenerator(world, 8);
            noise.setScale(1 / 16.0);
        }

        ChunkData chunk = createChunkData(world);
        int baseX = chunkX << 4;
        int baseZ = chunkZ << 4;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                // force basalt deltas biome everywhere
                biome.setBiome(x, z, Biome.BASALT_DELTAS);

                int worldX = baseX + x;
                int worldZ = baseZ + z;

                for (int y = MIN_Y; y < MIN_Y + HEIGHT; y++) {
                    // bedrock floor/ceiling
                    if (y < MIN_Y + BEDROCK_LAYERS
                     || y >= MIN_Y + HEIGHT - BEDROCK_LAYERS) {
                        chunk.setBlock(x, y, z, Material.BEDROCK);
                    } else {
                        // carve caves: noise > 0 → concrete, else air
                        double d = noise.noise(worldX, y, worldZ, 0.5, 0.5);
                        if (d > 0) {
                            chunk.setBlock(x, y, z, Material.BLACK_CONCRETE);
                        }
                    }
                }
            }
        }

        return chunk;
    }

    @Override
    public boolean shouldGenerateStructures()   { return false; }
    @Override
    public boolean shouldGenerateMobs()         { return false; }
    @Override
    public boolean shouldGenerateOres()         { return false; }
    @Override
    public boolean shouldGenerateDecorations()  { return false; }
}
