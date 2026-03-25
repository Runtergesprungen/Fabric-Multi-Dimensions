package net.runter.multidimensions.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.DifficultyCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.Difficulty;
import net.runter.multidimensions.dimensions.DimensionsManager;
import net.runter.multidimensions.worlds.SubWorld;
import net.runter.multidimensions.worlds.SubWorldLevelProperties;
import net.runter.multidimensions.worlds.SubWorldManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DifficultyCommand.class)
public class DifficultyCommandMixin {

    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void multidimensions$executeScoped(
            ServerCommandSource source,
            Difficulty difficulty,
            CallbackInfoReturnable<Integer> cir
    ) {
        ServerWorld commandWorld = source.getWorld();
        MinecraftServer server = source.getServer();

        SubWorld subWorld = SubWorldManager.findByWorldKey(commandWorld.getRegistryKey());

        if (subWorld != null) {
            SubWorldLevelProperties properties = DimensionsManager.getOrCreateLevelProperties(server, subWorld);
            properties.setDifficulty(difficulty);

            source.sendFeedback(
                    () -> Text.literal("Set difficulty to " + difficulty.getTranslatableName().getString()
                            + " in subworld " + subWorld.getName()),
                    true
            );

            cir.setReturnValue(0);
            return;
        }

        server.setDifficulty(difficulty, true);

        source.sendFeedback(
                () -> Text.literal("Set difficulty to " + difficulty.getTranslatableName().getString()
                        + " in vanilla worlds"),
                true
        );

        cir.setReturnValue(0);
    }
}