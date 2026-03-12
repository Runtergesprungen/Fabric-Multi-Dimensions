package net.runter.multidimensions.dimensions;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class DimensionKeys {

    public static final String MOD_ID = "multidimensions";

    public static RegistryKey<World> overworld(String name) {
        return RegistryKey.of(
                RegistryKeys.WORLD,
                Identifier.of(MOD_ID, name + "_overworld")
        );
    }

    public static RegistryKey<World> nether(String name) {
        return RegistryKey.of(
                RegistryKeys.WORLD,
                Identifier.of(MOD_ID, name + "_nether")
        );
    }

    public static RegistryKey<World> end(String name) {
        return RegistryKey.of(
                RegistryKeys.WORLD,
                Identifier.of(MOD_ID, name + "_end")
        );
    }
}