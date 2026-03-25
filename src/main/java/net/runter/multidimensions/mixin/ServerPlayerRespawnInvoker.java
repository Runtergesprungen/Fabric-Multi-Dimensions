package net.runter.multidimensions.mixin;

import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerPlayerEntity.Respawn.class)
public interface ServerPlayerRespawnInvoker {

    @Invoker("getDimension")
    static RegistryKey<World> multidimensions$invokeGetDimension(ServerPlayerEntity.Respawn respawn) {
        throw new AssertionError();
    }
}