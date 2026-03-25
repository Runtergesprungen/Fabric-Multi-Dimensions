package net.runter.multidimensions;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import net.runter.multidimensions.commands.MultiDimensionsCommands;

import net.runter.multidimensions.player.SubWorldRespawnHandler;
import net.runter.multidimensions.worlds.SubWorldManager;
import net.runter.multidimensions.dimensions.DimensionsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiDimensions implements ModInitializer {
	public static final String MOD_ID = "multidimensions";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {

		MultiDimensionsCommands.register();
		SubWorldRespawnHandler.register();

		ServerLifecycleEvents.SERVER_STARTING.register(SubWorldManager::loadWorlds);

		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			DimensionsManager.debugCreateOverworldBlueprint(server);
			DimensionsManager.debugCreateNetherBlueprint(server);
			DimensionsManager.debugCreateEndBlueprint(server);
		});

		ServerLifecycleEvents.SERVER_STOPPING.register(DimensionsManager::saveAllLevelProperties);

	}
}