package net.runter.multidimensions.mixin;

import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.runter.multidimensions.storage.SubWorldEndFightStorage;
import net.runter.multidimensions.worlds.SubWorld;
import net.runter.multidimensions.worlds.SubWorldManager;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerWorld.class)
public abstract class ServerWorldDragonFightSaveMixin {

    @Shadow
    @Nullable
    private EnderDragonFight enderDragonFight;

    @Redirect(
            method = "savePersistentState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/SaveProperties;setDragonFight(Lnet/minecraft/entity/boss/dragon/EnderDragonFight$Data;)V"
            )
    )

    private void multidimensions$saveOnlyVanillaEndDragonFight(
            net.minecraft.world.SaveProperties saveProperties,
            EnderDragonFight.Data data
    ) {
        ServerWorld self = (ServerWorld) (Object) this;

        if (this.enderDragonFight == null) {
            return;
        }

        if (self.getRegistryKey() == World.END) {
            saveProperties.setDragonFight(data);
            return;
        }

        SubWorld subWorld = SubWorldManager.findByWorldKey(self.getRegistryKey());
        if (subWorld != null && subWorld.getEndKey().equals(self.getRegistryKey())) {
            SubWorldEndFightStorage.save(self.getServer(), subWorld, data);
        }
    }
}