package net.runter.multidimensions.worlds;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.server.MinecraftServer;
import net.runter.multidimensions.dimensions.DimensionsManager;
import net.runter.multidimensions.storage.SubWorldStorage;

public class SubWorldManager {

    private static final Map<String, SubWorld> WORLDS = new HashMap<>();

    public static boolean createWorld(MinecraftServer server, String name) {
        if (worldExists(name)) {
            return false;
        }

        SubWorld world = new SubWorld(name, WorldType.FULL);
        WORLDS.put(name, world);

        DimensionsManager.prepareSubWorld(server, world);
        DimensionsManager.registerLoadPlan(server, world);
        SubWorldStorage.save(server, getAllWorldObjects());

        boolean loadedNow = DimensionsManager.createSubWorldNow(server, world);

        return loadedNow;
    }

    public static void loadWorlds(MinecraftServer server) {
        WORLDS.clear();
        DimensionsManager.clearLoadPlans();
        DimensionsManager.clearBlueprints();
        DimensionsManager.clearTargets();

        for (SubWorldStorage.StoredSubWorld stored : SubWorldStorage.load(server)) {
            SubWorld world = new SubWorld(
                    stored.name(),
                    stored.getWorldType()
            );

            WORLDS.put(world.getName(), world);

            DimensionsManager.prepareSubWorld(server, world);
            DimensionsManager.registerLoadPlan(server, world);
        }
    }

    public static boolean worldExists(String name) {
        return WORLDS.containsKey(name);

    }

    public static Set<String> getWorlds() {
        return WORLDS.keySet();
    }

    public static SubWorld getWorld(String name) {
        return WORLDS.get(name);
    }

    public static Iterable<SubWorld> getAllWorldObjects() {
        return WORLDS.values();
    }

    public static SubWorld findByWorldKey(net.minecraft.registry.RegistryKey<net.minecraft.world.World> key) {
        for (SubWorld world : WORLDS.values()) {
            if (world.getOverworldKey().equals(key)
                    || world.getNetherKey().equals(key)
                    || world.getEndKey().equals(key)) {
                return world;
            }
        }

        return null;
    }

    public static boolean isSubWorldOverworld(net.minecraft.registry.RegistryKey<net.minecraft.world.World> key) {
        SubWorld world = findByWorldKey(key);
        return world != null && world.getOverworldKey().equals(key);
    }

    public static boolean isSubWorldEnd(net.minecraft.registry.RegistryKey<net.minecraft.world.World> key) {
        SubWorld world = findByWorldKey(key);
        return world != null && world.getEndKey().equals(key);
    }

    public static boolean isSubWorldNether(net.minecraft.registry.RegistryKey<net.minecraft.world.World> key) {
        SubWorld world = findByWorldKey(key);
        return world != null && world.getNetherKey().equals(key);
    }

    public static boolean isSubWorldOwnedKey(net.minecraft.registry.RegistryKey<net.minecraft.world.World> key) {
        return findByWorldKey(key) != null;
    }

    public static void removeWorld(MinecraftServer server, String name) {
        WORLDS.remove(name);
        SubWorldStorage.save(server, getAllWorldObjects());
    }

}