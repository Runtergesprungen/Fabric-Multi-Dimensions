package net.runter.multidimensions.mixin;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.GameRuleCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.runter.multidimensions.worlds.SubWorld;
import net.runter.multidimensions.worlds.SubWorldManager;
import net.minecraft.world.rule.GameRule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRuleCommand.class)
public abstract class GameRuleCommandMixin {

    @Inject(method = "executeSet", at = @At("RETURN"))
    private static <T> void multidimensions$appendScopeMessage(
            CommandContext<ServerCommandSource> context,
            GameRule<T> key,
            CallbackInfoReturnable<Integer> cir
    ) {
        ServerCommandSource source = context.getSource();
        ServerWorld commandWorld = source.getWorld();

        SubWorld subWorld = SubWorldManager.findByWorldKey(commandWorld.getRegistryKey());

        if (subWorld != null) {
            source.sendFeedback(
                    () -> Text.literal("Applied in subworld " + subWorld.getName()),
                    false
            );
        } else {
            source.sendFeedback(
                    () -> Text.literal("Applied in vanilla worlds"),
                    false
            );
        }
    }
}