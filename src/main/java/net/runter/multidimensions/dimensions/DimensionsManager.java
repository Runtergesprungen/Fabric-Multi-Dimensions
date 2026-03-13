package net.runter.multidimensions.dimensions;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.biome.source.MultiNoiseBiomeSourceParameterList;
import net.runter.multidimensions.storage.SubWorldStorage;
import net.runter.multidimensions.worlds.SubWorld;

public class DimensionsManager {

    public enum LoadState {
        LOADED,
        NOT_LOADED
    }

    public static void prepareSubWorld(MinecraftServer server, SubWorld world) {
        SubWorldStorage.createWorldFolders(server, world);
    }

    public static ServerWorld getWorld(MinecraftServer server, SubWorld world) {
        return server.getWorld(world.getMainWorldKey());
    }

    public static LoadState getLoadState(MinecraftServer server, SubWorld world) {
        return getWorld(server, world) != null ? LoadState.LOADED : LoadState.NOT_LOADED;
    }

    public static boolean isLoaded(MinecraftServer server, SubWorld world) {
        return getLoadState(server, world) == LoadState.LOADED;
    }

    public static String getLoadStatusMessage(MinecraftServer server, SubWorld world) {
        ServerWorld loadedWorld = getWorld(server, world);

        if (loadedWorld != null) {
            return "Loaded: " + world.getMainWorldKey().getValue();
        }

        return "Not loaded yet: " + world.getMainWorldKey().getValue();
    }

    public static void debugCreateEndBlueprint(MinecraftServer server) {

        RegistryEntryLookup<DimensionType> dimensionTypeLookup =
                server.getRegistryManager().getOrThrow(RegistryKeys.DIMENSION_TYPE);

        RegistryEntryLookup<Biome> biomeLookup =
                server.getRegistryManager().getOrThrow(RegistryKeys.BIOME);

        RegistryEntryLookup<ChunkGeneratorSettings> chunkGeneratorSettingsLookup =
                server.getRegistryManager().getOrThrow(RegistryKeys.CHUNK_GENERATOR_SETTINGS);

        DimensionBlueprint blueprint = VanillaBlueprintFactory.createEnd(
                dimensionTypeLookup,
                biomeLookup,
                chunkGeneratorSettingsLookup
        );

    }

    public static void debugCreateOverworldBlueprint(MinecraftServer server) {

        RegistryEntryLookup<DimensionType> dimensionTypeLookup =
                server.getRegistryManager().getOrThrow(RegistryKeys.DIMENSION_TYPE);

        RegistryEntryLookup<MultiNoiseBiomeSourceParameterList> biomeSourceParameterListLookup =
                server.getRegistryManager().getOrThrow(RegistryKeys.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST);

        RegistryEntryLookup<ChunkGeneratorSettings> chunkGeneratorSettingsLookup =
                server.getRegistryManager().getOrThrow(RegistryKeys.CHUNK_GENERATOR_SETTINGS);

        DimensionBlueprint blueprint = VanillaBlueprintFactory.createOverworld(
                dimensionTypeLookup,
                biomeSourceParameterListLookup,
                chunkGeneratorSettingsLookup
        );
    }

    public static void debugCreateNetherBlueprint(MinecraftServer server) {

        RegistryEntryLookup<DimensionType> dimensionTypeLookup =
                server.getRegistryManager().getOrThrow(RegistryKeys.DIMENSION_TYPE);

        RegistryEntryLookup<MultiNoiseBiomeSourceParameterList> biomeSourceParameterListLookup =
                server.getRegistryManager().getOrThrow(RegistryKeys.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST);

        RegistryEntryLookup<ChunkGeneratorSettings> chunkGeneratorSettingsLookup =
                server.getRegistryManager().getOrThrow(RegistryKeys.CHUNK_GENERATOR_SETTINGS);

        DimensionBlueprint blueprint = VanillaBlueprintFactory.createNether(
                dimensionTypeLookup,
                biomeSourceParameterListLookup,
                chunkGeneratorSettingsLookup
        );
    }
}