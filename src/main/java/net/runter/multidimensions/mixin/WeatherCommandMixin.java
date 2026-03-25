package net.runter.multidimensions.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.WeatherCommand;
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

@Mixin(WeatherCommand.class)
public class WeatherCommandMixin {

    @Inject(method = "executeClear", at = @At("HEAD"), cancellable = true)
    private static void multidimensions$executeClearScoped(
            ServerCommandSource source,
            int duration,
            CallbackInfoReturnable<Integer> cir
    ) {
        ServerWorld commandWorld = source.getWorld();

        List<ServerWorld> targets = multidimensions$getTargets(source);

        for (ServerWorld world : targets) {
            world.setWeather(duration, 0, false, false);
        }

        String scopeName = multidimensions$getScopeName(commandWorld);

        source.sendFeedback(
                () -> Text.literal("Set weather to clear in " + scopeName),
                true
        );

        cir.setReturnValue(duration);
    }

    @Inject(method = "executeRain", at = @At("HEAD"), cancellable = true)
    private static void multidimensions$executeRainScoped(
            ServerCommandSource source,
            int duration,
            CallbackInfoReturnable<Integer> cir
    ) {
        ServerWorld commandWorld = source.getWorld();

        List<ServerWorld> targets = multidimensions$getTargets(source);

        for (ServerWorld world : targets) {
            world.setWeather(0, duration, true, false);
        }

        String scopeName = multidimensions$getScopeName(commandWorld);

        source.sendFeedback(
                () -> Text.literal("Set weather to rain in " + scopeName),
                true
        );

        cir.setReturnValue(duration);
    }

    @Inject(method = "executeThunder", at = @At("HEAD"), cancellable = true)
    private static void multidimensions$executeThunderScoped(
            ServerCommandSource source,
            int duration,
            CallbackInfoReturnable<Integer> cir
    ) {
        ServerWorld commandWorld = source.getWorld();

        List<ServerWorld> targets = multidimensions$getTargets(source);

        for (ServerWorld world : targets) {
            world.setWeather(0, duration, true, true);
        }

        String scopeName = multidimensions$getScopeName(commandWorld);

        source.sendFeedback(
                () -> Text.literal("Set weather to thunder in " + scopeName),
                true
        );

        cir.setReturnValue(duration);
    }

    private static List<ServerWorld> multidimensions$getTargets(ServerCommandSource source) {
        ServerWorld commandWorld = source.getWorld();
        MinecraftServer server = source.getServer();

        SubWorld subWorld = SubWorldManager.findByWorldKey(commandWorld.getRegistryKey());

        if (subWorld != null) {
            return DimensionsManager.getLoadedWorldGroup(server, subWorld);
        }

        return DimensionsManager.getLoadedVanillaWorldGroup(server);
    }

    private static String multidimensions$getScopeName(ServerWorld commandWorld) {
        SubWorld subWorld = SubWorldManager.findByWorldKey(commandWorld.getRegistryKey());

        if (subWorld != null) {
            return "subworld " + subWorld.getName();
        }

        return "vanilla worlds";
    }
}