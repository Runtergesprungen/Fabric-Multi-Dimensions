package net.runter.multidimensions.worlds;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.rule.GameRules;

import java.util.Optional;
import java.util.UUID;

public class SubWorldLevelProperties implements ServerWorldProperties {

    private final SaveProperties saveProperties;
    private final ServerWorldProperties mainProperties;
    private final GameRules gameRules;

    private long time;
    private long timeOfDay;

    private boolean raining;
    private boolean thundering;
    private int clearWeatherTime;
    private int rainTime;
    private int thunderTime;

    private Difficulty difficulty;
    private boolean difficultyLocked;

    private int wanderingTraderSpawnDelay;
    private int wanderingTraderSpawnChance;
    private UUID wanderingTraderId;

    public SubWorldLevelProperties(
            SaveProperties saveProperties,
            ServerWorldProperties mainProperties,
            long time,
            long timeOfDay,
            boolean raining,
            boolean thundering,
            int clearWeatherTime,
            int rainTime,
            int thunderTime,
            Difficulty difficulty,
            boolean difficultyLocked,
            int wanderingTraderSpawnDelay,
            int wanderingTraderSpawnChance,
            UUID wanderingTraderId,
            GameRules gameRules
    ) {
        this.saveProperties = saveProperties;
        this.mainProperties = mainProperties;
        this.gameRules = gameRules;
        this.time = time;
        this.timeOfDay = timeOfDay;
        this.raining = raining;
        this.thundering = thundering;
        this.clearWeatherTime = clearWeatherTime;
        this.rainTime = rainTime;
        this.thunderTime = thunderTime;
        this.difficulty = difficulty;
        this.difficultyLocked = difficultyLocked;
        this.wanderingTraderSpawnDelay = wanderingTraderSpawnDelay;
        this.wanderingTraderSpawnChance = wanderingTraderSpawnChance;
        this.wanderingTraderId = wanderingTraderId;
    }

    @Override
    public long getTime() {
        return time;
    }

    @Override
    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public long getTimeOfDay() {
        return timeOfDay;
    }

    @Override
    public void setTimeOfDay(long timeOfDay) {
        this.timeOfDay = timeOfDay;
    }

    @Override
    public GameMode getGameMode() {
        return mainProperties.getGameMode();
    }

    @Override
    public void setGameMode(GameMode gameMode) {
        mainProperties.setGameMode(gameMode);
    }

    @Override
    public boolean isHardcore() {
        return mainProperties.isHardcore();
    }

    @Override
    public Difficulty getDifficulty() {
        return difficulty;
    }

    @Override
    public boolean isDifficultyLocked() {
        return difficultyLocked;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public void setDifficultyLocked(boolean difficultyLocked) {
        this.difficultyLocked = difficultyLocked;
    }

    @Override
    public GameRules getGameRules() {
        return gameRules;
    }

    @Override
    public String getLevelName() {
        return mainProperties.getLevelName();
    }

    @Override
    public WorldProperties.SpawnPoint getSpawnPoint() {
        return mainProperties.getSpawnPoint();
    }

    @Override
    public void setSpawnPoint(WorldProperties.SpawnPoint spawnPoint) {
        mainProperties.setSpawnPoint(spawnPoint);
    }

    @Override
    public boolean isRaining() {
        return raining;
    }

    @Override
    public void setRaining(boolean raining) {
        this.raining = raining;
    }

    @Override
    public boolean isThundering() {
        return thundering;
    }

    @Override
    public void setThundering(boolean thundering) {
        this.thundering = thundering;
    }

    @Override
    public int getClearWeatherTime() {
        return clearWeatherTime;
    }

    @Override
    public void setClearWeatherTime(int clearWeatherTime) {
        this.clearWeatherTime = clearWeatherTime;
    }

    @Override
    public int getRainTime() {
        return rainTime;
    }

    @Override
    public void setRainTime(int rainTime) {
        this.rainTime = rainTime;
    }

    @Override
    public int getThunderTime() {
        return thunderTime;
    }

    @Override
    public void setThunderTime(int thunderTime) {
        this.thunderTime = thunderTime;
    }

    @Override
    public boolean isInitialized() {
        return mainProperties.isInitialized();
    }

    @Override
    public void setInitialized(boolean initialized) {
        mainProperties.setInitialized(initialized);
    }

    @Override
    public boolean areCommandsAllowed() {
        return saveProperties.areCommandsAllowed();
    }

    public boolean isFlatWorld() {
        return saveProperties.isFlatWorld();
    }

    public boolean isDebugWorld() {
        return saveProperties.isDebugWorld();
    }

    @Override
    public Optional<net.minecraft.world.border.WorldBorder.Properties> getWorldBorder() {
        return mainProperties.getWorldBorder();
    }

    @Override
    public void setWorldBorder(Optional<net.minecraft.world.border.WorldBorder.Properties> worldBorder) {
        mainProperties.setWorldBorder(worldBorder);
    }

    @Override
    public net.minecraft.world.timer.Timer<MinecraftServer> getScheduledEvents() {
        return mainProperties.getScheduledEvents();
    }

    @Override
    public int getWanderingTraderSpawnDelay() {
        return wanderingTraderSpawnDelay;
    }

    @Override
    public void setWanderingTraderSpawnDelay(int delay) {
        this.wanderingTraderSpawnDelay = delay;
    }

    public int getWanderingTraderSpawnChance() {
        return wanderingTraderSpawnChance;
    }

    public void setWanderingTraderSpawnChance(int chance) {
        this.wanderingTraderSpawnChance = chance;
    }

    @Override
    @org.jspecify.annotations.Nullable
    public UUID getWanderingTraderId() {
        return wanderingTraderId;
    }

    @Override
    public void setWanderingTraderId(UUID uuid) {
        this.wanderingTraderId = uuid;
    }
}