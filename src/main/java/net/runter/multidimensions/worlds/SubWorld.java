package net.runter.multidimensions.worlds;

import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import net.runter.multidimensions.dimensions.DimensionKeys;

public class SubWorld {

    private final  String name;
    private final WorldType type;
    private final RegistryKey<World> overworldKey;
    private final RegistryKey<World> netherKey;
    private final RegistryKey<World> endKey;

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

    public RegistryKey<World> getOverworldKey() {
        return overworldKey;
    }

    public RegistryKey<World> getNetherKey() {
        return netherKey;
    }

    public RegistryKey<World> getEndKey() {
        return endKey;
    }

    public RegistryKey<World> getMainWorldKey() {
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

    public RegistryKey<World> getWorldKey(String dimensionName) {
        return switch (dimensionName) {
            case "overworld" -> overworldKey;
            case "nether" -> netherKey;
            case "end" -> endKey;
            default -> null;
        };
    }

    public boolean hasDimension(String dimensionName) {
        return getWorldKey(dimensionName) != null;
    }
}
