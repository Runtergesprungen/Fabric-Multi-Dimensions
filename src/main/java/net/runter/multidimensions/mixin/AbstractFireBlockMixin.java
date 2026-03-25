package net.runter.multidimensions.mixin;

import net.minecraft.world.World;
import net.runter.multidimensions.worlds.SubWorldManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.block.AbstractFireBlock;

@Mixin(AbstractFireBlock.class)
public abstract class AbstractFireBlockMixin {

    @Inject(method = "isOverworldOrNether", at = @At("HEAD"), cancellable = true)
    private static void multidimensions$allowSubWorldPortalLighting(
            World world,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (SubWorldManager.isSubWorldOverworld(world.getRegistryKey())
                || SubWorldManager.isSubWorldNether(world.getRegistryKey())) {
            cir.setReturnValue(true);
        }
    }
}