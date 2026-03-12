package net.runter.multidimensions.worlds;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class WorldManager {

    private static final Map<String, World> WORLDS = new HashMap<>();

    public static void createWorld(String name) {

        World world = new World(name);

        WORLDS.put(name, world);

    }

    public static boolean WorldExists(String name) {

        return WORLDS.containsKey(name);

    }

    public static Set<String> getWorlds() {
        return WORLDS.keySet();
    }

}