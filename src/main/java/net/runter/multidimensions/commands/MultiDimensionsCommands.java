package net.runter.multidimensions.commands;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.runter.multidimensions.worlds.WorldManager;
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

                                                WorldManager.createWorld(name);

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

                                                context.getSource().sendFeedback(
                                                        () -> Text.literal("Teleporting to: " + name),
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