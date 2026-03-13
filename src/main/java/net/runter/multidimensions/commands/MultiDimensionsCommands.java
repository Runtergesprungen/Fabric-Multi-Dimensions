package net.runter.multidimensions.commands;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.runter.multidimensions.worlds.SubWorld;
import net.runter.multidimensions.worlds.SubWorldManager;
import net.runter.multidimensions.dimensions.DimensionsManager;
import net.runter.multidimensions.dimensions.DimensionKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.Set;

public class MultiDimensionsCommands {

    public static void register() {

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {

            dispatcher.register(
                    CommandManager.literal("multidimensions")

                            .then(CommandManager.literal("create")
                                    .then(CommandManager.argument("name", StringArgumentType.word())
                                            .executes(context -> {

                                                String name = StringArgumentType.getString(context, "name");

                                                boolean created = SubWorldManager.createWorld(context.getSource().getServer(), name);

                                                if (!created) {
                                                    context.getSource().sendError(Text.literal("World already exists: " + name));
                                                    return 0;
                                                }

                                                context.getSource().sendFeedback(
                                                        () -> Text.literal("Created world: " + name),
                                                        false
                                                );

                                                return 1;
                                            })
                                    )
                            )

                            .then(CommandManager.literal("list")
                                    .executes(context -> {

                                        String worlds = String.join(",", SubWorldManager.getWorlds());

                                        context.getSource().sendFeedback(
                                                () -> Text.literal("Worlds: " + worlds),
                                                false
                                        );

                                        return 1;
                                    })
                            )

                            .then(CommandManager.literal("tp")
                                    .then(CommandManager.argument("name", StringArgumentType.word())
                                            .suggests((context, builder) -> {

                                                for (String world : SubWorldManager.getWorlds()) {
                                                    builder.suggest(world);
                                                }

                                                return builder.buildFuture();
                                            })
                                            .executes(context -> {
                                                String name = StringArgumentType.getString(context, "name");

                                                if (!SubWorldManager.worldExists(name)) {
                                                    context.getSource().sendError(Text.literal("World does not exist: " + name));
                                                    return 0;
                                                }

                                                ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                                MinecraftServer server = context.getSource().getServer();

                                                SubWorld subWorld = SubWorldManager.getWorld(name);

                                                ServerWorld targetWorld = DimensionsManager.getWorld(server, subWorld.getMainWorldKey());
                                                if (targetWorld == null) {
                                                    context.getSource().sendError(Text.literal(
                                                            "Target world is not loaded yet: " + subWorld.getMainWorldKey().getValue()
                                                    ));
                                                    return 0;
                                                }

                                                player.teleport(targetWorld, 0, 100, 0, Set.of(), player.getYaw(), player.getPitch(), true);

                                                context.getSource().sendFeedback(
                                                        () -> Text.literal(
                                                                "Overworld: " + subWorld.getOverworldKey().getValue()
                                                                        + " | Nether: " + subWorld.getNetherKey().getValue()
                                                                        + " | End: " + subWorld.getEndKey().getValue()
                                                        ),
                                                        false
                                                );

                                                return 1;
                                            })
                                    )
                            )

                            .then(CommandManager.literal("info")
                                    .then(CommandManager.argument("name", StringArgumentType.word())
                                            .suggests((context, builder) -> {
                                                for (String worldName : SubWorldManager.getWorlds()) {
                                                    builder.suggest(worldName);
                                                }
                                                return builder.buildFuture();
                                            })
                                            .executes(context -> {

                                                String name = StringArgumentType.getString(context, "name");

                                                if (!SubWorldManager.worldExists(name)) {
                                                    context.getSource().sendError(Text.literal("World does not exist: " + name));
                                                    return 0;
                                                }

                                                SubWorld world = SubWorldManager.getWorld(name);

                                                context.getSource().sendFeedback(
                                                        () -> Text.literal(
                                                                "Name: " + world.getName()
                                                                        + " | Type: " + world.getType()
                                                                        + " | Save names: "
                                                                        + world.getOverworldSaveName() + ", "
                                                                        + world.getNetherSaveName() + ", "
                                                                        + world.getEndSaveName()
                                                        ),
                                                        false
                                                );

                                                return 1;
                                            })
                                    )
                            )
            );

        });

    }

}