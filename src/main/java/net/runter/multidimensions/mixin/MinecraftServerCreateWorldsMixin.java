package net.runter.multidimensions.mixin;

import com.mojang.serialization.Lifecycle;
import net.minecraft.registry.MutableRegistry;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.entry.RegistryEntryInfo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.dimension.DimensionOptions;
import net.runter.multidimensions.MultiDimensions;
import net.runter.multidimensions.dimensions.DimensionsManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Map;
import java.util.Optional;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerCreateWorldsMixin {

    @ModifyVariable(
            method = "createWorlds",
            at = @At(
                    value = "STORE"
            ),
            ordinal = 0
    )
    private Registry<DimensionOptions> multidimensions$injectDimensionRegistry(Registry<DimensionOptions> registry) {
        Map<RegistryKey<DimensionOptions>, DimensionOptions> customOptions =
                DimensionsManager.buildDimensionOptionsMap();

        MultiDimensions.LOGGER.info("Injecting custom dimension registry entries: {}", customOptions.size());

        if (customOptions.isEmpty()) {
            return registry;
        }

        MutableRegistry<DimensionOptions> merged =
                new SimpleRegistry<>(RegistryKeys.DIMENSION, registry.getLifecycle());

        RegistryEntryInfo stableInfo =
                new RegistryEntryInfo(Optional.empty(), Lifecycle.stable());

        RegistryEntryInfo experimentalInfo =
                new RegistryEntryInfo(Optional.empty(), Lifecycle.experimental());

        for (RegistryKey<DimensionOptions> key : registry.getKeys()) {
            registry.getOptional(key).ifPresent(entry -> {
                if (!merged.contains(key)) {
                    merged.add(key, entry.value(), stableInfo);
                }
            });
        }

        for (Map.Entry<RegistryKey<DimensionOptions>, DimensionOptions> entry : customOptions.entrySet()) {
            if (!merged.contains(entry.getKey())) {
                merged.add(entry.getKey(), entry.getValue(), experimentalInfo);
            }
        }

        return merged.freeze();
    }
}