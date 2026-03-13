package net.runter.multidimensions.dimensions;

import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public record DimensionBlueprint(
        RegistryEntry<DimensionType> dimensionType,
        ChunkGenerator chunkGenerator
) {

    public DimensionOptions toDimensionOptions() {
        return new DimensionOptions(dimensionType, chunkGenerator);
    }

    public static DimensionBlueprint of(
            RegistryEntry<DimensionType> dimensionType,
            ChunkGenerator chunkGenerator
    ) {
        return new DimensionBlueprint(dimensionType, chunkGenerator);
    }
}