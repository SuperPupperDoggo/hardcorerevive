public class PurgatoryGenerator extends ChunkGenerator {
    private static final int MIN_Y = 0, HEIGHT = 128, BEDROCK_LAYERS = 5;
    private SimplexOctaveGenerator noise;

    @Override
    public @NotNull ChunkData generateNoise(@NotNull WorldInfo worldInfo,
                                            @NotNull Random random,
                                            int chunkX, int chunkZ,
                                            @NotNull ChunkData chunk) {
        if (noise == null) {
            noise = new SimplexOctaveGenerator(worldInfo.getSeed(), 8);
            noise.setScale(1/16.0);
        }
        int baseX = chunkX << 4, baseZ = chunkZ << 4;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = baseX + x, worldZ = baseZ + z;
                for (int y = MIN_Y; y < MIN_Y + HEIGHT; y++) {
                    if (y < MIN_Y + BEDROCK_LAYERS || y >= MIN_Y + HEIGHT - BEDROCK_LAYERS) {
                        chunk.setBlock(x, y, z, Material.BEDROCK);
                    } else if (noise.noise(worldX, y, worldZ, 0.5, 0.5) > 0) {
                        chunk.setBlock(x, y, z, Material.BLACK_CONCRETE);
                    }
                }
            }
        }
        return chunk;
    }

    @Override
    public void generateSurface(@NotNull WorldInfo worldInfo,
                                @NotNull Random random,
                                int chunkX, int chunkZ,
                                @NotNull ChunkData chunk) { /* no-op */ }

    @Override
    public void generateBedrock(@NotNull WorldInfo worldInfo,
                                @NotNull Random random,
                                int chunkX, int chunkZ,
                                @NotNull ChunkData chunk) { /* already in noise */ }

    @Override
    public void generateCaves(@NotNull WorldInfo worldInfo,
                              @NotNull Random random,
                              int chunkX, int chunkZ,
                              @NotNull ChunkData chunk) { /* no-op */ }

    @Override
    public boolean shouldGenerateNoise(WorldInfo w, Random r, int x, int z) { return false; }
    @Override
    public boolean shouldGenerateSurface(WorldInfo w, Random r, int x, int z) { return false; }
    @Override
    public boolean shouldGenerateStructures(WorldInfo w, Random r, int x, int z) { return false; }
    @Override
    public boolean shouldGenerateDecorations(WorldInfo w, Random r, int x, int z) { return false; }
    @Override
    public boolean shouldGenerateMobs(WorldInfo w, Random r, int x, int z) { return false; }
    @Override
    public boolean shouldGenerateCaves(WorldInfo w, Random r, int x, int z) { return false; }

    @Override
    public @Nullable BiomeProvider getDefaultBiomeProvider(WorldInfo worldInfo) {
        return new SingleBiomeProvider(Biome.BASALT_DELTAS);
    }
}
