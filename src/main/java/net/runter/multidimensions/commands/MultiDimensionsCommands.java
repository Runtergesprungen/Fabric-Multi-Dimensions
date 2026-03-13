package net.runter.multidimensions.commands;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.runter.multidimensions.worlds.WorldManager;
import net.runter.multidimensions.worlds.World;
import net.runter.multidimensions.dimensions.DimensionsManager;
import net.runter.multidimensions.dimensions.DimensionKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import java.util.Set;
import net.minecraft.server.command.ServerCommandSource;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.suggestion.Suggestions;
import java.util.concurrent.CompletableFuture;

public class MultiDimensionsCommands {

    public static void register() {

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {

            dispatcher.register(
                    CommandManager.literal("multidimensions")

                            .then(CommandManager.literal("create")
                                    .then(CommandManager.argument("name", StringArgumentType.word())
                                            .executes(context -> {

                                                String name = StringArgumentType.getString(context, "name");

                                                WorldManager.createWorld(context.getSource().getServer(), name);

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

                                        String worlds = String.join(",", WorldManager.getWorlds());

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

                                                for (String world : WorldManager.getWorlds()) {
                                                    builder.suggest(world);
                                                }

                                                return builder.buildFuture();
                                            })
                                            .executes(context -> {
                                                String name = StringArgumentType.getString(context, "name");

                                                if (!WorldManager.worldExists(name)) {
                                                    context.getSource().sendError(Text.literal("World does not exist: " + name));
                                                    return 0;
                                                }

                                                ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                                MinecraftServer server = context.getSource().getServer();

                                                World world = WorldManager.getWorld(name);

                                                ServerWorld targetWorld = DimensionsManager.getWorld(server, world.getMainWorldKey());
                                                if (targetWorld == null) {
                                                    context.getSource().sendError(Text.literal(
                                                            "Target world is not loaded yet: " + world.getMainWorldKey().getValue()
                                                    ));
                                                    return 0;
                                                }

                                                player.teleport(targetWorld, 0, 100, 0, Set.of(), player.getYaw(), player.getPitch(), true);

                                                context.getSource().sendFeedback(
                                                        () -> Text.literal(
                                                                "Overworld: " + world.getOverworldKey().getValue()
                                                                        + " | Nether: " + world.getNetherKey().getValue()
                                                                        + " | End: " + world.getEndKey().getValue()
                                                        ),
                                                        false
                                                );

                                                return 1;
                                            })
                                    )
                            )

                            .then(CommandManager.literal("tptest")
                                    .executes(context -> {
                                        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                        MinecraftServer server = context.getSource().getServer();

                                        ServerWorld targetWorld = server.getWorld(DimensionKeys.testOverworld());

                                        if (targetWorld == null) {
                                            context.getSource().sendError(Text.literal(
                                                    "Test dimension is not loaded: " + DimensionKeys.testOverworld().getValue()
                                            ));
                                            return 0;
                                        }

                                        player.teleport(
                                                targetWorld,
                                                0,
                                                100,
                                                0,
                                                Set.of(),
                                                player.getYaw(),
                                                player.getPitch(),
                                                true
                                        );

                                        context.getSource().sendFeedback(
                                                () -> Text.literal("Teleported to test dimension."),
                                                false
                                        );

                                        return 1;
                                    })
                            )
            );

        });

    }

}