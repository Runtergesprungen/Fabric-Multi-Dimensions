package net.runter.multidimensions.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.TimeCommand;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.runter.multidimensions.dimensions.DimensionsManager;
import net.runter.multidimensions.worlds.SubWorld;
import net.runter.multidimensions.worlds.SubWorldManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(TimeCommand.class)
public class TimeCommandMixin {

    @Inject(method = "executeSet", at = @At("HEAD"), cancellable = true)
    private static void multidimensions$executeSetScoped(
            ServerCommandSource source,
            int time,
            CallbackInfoReturnable<Integer> cir
    ) {
        ServerWorld commandWorld = source.getWorld();
        MinecraftServer server = source.getServer();

        List<ServerWorld> targets;
        SubWorld subWorld = SubWorldManager.findByWorldKey(commandWorld.getRegistryKey());

        if (subWorld != null) {
            targets = DimensionsManager.getLoadedWorldGroup(server, subWorld);
        } else {
            targets = DimensionsManager.getLoadedVanillaWorldGroup(server);
        }

        for (ServerWorld world : targets) {
            world.setTimeOfDay(time);
        }

        String scopeName = multidimensions$getScopeName(commandWorld);

        source.sendFeedback(
                () -> Text.literal("Set time to " + time + " in " + scopeName),
                true
        );

        cir.setReturnValue((int) (commandWorld.getTimeOfDay() % 24000L));
    }

    @Inject(method = "executeAdd", at = @At("HEAD"), cancellable = true)
    private static void multidimensions$executeAddScoped(
            ServerCommandSource source,
            int time,
            CallbackInfoReturnable<Integer> cir
    ) {
        ServerWorld commandWorld = source.getWorld();
        MinecraftServer server = source.getServer();

        List<ServerWorld> targets;
        SubWorld subWorld = SubWorldManager.findByWorldKey(commandWorld.getRegistryKey());

        if (subWorld != null) {
            targets = DimensionsManager.getLoadedWorldGroup(server, subWorld);
        } else {
            targets = DimensionsManager.getLoadedVanillaWorldGroup(server);
        }

        for (ServerWorld world : targets) {
            world.setTimeOfDay(world.getTimeOfDay() + time);
        }

        String scopeName = multidimensions$getScopeName(commandWorld);

        source.sendFeedback(
                () -> Text.literal("Added " + time + " ticks in " + scopeName),
                true
        );

        cir.setReturnValue((int) (commandWorld.getTimeOfDay() % 24000L));
    }

    private static String multidimensions$getScopeName(ServerWorld commandWorld) {
        SubWorld subWorld = SubWorldManager.findByWorldKey(commandWorld.getRegistryKey());

        if (subWorld != null) {
            return "subworld " + subWorld.getName();
        }

        return "vanilla worlds";
    }
}