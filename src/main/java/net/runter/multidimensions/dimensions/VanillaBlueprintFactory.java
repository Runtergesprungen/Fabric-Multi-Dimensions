package net.runter.multidimensions.dimensions;

import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import net.minecraft.world.biome.source.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.biome.source.MultiNoiseBiomeSourceParameterLists;
import net.minecraft.world.biome.source.TheEndBiomeSource;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;

public class VanillaBlueprintFactory {

    public static DimensionBlueprint createOverworld(
            RegistryEntryLookup<DimensionType> dimensionTypeLookup,
            RegistryEntryLookup<MultiNoiseBiomeSourceParameterList> biomeSourceParameterListLookup,
            RegistryEntryLookup<ChunkGeneratorSettings> chunkGeneratorSettingsLookup
    ) {
        RegistryEntry<DimensionType> dimensionType =
                dimensionTypeLookup.getOrThrow(DimensionTypes.OVERWORLD);

        RegistryEntry<MultiNoiseBiomeSourceParameterList> biomeParameters =
                biomeSourceParameterListLookup.getOrThrow(MultiNoiseBiomeSourceParameterLists.OVERWORLD);

        MultiNoiseBiomeSource biomeSource =
                MultiNoiseBiomeSource.create(biomeParameters);

        RegistryEntry<ChunkGeneratorSettings> generatorSettings =
                chunkGeneratorSettingsLookup.getOrThrow(ChunkGeneratorSettings.OVERWORLD);

        NoiseChunkGenerator chunkGenerator =
                new NoiseChunkGenerator(biomeSource, generatorSettings);

        return DimensionBlueprint.of(dimensionType, chunkGenerator);
    }

    public static DimensionBlueprint createNether(
            RegistryEntryLookup<DimensionType> dimensionTypeLookup,
            RegistryEntryLookup<MultiNoiseBiomeSourceParameterList> biomeSourceParameterListLookup,
            RegistryEntryLookup<ChunkGeneratorSettings> chunkGeneratorSettingsLookup
    ) {
        RegistryEntry<DimensionType> dimensionType =
                dimensionTypeLookup.getOrThrow(DimensionTypes.THE_NETHER);

        RegistryEntry<MultiNoiseBiomeSourceParameterList> biomeParameters =
                biomeSourceParameterListLookup.getOrThrow(MultiNoiseBiomeSourceParameterLists.NETHER);

        MultiNoiseBiomeSource biomeSource =
                MultiNoiseBiomeSource.create(biomeParameters);

        RegistryEntry<ChunkGeneratorSettings> generatorSettings =
                chunkGeneratorSettingsLookup.getOrThrow(ChunkGeneratorSettings.NETHER);

        NoiseChunkGenerator chunkGenerator =
                new NoiseChunkGenerator(biomeSource, generatorSettings);

        return DimensionBlueprint.of(dimensionType, chunkGenerator);
    }

    public static DimensionBlueprint createEnd(
            RegistryEntryLookup<DimensionType> dimensionTypeLookup,
            RegistryEntryLookup<Biome> biomeLookup,
            RegistryEntryLookup<ChunkGeneratorSettings> chunkGeneratorSettingsLookup
    ) {
        RegistryEntry<DimensionType> dimensionType =
                dimensionTypeLookup.getOrThrow(DimensionTypes.THE_END);

        TheEndBiomeSource biomeSource =
                TheEndBiomeSource.createVanilla(biomeLookup);

        RegistryEntry<ChunkGeneratorSettings> generatorSettings =
                chunkGeneratorSettingsLookup.getOrThrow(ChunkGeneratorSettings.END);

        NoiseChunkGenerator chunkGenerator =
                new NoiseChunkGenerator(biomeSource, generatorSettings);

        return DimensionBlueprint.of(dimensionType, chunkGenerator);
    }
}