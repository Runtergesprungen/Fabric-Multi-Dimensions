package net.runter.multidimensions.commands;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import net.runter.multidimensions.dimensions.StartupWorldTarget;
import net.runter.multidimensions.worlds.SubWorld;
import net.runter.multidimensions.worlds.SubWorldManager;
import net.runter.multidimensions.dimensions.DimensionsManager;
import net.runter.multidimensions.dimensions.SubWorldLoadPlan;
import net.runter.multidimensions.dimensions.CreationQueueResult;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;

import com.mojang.brigadier.arguments.StringArgumentType;

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

                                                if (SubWorldManager.worldExists(name)) {
                                                    context.getSource().sendError(Text.literal("World already exists: " + name));
                                                    return 0;
                                                }

                                                boolean created = SubWorldManager.createWorld(context.getSource().getServer(), name);

                                                if (!created) {
                                                    context.getSource().sendError(Text.literal("Failed to create and load world: " + name));
                                                    return 0;
                                                }

                                                context.getSource().sendFeedback(
                                                        () -> Text.literal("Created and loaded world: " + name),
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
                                                for (String worldName : SubWorldManager.getWorlds()) {
                                                    builder.suggest(worldName);
                                                }
                                                return builder.buildFuture();
                                            })
                                            .then(CommandManager.argument("dimension", StringArgumentType.word())
                                                    .suggests((context, builder) -> {
                                                        builder.suggest("overworld");
                                                        builder.suggest("nether");
                                                        builder.suggest("end");
                                                        return builder.buildFuture();
                                                    })
                                                    .executes(context -> {
                                                        String name = StringArgumentType.getString(context, "name");
                                                        String dimension = StringArgumentType.getString(context, "dimension");

                                                        if (!SubWorldManager.worldExists(name)) {
                                                            context.getSource().sendError(Text.literal("World does not exist: " + name));
                                                            return 0;
                                                        }

                                                        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                                        MinecraftServer server = context.getSource().getServer();

                                                        SubWorld subWorld = SubWorldManager.getWorld(name);
                                                        RegistryKey<World> targetKey = subWorld.getWorldKey(dimension);

                                                        if (targetKey == null) {
                                                            context.getSource().sendError(Text.literal("Unknown dimension: " + dimension));
                                                            return 0;
                                                        }

                                                        ServerWorld targetWorld = server.getWorld(targetKey);

                                                        if (targetWorld == null) {
                                                            context.getSource().sendError(
                                                                    Text.literal("Target dimension is not loaded: " + targetKey.getValue())
                                                            );
                                                            return 0;
                                                        }

                                                        double x = 0.5;
                                                        double y = 100.0;
                                                        double z = 0.5;
                                                        float yaw = player.getYaw();
                                                        float pitch = player.getPitch();

                                                        if ("overworld".equals(dimension)) {
                                                            BlockPos spawn = DimensionsManager.resolveSafeOverworldSpawn(server, subWorld);

                                                            x = spawn.getX() + 0.5;
                                                            y = spawn.getY();
                                                            z = spawn.getZ() + 0.5;
                                                            yaw = DimensionsManager.getOverworldSpawnYaw(server, subWorld);
                                                            pitch = DimensionsManager.getOverworldSpawnPitch(server, subWorld);
                                                        }

                                                        player.teleport(
                                                                targetWorld,
                                                                x,
                                                                y,
                                                                z,
                                                                Set.of(),
                                                                yaw,
                                                                pitch,
                                                                true
                                                        );

                                                        context.getSource().sendFeedback(
                                                                () -> Text.literal("Teleported to " + name + " -> " + dimension),
                                                                false
                                                        );

                                                        return 1;
                                                    })
                                            ))
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
                                                SubWorldLoadPlan loadPlan = DimensionsManager.getLoadPlan(name);

                                                String dimensionIds;

                                                if (loadPlan != null) {
                                                    dimensionIds =
                                                            loadPlan.overworldKey().getValue() + ", "
                                                                    + loadPlan.netherKey().getValue() + ", "
                                                                    + loadPlan.endKey().getValue();
                                                } else {
                                                    dimensionIds = "missing";
                                                }

                                                String blueprintStatus = DimensionsManager.getBlueprintStatus(world);

                                                context.getSource().sendFeedback(
                                                        () -> Text.literal(
                                                                "Name: " + world.getName()
                                                                        + " | Type: " + world.getType()
                                                                        + " | Save names: "
                                                                        + world.getOverworldSaveName() + ", "
                                                                        + world.getNetherSaveName() + ", "
                                                                        + world.getEndSaveName()
                                                                        + " | Dimension IDs: "
                                                                        + dimensionIds
                                                                        + " | Plan: "
                                                                        + (loadPlan != null ? "ready" : "missing")
                                                                        + " | Blueprints: "
                                                                        + blueprintStatus
                                                                        + " | Targets: "
                                                                        + DimensionsManager.getPreparedTargetStatus(world)
                                                                        + " | Status: "
                                                                        + DimensionsManager.getLoadStatusMessage(context.getSource().getServer(), world)
                                                                        + " | DimensionOptions: "
                                                                        + DimensionsManager.getPreparedDimensionOptionsCount()
                                                        ),
                                                        false
                                                );

                                                return 1;
                                            })
                                    )
                            )

                            .then(CommandManager.literal("blueprint")
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

                                                boolean overworldReady = DimensionsManager.hasBlueprint(world.getOverworldKey());
                                                boolean netherReady = DimensionsManager.hasBlueprint(world.getNetherKey());
                                                boolean endReady = DimensionsManager.hasBlueprint(world.getEndKey());

                                                context.getSource().sendFeedback(
                                                        () -> Text.literal(
                                                                "Blueprint map for " + world.getName()
                                                                        + " | overworld=" + overworldReady
                                                                        + " | nether=" + netherReady
                                                                        + " | end=" + endReady
                                                        ),
                                                        false
                                                );

                                                return 1;
                                            })
                                    )
                            )

                            .then(CommandManager.literal("targets")
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

                                                var targets = DimensionsManager.getTargets(world);

                                                if (targets == null || targets.isEmpty()) {
                                                    context.getSource().sendError(Text.literal("No targets prepared for: " + name));
                                                    return 0;
                                                }

                                                for (StartupWorldTarget target : targets) {
                                                    context.getSource().sendFeedback(
                                                            () -> Text.literal(
                                                                    "Role: " + target.role()
                                                                            + " | Key: " + target.worldKey().getValue()
                                                                            + " | Path " + target.savePath()
                                                            ),
                                                            false
                                                    );
                                                }

                                                return 1;
                                            })
                                    )
                            )

                            .then(CommandManager.literal("queue")
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
                                                CreationQueueResult result = DimensionsManager.executeCreationQueue(world);

                                                context.getSource().sendFeedback(
                                                        () -> Text.literal(
                                                                "Queue for " + name
                                                                        + " | total=" + result.totalTargets()
                                                                        + " | valid=" + result.validTargets()
                                                                        + " | invalid=" + result.invalidTargets()
                                                        ),
                                                        false
                                                );

                                                context.getSource().sendFeedback(
                                                        () -> Text.literal("Summary: " + result.summary()),
                                                        false
                                                );

                                                return result.invalidTargets() == 0 ? 1 : 0;
                                            })
                                    )
                            )

                            .then(CommandManager.literal("loadnow")
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

                                                boolean loaded = DimensionsManager.createSubWorldNow(
                                                        context.getSource().getServer(),
                                                        world
                                                );

                                                if (!loaded) {
                                                    context.getSource().sendError(Text.literal("Failed to load overworld now: " + name));
                                                    return 0;
                                                }

                                                context.getSource().sendFeedback(
                                                        () -> Text.literal(
                                                                "Loaded subworld now: "
                                                                        + world.getOverworldKey().getValue()
                                                                        + " | "
                                                                        + world.getNetherKey().getValue()
                                                                        + " | "
                                                                        + world.getEndKey().getValue()
                                                        ),
                                                        false
                                                );

                                                return 1;
                                            })
                                    )
                            )

                            .then(CommandManager.literal("delete")
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

                                                boolean deleted = DimensionsManager.deleteSubWorldNow(
                                                        context.getSource().getServer(),
                                                        world
                                                );

                                                if (!deleted) {
                                                    context.getSource().sendError(Text.literal("Failed to delete world: " + name));
                                                    return 0;
                                                }

                                                context.getSource().sendFeedback(
                                                        () -> Text.literal("Deleted world: " + name),
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