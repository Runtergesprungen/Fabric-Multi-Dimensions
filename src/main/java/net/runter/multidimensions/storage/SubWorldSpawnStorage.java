package net.runter.multidimensions.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.runter.multidimensions.worlds.SubWorld;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class SubWorldSpawnStorage {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void save(MinecraftServer server, SubWorld world, BlockPos pos, float yaw, float pitch) {
        Path file = getFile(server, world);

        try {
            Files.createDirectories(file.getParent());

            try (Writer writer = Files.newBufferedWriter(file)) {
                GSON.toJson(new StoredSpawn(pos.getX(), pos.getY(), pos.getZ(), yaw, pitch), writer);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save spawn for " + world.getName(), e);
        }
    }

    public static StoredSpawn load(MinecraftServer server, SubWorld world) {
        Path file = getFile(server, world);

        if (!Files.exists(file)) {
            return null;
        }

        try (Reader reader = Files.newBufferedReader(file)) {
            return GSON.fromJson(reader, StoredSpawn.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load spawn for " + world.getName(), e);
        }
    }

    public static void delete(MinecraftServer server, SubWorld world) {
        Path file = getFile(server, world);

        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete spawn for " + world.getName(), e);
        }
    }

    private static Path getFile(MinecraftServer server, SubWorld world) {
        return SubWorldStorage.getSubWorldMetaRoot(server, world)
                .resolve("spawn.json");
    }

    public record StoredSpawn(int x, int y, int z, float yaw, float pitch) {
        public BlockPos toBlockPos() {
            return new BlockPos(x, y, z);
        }
    }
}