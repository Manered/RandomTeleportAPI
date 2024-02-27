package dev.continuum.rtpapi;

import org.bukkit.block.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Represents a biome for random teleportation.
 */
@SuppressWarnings("unused")
public class RTPBiome {
    /**
     * The specific biome, can be null if any biome is allowed.
     */
    @Nullable
    private Biome biome;

    /**
     * Constructs an RTPBiome instance with a specific biome.
     *
     * @param biome The specific biome.
     */
    public RTPBiome(final @Nullable Biome biome) {
        this.biome = biome;
    }

    /**
     * Creates an RTPBiome instance with a specific biome.
     *
     * @param biome The specific biome.
     * @return An RTPBiome instance.
     */
    @NotNull
    public static RTPBiome ofBiome(final @NotNull Biome biome) {
        return new RTPBiome(biome);
    }

    /**
     * Creates an RTPBiome instance representing any biome.
     *
     * @return An RTPBiome instance representing any biome.
     */
    @NotNull
    public static RTPBiome ofAny() {
        return new RTPBiome(null);
    }

    /**
     * Generates a random biome.
     *
     * @return A randomly selected biome.
     */
    @NotNull
    public Biome generateAny() {
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final Biome[] biomes = Biome.values();

        final int randomInt = random.nextInt(0, biomes.length - 1);

        return biomes[randomInt];
    }

    /**
     * Gets the specific biome.
     *
     * @return The specific biome, or null if any biome is allowed.
     */
    @Nullable
    public Biome biome() {
        return biome;
    }

    /**
     * Sets the specific biome.
     *
     * @param biome The specific biome.
     */
    public void biome(final @Nullable Biome biome) {
        this.biome = biome;
    }

    /**
     * Checks if the biome is set to any biome.
     *
     * @return True if any biome is allowed, false otherwise.
     */
    public boolean isAny() {
        return biome == null;
    }
}
