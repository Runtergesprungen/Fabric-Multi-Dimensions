package net.runter.multidimensions.worlds;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.server.MinecraftServer;
import net.runter.multidimensions.dimensions.DimensionsManager;

public class WorldManager {

    private static final Map<String, World> WORLDS = new HashMap<>();

    public static void createWorld(MinecraftServer server, String name) {
        World world = new World(name, WorldType.FULL);
        WORLDS.put(name, world);

        // attempt to load the overworld dimension
        DimensionsManager.getWorld(server, world.getMainWorldKey());

    }

    public static boolean worldExists(String name) {
        return WORLDS.containsKey(name);

    }

    public static Set<String> getWorlds() {
        return WORLDS.keySet();
    }

    public static World getWorld(String name) {
        return WORLDS.get(name);
    }

}