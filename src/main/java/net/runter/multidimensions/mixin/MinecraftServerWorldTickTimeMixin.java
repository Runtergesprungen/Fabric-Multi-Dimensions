package net.runter.multidimensions.mixin;

import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.runter.multidimensions.worlds.SubWorld;
import net.runter.multidimensions.worlds.SubWorldManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerWorldTickTimeMixin {

    @ModifyArgs(
            method = "createWorlds",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;<init>(Lnet/minecraft/server/MinecraftServer;Ljava/util/concurrent/Executor;Lnet/minecraft/world/level/storage/LevelStorage$Session;Lnet/minecraft/world/level/ServerWorldProperties;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/world/dimension/DimensionOptions;ZJLjava/util/List;ZLnet/minecraft/util/math/random/RandomSequencesState;)V"
            )
    )
    private void multidimensions$setShouldTickTimeForCustomOverworlds(Args args) {
        RegistryKey<World> worldKey = args.get(4);

        SubWorld subWorld = SubWorldManager.findByWorldKey(worldKey);
        if (subWorld == null) {
            return;
        }

        boolean shouldTickTime = worldKey.equals(subWorld.getOverworldKey());
        args.set(9, shouldTickTime);
    }
}