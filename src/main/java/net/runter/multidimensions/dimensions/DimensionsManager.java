package net.runter.multidimensions.dimensions;

import net.minecraft.registry.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.SpawnLocating;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.RandomSequencesState;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.World;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.biome.source.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.dimension.DimensionOptions;

import net.runter.multidimensions.mixin.MinecraftServerAccessor;
import net.runter.multidimensions.storage.SubWorldEndFightStorage;
import net.runter.multidimensions.storage.SubWorldSpawnStorage;
import net.runter.multidimensions.storage.SubWorldStorage;
import net.runter.multidimensions.worlds.SubWorld;
import net.runter.multidimensions.worlds.SubWorldManager;
import net.runter.multidimensions.mixin.EnderDragonFightInvoker;
import net.runter.multidimensions.worlds.SubWorldLevelProperties;
import net.runter.multidimensions.storage.SubWorldLevelPropertiesStorage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

public class DimensionsManager {

    public enum LoadState {
        LOADED,
        NOT_LOADED
    }

    private static final Map<String, SubWorldLoadPlan> LOAD_PLANS = new HashMap<>();
    private static final Map<RegistryKey<World>, DimensionBlueprint> BLUEPRINTS = new HashMap<>();
    private static final Map<String, List<StartupWorldTarget>> TARGETS = new HashMap<>();

    public static void registerLoadPlan(MinecraftServer server, SubWorld world) {
        SubWorldLoadPlan loadPlan = buildLoadPlan(server, world);

        LOAD_PLANS.put(world.getName(), loadPlan);
        registerBlueprints(loadPlan);
        prepareTargets(server, world);
    }

    public static SubWorldLoadPlan getLoadPlan(String name) {
        return LOAD_PLANS.get(name);
    }

    public static void clearLoadPlans() {
        LOAD_PLANS.clear();
    }

    public static void clearBlueprints() {
        BLUEPRINTS.clear();
    }

    public static void registerBlueprint(RegistryKey<World> key, DimensionBlueprint blueprint) {
        BLUEPRINTS.put(key, blueprint);
    }

    public static DimensionBlueprint getBlueprint(RegistryKey<World> key) {
        return BLUEPRINTS.get(key);
    }

    public static void registerBlueprints(SubWorldLoadPlan loadPlan) {
        registerBlueprint(loadPlan.overworldKey(), loadPlan.overworldBlueprint());
        registerBlueprint(loadPlan.netherKey(), loadPlan.netherBlueprint());
        registerBlueprint(loadPlan.endKey(), loadPlan.endBlueprint());
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

    public static boolean hasBlueprint(RegistryKey<World> key) {
        return getBlueprint(key) != null;
    }

    public static String getBlueprintStatus(SubWorld world) {
        boolean overworldReady = hasBlueprint(world.getOverworldKey());
        boolean netherReady = hasBlueprint(world.getNetherKey());
        boolean endReady = hasBlueprint(world.getEndKey());

        return "overworld=" + overworldReady
                + ", nether=" + netherReady
                + ", end=" +endReady;
    }

    private static final Map<String, SubWorldLevelProperties> LEVEL_PROPERTIES = new HashMap<>();

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

    public static SubWorldLoadPlan buildLoadPlan(MinecraftServer server, SubWorld world) {

        RegistryEntryLookup<DimensionType> dimensionTypeLookup =
                server.getRegistryManager().getOrThrow(RegistryKeys.DIMENSION_TYPE);

        RegistryEntryLookup<MultiNoiseBiomeSourceParameterList> biomeSourceParameterListLookup =
                server.getRegistryManager().getOrThrow(RegistryKeys.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST);

        RegistryEntryLookup<Biome> biomeLookup =
                server.getRegistryManager().getOrThrow(RegistryKeys.BIOME);

        RegistryEntryLookup<ChunkGeneratorSettings> chunkGeneratorSettingsLookup =
                server.getRegistryManager().getOrThrow(RegistryKeys.CHUNK_GENERATOR_SETTINGS);

        DimensionBlueprint overworldBlueprint = VanillaBlueprintFactory.createOverworld(
                dimensionTypeLookup,
                biomeSourceParameterListLookup,
                chunkGeneratorSettingsLookup
        );

        DimensionBlueprint netherBlueprint = VanillaBlueprintFactory.createNether(
                dimensionTypeLookup,
                biomeSourceParameterListLookup,
                chunkGeneratorSettingsLookup
        );

        DimensionBlueprint endBlueprint = VanillaBlueprintFactory.createEnd(
                dimensionTypeLookup,
                biomeLookup,
                chunkGeneratorSettingsLookup
        );

        return new SubWorldLoadPlan(
                world.getOverworldKey(),
                world.getNetherKey(),
                world.getEndKey(),
                overworldBlueprint,
                netherBlueprint,
                endBlueprint
        );
    }

    public static DimensionBlueprint getBlueprint(SubWorld world, RegistryKey<World> key) {
        SubWorldLoadPlan loadPlan = getLoadPlan(world.getName());

        if (loadPlan == null) {
            return null;
        }

        return loadPlan.getBlueprintFor(key);
    }

    public static DimensionBlueprint getBlueprint(String worldName, RegistryKey<World> key) {
        SubWorldLoadPlan loadPlan = getLoadPlan(worldName);

        if (loadPlan == null) {
            return null;
        }

        return loadPlan.getBlueprintFor(key);
    }

    public static String getDimensionRole(SubWorld world, RegistryKey<World> key) {
        SubWorldLoadPlan loadPlan = getLoadPlan(world.getName());

        if (loadPlan == null) {
            return "missing-plan";
        }

        return loadPlan.getDimensionRole(key);
    }

    public static StartupWorldTarget buildTarget(
            MinecraftServer server,
            SubWorld world,
            RegistryKey<World> key
    ) {
        DimensionBlueprint blueprint = getBlueprint(world, key);

        if (blueprint == null) {
            return null;
        }

        String role = getDimensionRole(world, key);
        Path savePath;

        if ("overworld".equals(role)) {
            savePath = SubWorldStorage.getOverworldPath(server, world);
        } else if ("nether".equals(role)) {
            savePath = SubWorldStorage.getNetherPath(server, world);
        } else if ("end".equals(role)) {
            savePath = SubWorldStorage.getEndPath(server, world);
        } else {
            return null;
        }

        return new StartupWorldTarget(
                world.getName(),
                role,
                key,
                savePath,
                blueprint
        );
    }

    public static List<StartupWorldTarget> buildTargets(MinecraftServer server, SubWorld world) {
        List<StartupWorldTarget> targets = new ArrayList<>();

        StartupWorldTarget overworldTarget = buildTarget(server, world, world.getOverworldKey());
        StartupWorldTarget netherTarget = buildTarget(server, world, world.getNetherKey());
        StartupWorldTarget endTarget = buildTarget(server, world, world.getEndKey());

        if (overworldTarget != null) {
            targets.add(overworldTarget);
        }

        if (netherTarget != null) {
            targets.add(netherTarget);
        }

        if (endTarget != null) {
            targets.add(endTarget);
        }

        return targets;
    }

    public static String getTargetStatus(MinecraftServer server, SubWorld world) {
        List<StartupWorldTarget> targets = buildTargets(server, world);

        if (targets.isEmpty()) {
            return "missing";
        }

        StringBuilder builder = new StringBuilder();

        for (StartupWorldTarget target : targets) {
            if (!builder.isEmpty()) {
                builder.append(" | ");
            }

            builder.append(target.role())
                    .append("=")
                    .append(target.worldKey().getValue());
        }

        return builder.toString();
    }

    public static void clearTargets() {
        TARGETS.clear();
    }

    public static void prepareTargets(MinecraftServer server, SubWorld world) {
        TARGETS.put(world.getName(), buildTargets(server, world));
    }

    public static List<StartupWorldTarget> getTargets(String worldName) {
        return TARGETS.get(worldName);
    }

    public static List<StartupWorldTarget> getTargets(SubWorld world) {
        return TARGETS.get(world.getName());
    }

    public static String getPreparedTargetStatus(SubWorld world) {
        List<StartupWorldTarget> targets = getTargets(world);

        if (targets == null || targets.isEmpty()) {
            return "missing";
        }

        StringBuilder builder = new StringBuilder();

        for (StartupWorldTarget target : targets) {
            if (builder.length() > 0) {
                builder.append(" | ");
            }

            builder.append(target.role())
                    .append("=")
                    .append(target.worldKey().getValue());
        }

        return builder.toString();
    }

    public static CreationQueueResult executeCreationQueue() {
        int totalTargets = 0;
        int validTargets = 0;
        int invalidTargets = 0;

        StringBuilder summary = new StringBuilder();

        for (Map.Entry<String, List<StartupWorldTarget>> entry : TARGETS.entrySet()) {
            String subWorldName = entry.getKey();
            List<StartupWorldTarget> targets = entry.getValue();

            if (targets == null || targets.isEmpty()) {
                invalidTargets++;
                appendSummary(summary, subWorldName + ": no targets prepared");
                continue;
            }

            for (StartupWorldTarget target : targets) {
                totalTargets++;

                boolean valid = validateTarget(target);

                if (valid) {
                    validTargets++;
                    appendSummary(summary,
                            subWorldName + " -> " + target.role()
                                    + " ready at " + target.savePath());
                } else {
                    invalidTargets++;
                    appendSummary(summary,
                            subWorldName + " -> " + target.role()
                                    + " invalid");
                }
            }
        }

        return new CreationQueueResult(
                totalTargets,
                validTargets,
                invalidTargets,
                summary.toString()
        );
    }

    private static boolean validateTarget(StartupWorldTarget target) {
        return target != null
                && target.worldKey() != null
                && target.savePath() != null
                && target.blueprint() != null
                && target.role() != null
                && !target.role().isBlank();
    }

    private static void appendSummary(StringBuilder builder, String line) {
        if (builder.length() > 0) {
            builder.append(" || ");
        }

        builder.append(line);
    }

    public static CreationQueueResult executeCreationQueue(SubWorld world) {
        List<StartupWorldTarget> targets = getTargets(world);

        int totalTargets = 0;
        int validTargets = 0;
        int invalidTargets = 0;

        StringBuilder summary = new StringBuilder();

        if (targets == null || targets.isEmpty()) {
            return new CreationQueueResult(0, 0, 1, world.getName() + ": no targets prepared");
        }

        for (StartupWorldTarget target : targets) {
            totalTargets++;

            boolean valid = validateTarget(target);

            if (valid) {
                validTargets++;
                appendSummary(summary,
                        world.getName() + " -> " + target.role()
                                + " ready at " + target.savePath());
            } else {
                invalidTargets++;
                appendSummary(summary,
                        world.getName() + " -> " + target.role()
                                + " invalid");
            }
        }

        return new CreationQueueResult(
                totalTargets,
                validTargets,
                invalidTargets,
                summary.toString()
        );
    }

    public static Map<RegistryKey<DimensionOptions>, DimensionOptions> buildDimensionOptionsMap() {
        Map<RegistryKey<DimensionOptions>, DimensionOptions> result = new LinkedHashMap<>();

        for (List<StartupWorldTarget> targets : TARGETS.values()) {
            if (targets == null) {
                continue;
            }

            for (StartupWorldTarget target : targets) {
                if (target == null || target.blueprint() == null) {
                    continue;
                }

                RegistryKey<DimensionOptions> dimensionKey = RegistryKey.of(
                        RegistryKeys.DIMENSION,
                        target.worldKey().getValue()
                );

                result.put(dimensionKey, target.blueprint().toDimensionOptions());
            }
        }

        return result;
    }

    public static int getPreparedDimensionOptionsCount() {
        return buildDimensionOptionsMap().size();
    }

    public static String getPreparedDimensionOptionsStatus() {
        Map<RegistryKey<DimensionOptions>, DimensionOptions> options = buildDimensionOptionsMap();

        if (options.isEmpty()) {
            return "none";
        }

        StringBuilder builder = new StringBuilder();

        for (RegistryKey<DimensionOptions> key : options.keySet()) {
            if (builder.length() > 0) {
                builder.append(" | ");
            }

            builder.append(key.getValue());
        }

        return builder.toString();
    }

    public static boolean createOverworldNow(MinecraftServer server, SubWorld world) {
        if (server.getWorld(world.getOverworldKey()) != null) {
            return true;
        }

        Map<RegistryKey<World>, ServerWorld> worlds =
                ((MinecraftServerAccessor) server).multidimensions$getWorlds();

        Registry<DimensionOptions> registry =
                server.getCombinedDynamicRegistries()
                        .getCombinedRegistryManager()
                        .getOrThrow(RegistryKeys.DIMENSION);

        RegistryKey<DimensionOptions> dimensionKey = RegistryKey.of(
                RegistryKeys.DIMENSION,
                world.getOverworldKey().getValue()
        );

        DimensionOptions dimensionOptions = registry.get(dimensionKey);

        if (dimensionOptions == null) {
            DimensionBlueprint blueprint = getBlueprint(world, world.getOverworldKey());

            if (blueprint == null) {
                return false;
            }

            dimensionOptions = blueprint.toDimensionOptions();
        }

        System.out.println("Runtime load " + world.getName() + " dimensionOptions = " + (dimensionOptions != null));

        ServerWorld overworld = server.getOverworld();
        if (overworld == null) {
            return false;
        }

        SubWorldLevelProperties properties = getOrCreateLevelProperties(server, world);

        long seed = server.getSaveProperties().getGeneratorOptions().getSeed();
        long biomeSeed = net.minecraft.world.biome.source.BiomeAccess.hashSeed(seed);
        RandomSequencesState randomSequencesState = overworld.getRandomSequences();

        ServerWorld newWorld = new ServerWorld(
                server,
                ((MinecraftServerAccessor) server).multidimensions$getWorkerExecutor(),
                ((MinecraftServerAccessor) server).multidimensions$getSession(),
                properties,
                world.getOverworldKey(),
                dimensionOptions,
                server.getSaveProperties().isDebugWorld(),
                biomeSeed,
                com.google.common.collect.ImmutableList.of(),
                true,
                randomSequencesState
        );

        worlds.put(world.getOverworldKey(), newWorld);
        newWorld.getWorldBorder().setMaxRadius(server.getMaxWorldBorderRadius());

        return server.getWorld(world.getOverworldKey()) != null;
    }

    public static boolean createNetherNow(MinecraftServer server, SubWorld world) {
        if (server.getWorld(world.getNetherKey()) != null) {
            return true;
        }

        Map<RegistryKey<World>, ServerWorld> worlds =
                ((MinecraftServerAccessor) server).multidimensions$getWorlds();

        Registry<DimensionOptions> registry =
                server.getCombinedDynamicRegistries()
                        .getCombinedRegistryManager()
                        .getOrThrow(RegistryKeys.DIMENSION);

        RegistryKey<DimensionOptions> dimensionKey = RegistryKey.of(
                RegistryKeys.DIMENSION,
                world.getNetherKey().getValue()
        );

        DimensionOptions dimensionOptions = registry.get(dimensionKey);

        if (dimensionOptions == null) {
            DimensionBlueprint blueprint = getBlueprint(world, world.getNetherKey());

            if (blueprint == null) {
                return false;
            }

            dimensionOptions = blueprint.toDimensionOptions();
        }

        ServerWorld overworld = server.getOverworld();
        if (overworld == null) {
            return false;
        }

        SubWorldLevelProperties properties = getOrCreateLevelProperties(server, world);

        long seed = server.getSaveProperties().getGeneratorOptions().getSeed();
        long biomeSeed = net.minecraft.world.biome.source.BiomeAccess.hashSeed(seed);
        RandomSequencesState randomSequencesState = overworld.getRandomSequences();

        ServerWorld newWorld = new ServerWorld(
                server,
                ((MinecraftServerAccessor) server).multidimensions$getWorkerExecutor(),
                ((MinecraftServerAccessor) server).multidimensions$getSession(),
                properties,
                world.getNetherKey(),
                dimensionOptions,
                server.getSaveProperties().isDebugWorld(),
                biomeSeed,
                com.google.common.collect.ImmutableList.of(),
                false,
                randomSequencesState
        );

        worlds.put(world.getNetherKey(), newWorld);
        newWorld.getWorldBorder().setMaxRadius(server.getMaxWorldBorderRadius());

        return server.getWorld(world.getNetherKey()) != null;
    }

    public static boolean createEndNow(MinecraftServer server, SubWorld world) {
        if (server.getWorld(world.getEndKey()) != null) {
            return true;
        }

        Map<RegistryKey<World>, ServerWorld> worlds =
                ((MinecraftServerAccessor) server).multidimensions$getWorlds();

        Registry<DimensionOptions> registry =
                server.getCombinedDynamicRegistries()
                        .getCombinedRegistryManager()
                        .getOrThrow(RegistryKeys.DIMENSION);

        RegistryKey<DimensionOptions> dimensionKey = RegistryKey.of(
                RegistryKeys.DIMENSION,
                world.getEndKey().getValue()
        );

        DimensionOptions dimensionOptions = registry.get(dimensionKey);

        if (dimensionOptions == null) {
            DimensionBlueprint blueprint = getBlueprint(world, world.getEndKey());

            if (blueprint == null) {
                return false;
            }

            dimensionOptions = blueprint.toDimensionOptions();
        }

        ServerWorld overworld = server.getOverworld();
        if (overworld == null) {
            return false;
        }

        SubWorldLevelProperties properties = getOrCreateLevelProperties(server, world);

        long seed = server.getSaveProperties().getGeneratorOptions().getSeed();
        long biomeSeed = net.minecraft.world.biome.source.BiomeAccess.hashSeed(seed);
        RandomSequencesState randomSequencesState = overworld.getRandomSequences();

        ServerWorld newWorld = new ServerWorld(
                server,
                ((MinecraftServerAccessor) server).multidimensions$getWorkerExecutor(),
                ((MinecraftServerAccessor) server).multidimensions$getSession(),
                properties,
                world.getEndKey(),
                dimensionOptions,
                server.getSaveProperties().isDebugWorld(),
                biomeSeed,
                com.google.common.collect.ImmutableList.of(),
                false,
                randomSequencesState
        );

        worlds.put(world.getEndKey(), newWorld);
        newWorld.getWorldBorder().setMaxRadius(server.getMaxWorldBorderRadius());

        return server.getWorld(world.getEndKey()) != null;
    }

    public static boolean createSubWorldNow(MinecraftServer server, SubWorld world) {
        boolean overworldLoaded = createOverworldNow(server, world);
        boolean netherLoaded = createNetherNow(server, world);
        boolean endLoaded = createEndNow(server, world);

        boolean loaded = overworldLoaded && netherLoaded && endLoaded;

        if (loaded) {
            prepareCustomOverworldSpawn(server, world);
        }

        return loaded;
    }

    public static void unregisterLoadPlan(String worldName) {
        LOAD_PLANS.remove(worldName);
        TARGETS.remove(worldName);
    }

    public static void unregisterBlueprint(RegistryKey<World> key) {
        BLUEPRINTS.remove(key);
    }

    public static void unregisterBlueprints(SubWorld world) {
        unregisterBlueprint(world.getOverworldKey());
        unregisterBlueprint(world.getNetherKey());
        unregisterBlueprint(world.getEndKey());
    }

    public static void unregisterSubWorld(SubWorld world) {
        unregisterLoadPlan(world.getName());
        unregisterBlueprints(world);
        LEVEL_PROPERTIES.remove(world.getName());
    }

    public static boolean deleteSubWorldNow(MinecraftServer server, SubWorld world) {
        Map<RegistryKey<World>, ServerWorld> worlds =
                ((MinecraftServerAccessor) server).multidimensions$getWorlds();

        ServerWorld spawnWorld = server.getSpawnWorld();
        if (spawnWorld == null) {
            return false;
        }

        List<ServerPlayerEntity> playersToMove = new ArrayList<>();

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            RegistryKey<World> key = player.getEntityWorld().getRegistryKey();

            if (key.equals(world.getOverworldKey())
                    || key.equals(world.getNetherKey())
                    || key.equals(world.getEndKey())) {
                playersToMove.add(player);
            }
        }

        // Move players out first
        for (ServerPlayerEntity player : playersToMove) {
            player.teleport(
                    spawnWorld,
                    server.getSpawnPoint().getPos().getX() + 0.5,
                    server.getSpawnPoint().getPos().getY(),
                    server.getSpawnPoint().getPos().getZ() + 0.5,
                    java.util.Set.of(),
                    player.getYaw(),
                    player.getPitch(),
                    true
            );
        }

        // Get live world instances
        ServerWorld overworld = server.getWorld(world.getOverworldKey());
        ServerWorld nether = server.getWorld(world.getNetherKey());
        ServerWorld end = server.getWorld(world.getEndKey());

        // Let vanilla dragon fight cleanup remove boss bar players after teleport
        if (end != null && end.getEnderDragonFight() != null) {
            ((EnderDragonFightInvoker) end.getEnderDragonFight()).multidimensions$invokeUpdatePlayers();
        }

        // Save before removal
        if (overworld != null) {
            overworld.save(null, true, false);
        }
        if (nether != null) {
            nether.save(null, true, false);
        }
        if (end != null) {
            end.save(null, true, false);
        }

        // Remove from server map first so lookups stop resolving them
        worlds.remove(world.getOverworldKey());
        worlds.remove(world.getNetherKey());
        worlds.remove(world.getEndKey());

        // Close worlds
        try {
            if (overworld != null) {
                overworld.close();
            }
            if (nether != null) {
                nether.close();
            }
            if (end != null) {
                end.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to close subworld: " + world.getName(), e);
        }

        // Save custom level properties
        saveLevelProperties(server, world);

        // Remove mod caches
        unregisterSubWorld(world);

        // Remove metadata
        SubWorldManager.removeWorld(server, world.getName());

        // Remove custom end fight save
        SubWorldEndFightStorage.delete(server, world);

        // Remove custom spawn data
        SubWorldSpawnStorage.delete(server, world);

        // Remove custom level data
        SubWorldLevelPropertiesStorage.delete(server, world);

        // Remove world folders
        SubWorldStorage.deleteWorldFolders(server, world);

        return true;
    }

    public static void ensureOverworldSpawn(MinecraftServer server, SubWorld world) {
        ServerWorld overworld = server.getWorld(world.getOverworldKey());

        if (overworld == null) {
            return;
        }

        SubWorldSpawnStorage.StoredSpawn stored = SubWorldSpawnStorage.load(server, world);
        if (stored != null) {
            return;
        }

        ChunkPos chunkPos = new ChunkPos(
                overworld.getChunkManager().getNoiseConfig().getMultiNoiseSampler().findBestSpawnPosition()
        );

        int y = overworld.getChunkManager().getChunkGenerator().getSpawnHeight(overworld);
        if (y < overworld.getBottomY()) {
            BlockPos start = chunkPos.getStartPos();
            y = overworld.getTopY(Heightmap.Type.WORLD_SURFACE, start.getX() + 8, start.getZ() + 8);
        }

        BlockPos spawn = chunkPos.getStartPos().add(8, y, 8);

        int dx = 0;
        int dz = 0;
        int stepX = 0;
        int stepZ = -1;

        for (int i = 0; i < 121; i++) {
            if (dx >= -5 && dx <= 5 && dz >= -5 && dz <= 5) {
                BlockPos found = SpawnLocating.findServerSpawnPoint(
                        overworld,
                        new ChunkPos(chunkPos.x + dx, chunkPos.z + dz)
                );

                if (found != null) {
                    spawn = found;
                    break;
                }
            }

            if (dx == dz || dx < 0 && dx == -dz || dx > 0 && dx == 1 - dz) {
                int old = stepX;
                stepX = -stepZ;
                stepZ = old;
            }

            dx += stepX;
            dz += stepZ;
        }

        SubWorldSpawnStorage.save(server, world, spawn, 0.0F, 0.0F);
    }

    public static void prepareCustomOverworldSpawn(MinecraftServer server, SubWorld world) {
        ensureOverworldSpawn(server, world);
    }

    public static BlockPos getOverworldSpawn(MinecraftServer server, SubWorld world) {
        SubWorldSpawnStorage.StoredSpawn stored = SubWorldSpawnStorage.load(server, world);

        if (stored != null) {
            return stored.toBlockPos();
        }

        return new BlockPos(0, 100, 0);
    }

    public static float getOverworldSpawnYaw(MinecraftServer server, SubWorld world) {
        SubWorldSpawnStorage.StoredSpawn stored = SubWorldSpawnStorage.load(server, world);

        if (stored != null) {
            return stored.yaw();
        }

        return 0.0F;
    }

    public static float getOverworldSpawnPitch(MinecraftServer server, SubWorld world) {
        SubWorldSpawnStorage.StoredSpawn stored = SubWorldSpawnStorage.load(server, world);

        if (stored != null) {
            return stored.pitch();
        }

        return 0.0F;
    }

    public static BlockPos resolveSafeOverworldSpawn(MinecraftServer server, SubWorld world) {
        ServerWorld overworld = server.getWorld(world.getOverworldKey());

        if (overworld == null) {
            return new BlockPos(0, 100, 0);
        }

        BlockPos center = getOverworldSpawn(server, world);

        BlockPos found = SpawnLocating.findServerSpawnPoint(
                overworld,
                new ChunkPos(center)
        );

        if (found != null) {
            return found;
        }

        int y = overworld.getTopY(Heightmap.Type.WORLD_SURFACE, center.getX(), center.getZ());
        return new BlockPos(center.getX(), y, center.getZ());
    }

    public static SubWorldLevelProperties getOrCreateLevelProperties(MinecraftServer server, SubWorld world) {
        return LEVEL_PROPERTIES.computeIfAbsent(
                world.getName(),
                ignored -> SubWorldLevelPropertiesStorage.loadOrCreate(
                        server,
                        world,
                        server.getSaveProperties(),
                        server.getSaveProperties().getMainWorldProperties()
                )
        );
    }

    public static java.util.List<ServerWorld> getLoadedWorldGroup(MinecraftServer server, SubWorld world) {
        java.util.List<ServerWorld> result = new java.util.ArrayList<>();

        ServerWorld overworld = server.getWorld(world.getOverworldKey());
        ServerWorld nether = server.getWorld(world.getNetherKey());
        ServerWorld end = server.getWorld(world.getEndKey());

        if (overworld != null) {
            result.add(overworld);
        }

        if (nether != null) {
            result.add(nether);
        }

        if (end != null) {
            result.add(end);
        }

        return result;
    }

    public static List<ServerWorld> getLoadedVanillaWorldGroup(MinecraftServer server) {
        List<ServerWorld> result = new ArrayList<>();

        ServerWorld overworld = server.getWorld(World.OVERWORLD);
        ServerWorld nether = server.getWorld(World.NETHER);
        ServerWorld end = server.getWorld(World.END);

        if (overworld != null) {
            result.add(overworld);
        }

        if (nether != null) {
            result.add(nether);
        }

        if (end != null) {
            result.add(end);
        }

        return result;
    }

    public static void saveLevelProperties(MinecraftServer server, SubWorld world) {
        SubWorldLevelProperties properties = LEVEL_PROPERTIES.get(world.getName());

        if (properties == null) {
            return;
        }

        SubWorldLevelPropertiesStorage.save(server, world, properties);
    }

    public static void saveAllLevelProperties(MinecraftServer server) {
        for (String worldName : LEVEL_PROPERTIES.keySet()) {
            if (!SubWorldManager.worldExists(worldName)) {
                continue;
            }

            SubWorld world = SubWorldManager.getWorld(worldName);
            if (world != null) {
                saveLevelProperties(server, world);
            }
        }
    }

}