package net.runter.multidimensions.worlds;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.server.MinecraftServer;
import net.runter.multidimensions.dimensions.DimensionsManager;
import net.runter.multidimensions.storage.SubWorldStorage;

public class SubWorldManager {

    private static final Map<String, SubWorld> WORLDS = new HashMap<>();

    public static void createWorld(MinecraftServer server, String name) {
        SubWorld world = new SubWorld(name, WorldType.FULL);
        WORLDS.put(name, world);

        SubWorldStorage.save(server, getAllWorldObjects());

        DimensionsManager.getWorld(server, world.getMainWorldKey());

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

}