package dev.continuum.rtpapi;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * The main API class for handling random teleportation.
 */
@SuppressWarnings("unused")
public abstract class RandomTeleportAPI {
    /**
     * Retrieves an optional instance of the RandomTeleportAPI.
     *
     * @return An optional containing the RandomTeleportAPI instance if found.
     */
    @NotNull
    public static Optional<RandomTeleportAPI> api() {
        return Optional.ofNullable(find());
    }

    /**
     * Finds the RandomTeleportAPI instance.
     *
     * @return The RandomTeleportAPI instance, or null if not found.
     */
    @Nullable
    public static RandomTeleportAPI find() {
        final RegisteredServiceProvider<RandomTeleportAPI> provider = Bukkit
            .getServicesManager().getRegistration(
                RandomTeleportAPI.class
            );

        if (provider == null) return null;

        return provider.getProvider();
    }


    /**
     * Teleports players to the specified location.
     *
     * @param location The destination location.
     * @param players  The players to teleport.
     */
    public abstract void teleport(final @NotNull Location location, final @NotNull Player @NotNull ... players);

    /**
     * Teleports a single player to the specified location.
     *
     * @param location The destination location.
     * @param player   The player to teleport.
     */
    public abstract void teleport(final @NotNull Location location, final @NotNull Player player);

    /**
     * Performs a timed teleport of players to the specified location.
     *
     * @param location The destination location.
     * @param players  The players to teleport.
     * @return The time in milliseconds when the teleportation will occur.
     */
    public abstract long timedTeleport(final @NotNull Location location, final @NotNull Player @NotNull ... players);

    /**
     * Searches for a suitable teleportation location based on the world and biome.
     *
     * @param world The target world.
     * @param biome The target biome.
     * @return A CompletableFuture containing the teleportation location.
     */
    @NotNull
    public abstract CompletableFuture<Location> search(final @NotNull World world, final @NotNull RTPBiome biome);

    /**
     * Searches for a suitable teleportation location based on the world with any biome.
     *
     * @param world The target world.
     * @return A CompletableFuture containing the teleportation location.
     */
    @NotNull
    public CompletableFuture<Location> search(final @NotNull World world) {
        return search(world, RTPBiome.ofAny());
    }
}
