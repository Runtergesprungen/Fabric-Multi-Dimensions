package net.runter.multidimensions.mixin;

import net.minecraft.block.NetherPortalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.dimension.DimensionType;
import net.runter.multidimensions.worlds.SubWorld;
import net.runter.multidimensions.worlds.SubWorldManager;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NetherPortalBlock.class)
public abstract class NetherPortalBlockMixin {

    @Shadow
    @Nullable
    private TeleportTarget getOrCreateExitPortalTarget(
            ServerWorld world,
            Entity entity,
            BlockPos pos,
            BlockPos scaledPos,
            boolean inNether,
            WorldBorder worldBorder
    ) {
        return null;
    }

    @Inject(method = "createTeleportTarget", at = @At("HEAD"), cancellable = true)
    private void multidimensions$redirectNetherPortal(
            ServerWorld world,
            Entity entity,
            BlockPos pos,
            CallbackInfoReturnable<TeleportTarget> cir
    ) {
        RegistryKey<World> currentKey = world.getRegistryKey();
        SubWorld subWorld = SubWorldManager.findByWorldKey(currentKey);

        if (subWorld == null) {
            return;
        }

        boolean fromSubWorldOverworld = subWorld.getOverworldKey().equals(currentKey);
        boolean fromSubWorldNether = subWorld.getNetherKey().equals(currentKey);

        if (!fromSubWorldOverworld && !fromSubWorldNether) {
            return;
        }

        RegistryKey<World> targetKey = fromSubWorldNether
                ? subWorld.getOverworldKey()
                : subWorld.getNetherKey();

        ServerWorld targetWorld = world.getServer().getWorld(targetKey);
        if (targetWorld == null) {
            cir.setReturnValue(null);
            return;
        }

        boolean targetIsNether = targetWorld.getRegistryKey().equals(subWorld.getNetherKey());
        WorldBorder worldBorder = targetWorld.getWorldBorder();

        double scale = DimensionType.getCoordinateScaleFactor(world.getDimension(), targetWorld.getDimension());
        BlockPos scaledPos = worldBorder.clampFloored(
                entity.getX() * scale,
                entity.getY(),
                entity.getZ() * scale
        );

        TeleportTarget target = this.getOrCreateExitPortalTarget(
                targetWorld,
                entity,
                pos,
                scaledPos,
                targetIsNether,
                worldBorder
        );

        cir.setReturnValue(target);
    }
}