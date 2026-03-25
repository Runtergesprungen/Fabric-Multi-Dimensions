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
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class SubWorldStorage {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type LIST_TYPE = new TypeToken<List<StoredSubWorld>>() {}.getType();

    private static Path getFile(MinecraftServer server) {
        return server.getSavePath(WorldSavePath.ROOT)
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
            Files.createDirectories(getSubWorldMetaRoot(server, world));
        } catch (IOException e) {
            throw new RuntimeException("Failed to create subworld folders for " + world.getName(), e);
        }
    }

    public static void deleteWorldFolders(MinecraftServer server, SubWorld world) {
        deleteIfExists(getOverworldPath(server, world));
        deleteIfExists(getNetherPath(server, world));
        deleteIfExists(getEndPath(server, world));
        deleteIfExists(getSubWorldMetaRoot(server, world));
    }

    private static void deleteIfExists(Path path) {
        if (!Files.exists(path)) {
            return;
        }

        try (Stream<Path> stream = Files.walk(path)) {
            stream.sorted(Comparator.reverseOrder())
                    .forEach(current -> {
                        try {
                            Files.deleteIfExists(current);
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to delete path: " + current, e);
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete path tree: " + path, e);
        }
    }

    public static void save(MinecraftServer server, Iterable<SubWorld> worlds) {
        Path file = getFile(server);
        List<StoredSubWorld> stored = new ArrayList<>();

        for (SubWorld world : worlds) {
            stored.add(new StoredSubWorld(world.getName(), world.getType()));
        }

        try {
            Files.createDirectories(file.getParent());

            try (Writer writer = Files.newBufferedWriter(file)) {
                GSON.toJson(stored, LIST_TYPE, writer);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save subworld list", e);
        }
    }

    public static List<StoredSubWorld> load(MinecraftServer server) {
        Path file = getFile(server);

        if (!Files.exists(file)) {
            return List.of();
        }

        try (Reader reader = Files.newBufferedReader(file)) {
            List<StoredSubWorld> stored = GSON.fromJson(reader, LIST_TYPE);
            return stored != null ? stored : List.of();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load subworld list", e);
        }
    }

    public record StoredSubWorld(String name, WorldType worldType) {
        public WorldType getWorldType() {
            return worldType;
        }
    }

    public static Path getSubWorldMetaRoot(MinecraftServer server, SubWorld world) {
        return server.getSavePath(WorldSavePath.ROOT)
                .resolve("multidimensions")
                .resolve(world.getName());
    }
}