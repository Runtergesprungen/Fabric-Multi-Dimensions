package net.runter.multidimensions.mixin;

import net.minecraft.block.EndPortalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.gen.feature.EndPlatformFeature;
import net.runter.multidimensions.worlds.SubWorld;
import net.runter.multidimensions.worlds.SubWorldManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(EndPortalBlock.class)
public abstract class EndPortalBlockMixin {

    @Inject(method = "createTeleportTarget", at = @At("HEAD"), cancellable = true)
    private void multidimensions$redirectEndPortal(
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
        boolean fromSubWorldEnd = subWorld.getEndKey().equals(currentKey);

        if (!fromSubWorldOverworld && !fromSubWorldEnd) {
            return;
        }

        RegistryKey<World> targetKey;
        BlockPos targetPos;
        float yaw;
        float pitch;
        Set<PositionFlag> flags;
        Vec3d targetVec;

        if (fromSubWorldOverworld) {
            targetKey = subWorld.getEndKey();
            targetPos = ServerWorld.END_SPAWN_POS;
        } else {
            WorldProperties.SpawnPoint spawnPoint = world.getSpawnPoint();
            targetKey = subWorld.getOverworldKey();
            targetPos = spawnPoint.getPos();
        }

        ServerWorld targetWorld = world.getServer().getWorld(targetKey);
        if (targetWorld == null) {
            cir.setReturnValue(null);
            return;
        }

        if (fromSubWorldOverworld) {
            targetVec = targetPos.toBottomCenterPos();
            EndPlatformFeature.generate(targetWorld, BlockPos.ofFloored(targetVec).down(), true);
            yaw = Direction.WEST.getPositiveHorizontalDegrees();
            pitch = 0.0F;
            flags = PositionFlag.combine(PositionFlag.DELTA, Set.of(PositionFlag.X_ROT));

            if (entity instanceof ServerPlayerEntity) {
                targetVec = targetVec.subtract(0.0, 1.0, 0.0);
            }
        } else {
            WorldProperties.SpawnPoint spawnPoint = world.getSpawnPoint();
            yaw = spawnPoint.yaw();
            pitch = spawnPoint.pitch();
            flags = PositionFlag.combine(PositionFlag.DELTA, PositionFlag.ROT);

            if (entity instanceof ServerPlayerEntity serverPlayerEntity) {
                // mirrors vanilla player return behavior
                TeleportTarget respawnTarget = serverPlayerEntity.getRespawnTarget(false, TeleportTarget.NO_OP);
                if (respawnTarget != null && respawnTarget.world() == targetWorld) {
                    cir.setReturnValue(respawnTarget);
                    return;
                }
            }

            targetVec = entity.getWorldSpawnPos(targetWorld, targetPos).toBottomCenterPos();
        }

        cir.setReturnValue(new TeleportTarget(
                targetWorld,
                targetVec,
                Vec3d.ZERO,
                yaw,
                pitch,
                flags,
                TeleportTarget.SEND_TRAVEL_THROUGH_PORTAL_PACKET.then(TeleportTarget.ADD_PORTAL_CHUNK_TICKET)
        ));
    }
}