package net.runter.multidimensions.mixin;

import net.minecraft.server.MinecraftServer;
import net.runter.multidimensions.MultiDimensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    @Inject(method = "loadWorld", at = @At("HEAD"))
    private void multidimensions$logLoadWorld(CallbackInfo ci) {
        MultiDimensions.LOGGER.info("========== MinecraftServerMixin loadWorld called ==========");
    }
}