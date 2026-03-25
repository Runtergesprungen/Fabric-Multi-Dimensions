package net.runter.multidimensions.dimensions;

import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

import java.nio.file.Path;

public record StartupWorldTarget(
        String subWorldName,
        String role,
        RegistryKey<World> worldKey,
        Path savePath,
        DimensionBlueprint blueprint
) {
}