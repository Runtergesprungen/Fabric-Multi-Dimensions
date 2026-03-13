package net.runter.multidimensions.worlds;

import net.minecraft.registry.RegistryKey;
import net.runter.multidimensions.dimensions.DimensionKeys;

public class SubWorld {

    private final  String name;
    private final WorldType type;
    private final RegistryKey<net.minecraft.world.World> overworldKey;
    private final RegistryKey<net.minecraft.world.World> netherKey;
    private final RegistryKey<net.minecraft.world.World> endKey;

    public SubWorld(String name, WorldType type) {
        this.name = name;
        this.type = type;

        this.overworldKey = DimensionKeys.overworld(name);
        this.netherKey = DimensionKeys.nether(name);
        this.endKey = DimensionKeys.end(name);
    }

    public String getName() {
        return name;
    }

    public WorldType getType() {
        return type;
    }

    public RegistryKey<net.minecraft.world.World> getOverworldKey() {
        return overworldKey;
    }

    public RegistryKey<net.minecraft.world.World> getNetherKey() {
        return netherKey;
    }

    public RegistryKey<net.minecraft.world.World> getEndKey() {
        return endKey;
    }

    public RegistryKey<net.minecraft.world.World> getMainWorldKey() {
        return overworldKey;
    }

    public String getOverworldSaveName() {
        return name + "_overworld";
    }

    public String getNetherSaveName() {
        return name + "_nether";
    }

    public String getEndSaveName() {
        return name + "_end";
    }
}
