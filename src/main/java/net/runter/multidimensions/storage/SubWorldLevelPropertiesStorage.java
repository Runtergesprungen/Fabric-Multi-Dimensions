package net.runter.multidimensions.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.rule.GameRules;
import net.runter.multidimensions.worlds.SubWorld;
import net.runter.multidimensions.worlds.SubWorldLevelProperties;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class SubWorldLevelPropertiesStorage {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static SubWorldLevelProperties loadOrCreate(
            MinecraftServer server,
            SubWorld world,
            SaveProperties saveProperties,
            ServerWorldProperties mainProperties
    ) {
        Path file = getFile(server, world);

        if (!Files.exists(file)) {
            return new SubWorldLevelProperties(
                    saveProperties,
                    mainProperties,
                    0L,
                    0L,
                    false,
                    false,
                    0,
                    0,
                    0,
                    mainProperties.getDifficulty(),
                    mainProperties.isDifficultyLocked(),
                    mainProperties.getWanderingTraderSpawnDelay(),
                    0,
                    null,
                    new GameRules(saveProperties.getEnabledFeatures())
            );
        }

        try (Reader reader = Files.newBufferedReader(file)) {
            StoredLevelProperties stored = GSON.fromJson(reader, StoredLevelProperties.class);

            if (stored == null) {
                return new SubWorldLevelProperties(
                        saveProperties,
                        mainProperties,
                        0L,
                        0L,
                        false,
                        false,
                        0,
                        0,
                        0,
                        mainProperties.getDifficulty(),
                        mainProperties.isDifficultyLocked(),
                        mainProperties.getWanderingTraderSpawnDelay(),
                        0,
                        null,
                        new GameRules(saveProperties.getEnabledFeatures())
                );
            }

            GameRules gameRules = loadGameRules(saveProperties, stored.gamerules());

            return new SubWorldLevelProperties(
                    saveProperties,
                    mainProperties,
                    stored.time(),
                    stored.timeOfDay(),
                    stored.raining(),
                    stored.thundering(),
                    stored.clearWeatherTime(),
                    stored.rainTime(),
                    stored.thunderTime(),
                    stored.difficulty() != null ? Difficulty.valueOf(stored.difficulty()) : mainProperties.getDifficulty(),
                    stored.difficultyLocked(),
                    stored.wanderingTraderSpawnDelay(),
                    stored.wanderingTraderSpawnChance(),
                    stored.wanderingTraderId() != null ? UUID.fromString(stored.wanderingTraderId()) : null,
                    gameRules
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to load level properties for " + world.getName(), e);
        }
    }

    public static void save(MinecraftServer server, SubWorld world, SubWorldLevelProperties properties) {
        Path file = getFile(server, world);

        try {
            Files.createDirectories(file.getParent());

            try (Writer writer = Files.newBufferedWriter(file)) {
                GSON.toJson(
                        new StoredLevelProperties(
                                properties.getTime(),
                                properties.getTimeOfDay(),
                                properties.isRaining(),
                                properties.isThundering(),
                                properties.getClearWeatherTime(),
                                properties.getRainTime(),
                                properties.getThunderTime(),
                                properties.getDifficulty().name(),
                                properties.isDifficultyLocked(),
                                properties.getWanderingTraderSpawnDelay(),
                                properties.getWanderingTraderSpawnChance(),
                                properties.getWanderingTraderId() != null ? properties.getWanderingTraderId().toString() : null,
                                saveGameRules(properties.getGameRules(), server.getSaveProperties().getEnabledFeatures())
                        ),
                        writer
                );
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save level properties for " + world.getName(), e);
        }
    }

    public static void delete(MinecraftServer server, SubWorld world) {
        Path file = getFile(server, world);

        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete level properties for " + world.getName(), e);
        }
    }

    private static JsonElement saveGameRules(GameRules gameRules, net.minecraft.resource.featuretoggle.FeatureSet enabledFeatures) {
        return GameRules.createCodec(enabledFeatures)
                .encodeStart(JsonOps.INSTANCE, gameRules)
                .getOrThrow();
    }

    private static GameRules loadGameRules(SaveProperties saveProperties, JsonElement json) {
        if (json == null || json.isJsonNull()) {
            return new GameRules(saveProperties.getEnabledFeatures());
        }

        return GameRules.createCodec(saveProperties.getEnabledFeatures())
                .parse(JsonOps.INSTANCE, json)
                .getOrThrow();
    }

    private static Path getFile(MinecraftServer server, SubWorld world) {
        return SubWorldStorage.getSubWorldMetaRoot(server, world)
                .resolve("level.json");
    }

    private record StoredLevelProperties(
            long time,
            long timeOfDay,
            boolean raining,
            boolean thundering,
            int clearWeatherTime,
            int rainTime,
            int thunderTime,
            String difficulty,
            boolean difficultyLocked,
            int wanderingTraderSpawnDelay,
            int wanderingTraderSpawnChance,
            String wanderingTraderId,
            JsonElement gamerules
    ) {
    }
}