package net.runter.multidimensions.mixin;

import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.concurrent.Executor;

@Mixin(net.minecraft.server.MinecraftServer.class)
public interface MinecraftServerAccessor {

    @Accessor("worlds")
    Map<RegistryKey<World>, ServerWorld> multidimensions$getWorlds();

    @Accessor("session")
    LevelStorage.Session multidimensions$getSession();

    @Accessor("workerExecutor")
    Executor multidimensions$getWorkerExecutor();

    @Accessor("playerManager")
    net.minecraft.server.PlayerManager multidimensions$getPlayerManager();
}