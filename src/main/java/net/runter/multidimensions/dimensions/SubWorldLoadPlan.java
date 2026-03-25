package net.runter.multidimensions.dimensions;

import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

public record SubWorldLoadPlan(
        RegistryKey<World> overworldKey,
        RegistryKey<World> netherKey,
        RegistryKey<World> endKey,
        DimensionBlueprint overworldBlueprint,
        DimensionBlueprint netherBlueprint,
        DimensionBlueprint endBlueprint
) {

    public boolean matches(RegistryKey<World> key) {
        return overworldKey.equals(key)
                || netherKey.equals(key)
                || endKey.equals(key);
    }

    public DimensionBlueprint getBlueprintFor(RegistryKey<World> key) {
        if (overworldKey.equals(key)) {
            return overworldBlueprint;
        }

        if (netherKey.equals(key)) {
            return netherBlueprint;
        }

        if (endKey.equals(key)) {
            return endBlueprint;
        }

        return null;
    }

    public String getDimensionRole(RegistryKey<World> key) {
        if (overworldKey.equals(key)) {
            return "overworld";
        }

        if (netherKey.equals(key)) {
            return "nether";
        }

        if (endKey.equals(key)) {
            return "end";
        }

        return "unknown";
    }
}