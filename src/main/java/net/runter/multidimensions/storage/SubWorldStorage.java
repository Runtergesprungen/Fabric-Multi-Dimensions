package net.runter.multidimensions.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import net.runter.multidimensions.worlds.SubWorld;
import net.runter.multidimensions.worlds.WorldType;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SubWorldStorage {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type LIST_TYPE = new TypeToken<List<StoredSubWorld>>() {}.getType();

    public static void save(MinecraftServer server, Iterable<SubWorld> worlds) {
        Path file = getFile(server);

        List<StoredSubWorld> storedWorlds = new ArrayList<>();

        for (SubWorld world : worlds) {
            storedWorlds.add(new StoredSubWorld(world.getName(), world.getType().name()));
        }

        try {
            Files.createDirectories(file.getParent());

            try (Writer writer = Files.newBufferedWriter(file)) {
                GSON.toJson(storedWorlds, LIST_TYPE, writer);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save sub worlds", e);
        }
    }

    public static List<StoredSubWorld> load(MinecraftServer server) {
        Path file = getFile(server);

        if (!Files.exists(file)) {
            return new ArrayList<>();
        }

        try (Reader reader = Files.newBufferedReader(file)) {
            List<StoredSubWorld> worlds = GSON.fromJson(reader, LIST_TYPE);
            return worlds != null ? worlds : new ArrayList<>();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load sub worlds", e);
        }
    }

    private static Path getFile(MinecraftServer server) {
        return server.getSavePath(WorldSavePath.ROOT)
                .resolve("config")
                .resolve("multidimensions")
                .resolve("subworlds.json");
    }

    public static Path getDimensionsRoot(MinecraftServer server) {
        return server.getSavePath(WorldSavePath.ROOT).resolve("dimensions");
    }

    public static Path getDimensionPath(MinecraftServer server, String namespace, String path) {
        return getDimensionsRoot(server)
                .resolve(namespace)
                .resolve(path);
    }

    public static Path getOverworldPath(MinecraftServer server, SubWorld world) {
        return getDimensionPath(server, "multidimensions", world.getOverworldSaveName());
    }

    public static Path getNetherPath(MinecraftServer server, SubWorld world) {
        return getDimensionPath(server, "multidimensions", world.getNetherSaveName());
    }

    public static Path getEndPath(MinecraftServer server, SubWorld world) {
        return getDimensionPath(server, "multidimensions", world.getEndSaveName());
    }

    public static void createWorldFolders(MinecraftServer server, SubWorld world) {
        try {
            Files.createDirectories(getOverworldPath(server, world));
            Files.createDirectories(getNetherPath(server, world));
            Files.createDirectories(getEndPath(server, world));
        } catch (IOException e) {
            throw new RuntimeException("Failed to create sub world folders for " + world.getName(), e);
        }
    }

    public record StoredSubWorld(String name, String type) {
        public WorldType getWorldType() {
            return WorldType.valueOf(type);
        }
    }
}