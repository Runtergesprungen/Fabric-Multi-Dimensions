package net.runter.multidimensions.mixin;

import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.random.RandomSequencesState;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionTypes;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.spawner.SpecialSpawner;
import net.runter.multidimensions.worlds.SubWorldManager;
import net.runter.multidimensions.storage.SubWorldEndFightStorage;
import net.runter.multidimensions.worlds.SubWorld;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.Executor;

@Mixin(ServerWorld.class)
public abstract class ServerWorldEndFightMixin {

    @Shadow
    @Nullable
    private EnderDragonFight enderDragonFight;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void multidimensions$createCustomEndDragonFight(
            MinecraftServer server,
            Executor workerExecutor,
            LevelStorage.Session session,
            ServerWorldProperties properties,
            RegistryKey<World> worldKey,
            DimensionOptions dimensionOptions,
            boolean debugWorld,
            long seed,
            List<SpecialSpawner> spawners,
            boolean shouldTickTime,
            @Nullable RandomSequencesState randomSequenceState,
            CallbackInfo ci
    ) {
        ServerWorld self = (ServerWorld) (Object) this;

        if (this.enderDragonFight != null) {
            return;
        }

        if (SubWorldManager.isSubWorldEnd(worldKey)
                && self.getDimensionEntry().matchesKey(DimensionTypes.THE_END)) {

            SubWorld subWorld = SubWorldManager.findByWorldKey(worldKey);

            if (subWorld != null) {
                this.enderDragonFight = new EnderDragonFight(
                        self,
                        seed,
                        SubWorldEndFightStorage.load(server, subWorld)
                );
            }
        }
    }
}