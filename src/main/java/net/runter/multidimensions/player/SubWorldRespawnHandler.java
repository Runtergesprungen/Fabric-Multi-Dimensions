package net.runter.multidimensions.player;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.runter.multidimensions.dimensions.DimensionsManager;
import net.runter.multidimensions.mixin.ServerPlayerRespawnInvoker;
import net.runter.multidimensions.worlds.SubWorld;
import net.runter.multidimensions.worlds.SubWorldManager;

public class SubWorldRespawnHandler {

    public static void register() {
        ServerPlayerEvents.AFTER_RESPAWN.register(SubWorldRespawnHandler::onAfterRespawn);
    }

    private static void onAfterRespawn(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, boolean alive) {
        ServerWorld deathWorld = oldPlayer.getEntityWorld();

        SubWorld subWorld = SubWorldManager.findByWorldKey(deathWorld.getRegistryKey());
        if (subWorld == null) {
            return;
        }

        MinecraftServer server = newPlayer.getEntityWorld().getServer();

        var respawn = oldPlayer.getRespawn();

        // If the player had an explicit respawn and vanilla actually respawned them
        // in that respawn dimension, keep vanilla behavior.
        if (respawn != null
                && newPlayer.getEntityWorld().getRegistryKey().equals(
                ServerPlayerRespawnInvoker.multidimensions$invokeGetDimension(respawn)
        )) {
            return;
        }

        ServerWorld targetWorld = server.getWorld(subWorld.getOverworldKey());
        if (targetWorld == null) {
            return;
        }

        BlockPos spawn = DimensionsManager.resolveSafeOverworldSpawn(server, subWorld);
        float yaw = DimensionsManager.getOverworldSpawnYaw(server, subWorld);
        float pitch = DimensionsManager.getOverworldSpawnPitch(server, subWorld);

        newPlayer.teleport(
                targetWorld,
                spawn.getX() + 0.5,
                spawn.getY(),
                spawn.getZ() + 0.5,
                java.util.Set.of(),
                yaw,
                pitch,
                true
        );
    }
}