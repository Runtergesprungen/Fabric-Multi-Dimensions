package net.runter.multidimensions.dimensions;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

public class DimensionsManager {

    public static ServerWorld getWorld(MinecraftServer server, RegistryKey<World> key) {

        return server.getWorld(key);
    }
}
