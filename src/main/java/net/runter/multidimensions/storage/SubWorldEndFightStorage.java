package net.runter.multidimensions.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.runter.multidimensions.worlds.SubWorld;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SubWorldEndFightStorage {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static EnderDragonFight.Data load(MinecraftServer server, SubWorld world) {
        Path file = getFile(server, world);

        if (!Files.exists(file)) {
            return EnderDragonFight.Data.DEFAULT;
        }

        try (Reader reader = Files.newBufferedReader(file)) {
            StoredEndFight stored = GSON.fromJson(reader, StoredEndFight.class);

            if (stored == null) {
                return EnderDragonFight.Data.DEFAULT;
            }

            return new EnderDragonFight.Data(
                    stored.needsStateScanning(),
                    stored.dragonKilled(),
                    stored.previouslyKilled(),
                    stored.isRespawning(),
                    Optional.ofNullable(stored.dragonUuid()),
                    Optional.ofNullable(stored.exitPortalLocation()),
                    Optional.ofNullable(stored.gateways())
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to load end fight data for " + world.getName(), e);
        }
    }

    public static void save(MinecraftServer server, SubWorld world, EnderDragonFight.Data data) {
        Path file = getFile(server, world);

        StoredEndFight stored = new StoredEndFight(
                data.needsStateScanning(),
                data.dragonKilled(),
                data.previouslyKilled(),
                data.isRespawning(),
                data.dragonUUID().orElse(null),
                data.exitPortalLocation().orElse(null),
                data.gateways().orElse(null)
        );

        try {
            Files.createDirectories(file.getParent());

            try (Writer writer = Files.newBufferedWriter(file)) {
                GSON.toJson(stored, writer);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save end fight data for " + world.getName(), e);
        }
    }

    private static Path getFile(MinecraftServer server, SubWorld world) {
        return SubWorldStorage.getSubWorldMetaRoot(server, world)
                .resolve("end_fight.json");
    }

    private record StoredEndFight(
            boolean needsStateScanning,
            boolean dragonKilled,
            boolean previouslyKilled,
            boolean isRespawning,
            UUID dragonUuid,
            BlockPos exitPortalLocation,
            List<Integer> gateways
    ) {
    }

    public static void delete(MinecraftServer server, SubWorld world) {
        Path file = getFile(server, world);

        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete end fight data for " + world.getName(), e);
        }
    }
}