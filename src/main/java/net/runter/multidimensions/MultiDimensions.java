package net.runter.multidimensions;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import net.runter.multidimensions.commands.MultiDimensionsCommands;

import net.runter.multidimensions.worlds.SubWorldManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiDimensions implements ModInitializer {
	public static final String MOD_ID = "multidimensions";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {

		MultiDimensionsCommands.register();

		ServerLifecycleEvents.SERVER_STARTED.register(SubWorldManager::loadWorlds);

	}
}