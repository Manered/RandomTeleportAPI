package dev.continuum.rtpapi;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * The main API class for handling random teleportation.
 * <p></p>
 * <b>USAGE</b>:
 * <pre>
 * {@code
 * final RandomTeleportAPI api = RandomTeleportAPI.apiOrThrow();
 * final RTPRequirements requirements = api.requirements(b -> b.require(Biome.DESERT).require(-1000, 1000, -1000, 1000));
 * final World world = Bukkit.getWorld("world");
 * final Player player = Bukkit.getPlayer("Manere_");
 *
 * player.sendRichMessage("<gray>Teleporting...");
 *
 * final long time = api.time(api.teleport(api.location(world, requirements, true), player)
 *   .whenComplete((unused, throwable) -> {
 *     player.sendRichMessage("<green>Teleported!");
 *     player.setGameMode(GameMode.SURVIVAL);
 *     player.setHealth(20.0);
 *   })
 * );
 *
 * player.sendRichMessage("Took " + DurationFormatUtils.formatDurationHMS(time));
 * }</pre>
 */
public abstract class RandomTeleportAPI {

    /**
     * Retrieves an optional instance of {@link RandomTeleportAPI}.
     *
     * @return An {@link Optional} containing the {@link RandomTeleportAPI} instance if available.
     */
    @NotNull
    public static Optional<RandomTeleportAPI> api() {
        return Optional.ofNullable(find());
    }

    /**
     * Retrieves the plugin that provides the {@link RandomTeleportAPI}.
     *
     * @return The plugin providing the API, or {@code null} if not found.
     */
    @Nullable
    public static Plugin plugin() {
        final RegisteredServiceProvider<RandomTeleportAPI> provider = Bukkit.getServicesManager().getRegistration(RandomTeleportAPI.class);
        if (provider == null) return null;

        return provider.getPlugin();
    }

    /**
     * Finds the {@link RandomTeleportAPI} instance.
     *
     * @return The {@link RandomTeleportAPI} instance, or {@code null} if not found.
     */
    @Nullable
    public static RandomTeleportAPI find() {
        final RegisteredServiceProvider<RandomTeleportAPI> provider = Bukkit.getServicesManager().getRegistration(RandomTeleportAPI.class);

        if (provider == null) return null;

        return provider.getProvider();
    }

    /**
     * Executes a consumer if the {@link RandomTeleportAPI} is available.
     *
     * @param execute The consumer to execute.
     */
    public static void execute(final @NotNull Consumer<RandomTeleportAPI> execute) {
        api().ifPresent(execute);
    }

    /**
     * Retrieves the {@link RandomTeleportAPI} instance or throws an exception if not found.
     *
     * @return The {@link RandomTeleportAPI} instance.
     * @throws NullPointerException if the API instance is not found.
     */
    @NotNull
    public static RandomTeleportAPI apiOrThrow() {
        return apiOrThrow(() -> new NullPointerException("Couldn't get instance of RandomTeleportAPI"));
    }

    /**
     * Retrieves the {@link RandomTeleportAPI} instance or throws a supplied exception if not found.
     *
     * @param <E> The type of exception to be thrown.
     * @param supplier The exception supplier.
     * @return The {@link RandomTeleportAPI} instance.
     * @throws RuntimeException if the API instance is not found.
     */
    @NotNull
    public static <E extends Exception> RandomTeleportAPI apiOrThrow(final @NotNull Supplier<E> supplier) {
        final RandomTeleportAPI api = find();

        if (api == null) {
            throw new RuntimeException(supplier.get());
        }

        return api;
    }

    /**
     * Retrieves the {@link RandomTeleportAPI} instance or runs a {@link Runnable} and throws an exception if not found.
     *
     * @param runnable The {@link Runnable} to execute if the API instance is not found.
     * @return The {@link RandomTeleportAPI} instance.
     * @throws RuntimeException if the API instance is not found.
     */
    @NotNull
    public static RandomTeleportAPI apiOr(final @NotNull Runnable runnable) {
        final RandomTeleportAPI api = find();

        if (api == null) {
            runnable.run();
            throw new RuntimeException();
        }

        return api;
    }

    /**
     * Executes a supplier asynchronously.
     *
     * @param <V> The type of the value supplied.
     * @param supplier The supplier to execute.
     * @return A {@link CompletableFuture} representing the asynchronous computation.
     */
    @NotNull
    @CanIgnoreReturnValue
    private <V> CompletableFuture<V> async(final @NotNull Supplier<V> supplier) {
        return CompletableFuture.supplyAsync(supplier, runnable -> {
            final Plugin plugin = plugin();
            if (plugin == null) {
                CompletableFuture.runAsync(runnable);
                return;
            }

            Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
        });
    }

    /**
     * Executes a runnable asynchronously.
     *
     * @param runnable The runnable to execute.
     * @return A {@link CompletableFuture} representing the asynchronous computation.
     */
    @NotNull
    @CanIgnoreReturnValue
    private CompletableFuture<Void> async(final @NotNull Runnable runnable) {
        return CompletableFuture.runAsync(runnable, $ -> {
            final Plugin plugin = plugin();
            if (plugin == null) {
                CompletableFuture.runAsync(runnable);
                return;
            }

            Bukkit.getScheduler().runTaskAsynchronously(plugin, $);
        });
    }

    /**
     * Generates a random integer within the specified range.
     *
     * @param minimum The minimum value (inclusive).
     * @param maximum The maximum value (exclusive if inclusive is false).
     * @param inclusive Whether the maximum value is inclusive.
     * @return The generated random integer.
     */
    public int random(final int minimum, final int maximum, final boolean inclusive) {
        try {
            return async(() -> !inclusive ? ThreadLocalRandom.current().nextInt(minimum, maximum) : ThreadLocalRandom.current().nextInt(minimum - 1, maximum + 1)).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Generates a random integer within the specified range, inclusive of the maximum value.
     *
     * @param minimum The minimum value (inclusive).
     * @param maximum The maximum value (inclusive).
     * @return The generated random integer.
     */
    public int random(final int minimum, final int maximum) {
        return random(minimum, maximum, true);
    }

    /**
     * Teleports players to the specified location.
     *
     * @param location The location to teleport the players to.
     * @param players The players to teleport.
     * @return A {@link CompletableFuture} representing the asynchronous teleportation operation.
     */
    @NotNull
    @CanIgnoreReturnValue
    public abstract CompletableFuture<Void> teleport(final @NotNull Location location, final @NotNull Player @NotNull ... players);

    /**
     * Teleports players to a future location.
     *
     * @param future The future location to teleport the players to.
     * @param players The players to teleport.
     * @return A {@link CompletableFuture} representing the asynchronous teleportation operation.
     */
    @NotNull
    @CanIgnoreReturnValue
    public CompletableFuture<Void> teleport(final @NotNull CompletableFuture<Location> future, final @NotNull Player @NotNull ... players) {
        return future.thenCompose(location -> teleport(location, players));
    }

    /**
     * Measures the time taken for a future computation.
     *
     * @param <V> The type of the future result.
     * @param future The future computation.
     * @return The time taken for the computation in milliseconds.
     */
    public <V> long time(final CompletableFuture<V> future) {
        final long startTime = System.currentTimeMillis();

        try {
            return future.thenApply((v) -> {
                final long endTime = System.currentTimeMillis();
                return endTime - startTime;
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Generates a random location in the specified world based on given requirements.
     *
     * @param world The world to generate the location in.
     * @param requirements The requirements for the location.
     * @return A {@link CompletableFuture} representing the asynchronous location generation.
     */
    @NotNull
    @CanIgnoreReturnValue
    public CompletableFuture<Location> location(final @NotNull World world, final @NotNull Consumer<RTPRequirements> requirements) {
        return location(world, requirements(requirements));
    }

    /**
     * Generates a random location in the specified world based on given requirements.
     *
     * @param world The world to generate the location in.
     * @param requirements The requirements for the location.
     * @return A {@link CompletableFuture} representing the asynchronous location generation.
     */
    @NotNull
    @CanIgnoreReturnValue
    public CompletableFuture<Location> location(final @NotNull World world, final @NotNull RTPRequirements requirements) {
        return location(world, requirements, false);
    }

    /**
     * Generates a random location in the specified world based on given requirements.
     *
     * @param world The world to generate the location in.
     * @param requirements The requirements for the location.
     * @param inclusive Whether the boundaries are inclusive.
     * @return A {@link CompletableFuture} representing the asynchronous location generation.
     */
    @NotNull
    @CanIgnoreReturnValue
    public CompletableFuture<Location> location(final @NotNull World world, final @NotNull Consumer<RTPRequirements> requirements, final boolean inclusive) {
        return location(world, requirements(requirements), inclusive);
    }

    /**
     * Generates a random location in the specified world.
     *
     * @param world The world to generate the location in.
     * @return A {@link CompletableFuture} representing the asynchronous location generation.
     */
    @NotNull
    @CanIgnoreReturnValue
    public CompletableFuture<Location> location(final @NotNull World world) {
        return location(world, false);
    }

    /**
     * Generates a random location in the specified world.
     *
     * @param world The world to generate the location in.
     * @param inclusive Whether the boundaries are inclusive.
     * @return A {@link CompletableFuture} representing the asynchronous location generation.
     */
    @NotNull
    @CanIgnoreReturnValue
    public CompletableFuture<Location> location(final @NotNull World world, final boolean inclusive) {
        return location(world, requirements(requirements -> {
            requirements.require(Biome.DESERT);
            requirements.require(Coordinate.X, -10000, 10000);
            requirements.require(Coordinate.Z, -10000, 10000);
        }), false);
    }

    /**
     * Generates a random location in the specified world based on given requirements.
     *
     * @param world The world to generate the location in.
     * @param requirements The requirements for the location.
     * @param inclusive Whether the boundaries are inclusive.
     * @return A {@link CompletableFuture} representing the asynchronous location generation.
     */
    @NotNull
    @CanIgnoreReturnValue
    public abstract CompletableFuture<Location> location(final @NotNull World world, final @NotNull RTPRequirements requirements, final boolean inclusive);

    /**
     * Creates a new {@link RTPRequirements} instance and applies the given consumer to it.
     *
     * @param consumer The consumer to apply to the requirements.
     * @return The created {@link RTPRequirements} instance.
     */
    @NotNull
    @CanIgnoreReturnValue
    public RTPRequirements requirements(final @NotNull Consumer<RTPRequirements> consumer) {
        final RTPRequirements requirements = new RTPRequirements();
        consumer.accept(requirements);

        return requirements;
    }

    /**
     * Creates a new {@link Location} instance.
     *
     * @param world The world for the location.
     * @param x The X coordinate.
     * @param y The Y coordinate.
     * @param z The Z coordinate.
     * @return The created {@link Location} instance.
     */
    @NotNull
    private Location location(final @NotNull World world, final int x, final int y, final int z) {
        return new Location(world, x, y, z);
    }

    /**
     * Creates a new {@link Location} instance.
     *
     * @param world The world for the location.
     * @param x The X coordinate.
     * @param y The Y coordinate.
     * @param z The Z coordinate.
     * @param yaw The yaw angle.
     * @param pitch The pitch angle.
     * @return The created {@link Location} instance.
     */
    @NotNull
    private Location location(final @NotNull World world, final int x, final int y, final int z, float yaw, float pitch) {
        return new Location(world, x, y, z, yaw, pitch);
    }
}
