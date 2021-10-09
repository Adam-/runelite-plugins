package tictac7x.tithe;

import tictac7x.Overlay;
import java.time.Instant;
import java.time.Duration;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import net.runelite.api.GameObject;

public class TithePatch extends Overlay {
    // Tithe empty patch.
    protected static final int TITHE_EMPTY_PATCH = 27383;

    // Golovanova plants.
    protected static final int GOLOVANOVA_SEEDLING = 27384;
    protected static final int GOLOVANOVA_SEEDLING_WATERED = 27385;
    protected static final int GOLOVANOVA_SEEDLING_BLIGHTED = 27386;
    protected static final int GOLOVANOVA_PLANT_1 = 27387;
    protected static final int GOLOVANOVA_PLANT_1_WATERED = 27388;
    protected static final int GOLOVANOVA_PLANT_1_BLIGHTED = 27389;
    protected static final int GOLOVANOVA_PLANT_2 = 27390;
    protected static final int GOLOVANOVA_PLANT_2_WATERED = 27391;
    protected static final int GOLOVANOVA_PLANT_2_BLIGHTED = 27392;
    protected static final int GOLOVANOVA_PLANT_3 = 27393;
    protected static final int GOLOVANOVA_PLANT_3_BLIGHTED = 27394;

    // Bologano plants.
    protected static final int BOLOGANO_SEEDLING = 27395;
    protected static final int BOLOGANO_SEEDLING_WATERED = 27396;
    protected static final int BOLOGANO_SEEDLING_BLIGHTED = 27397;
    protected static final int BOLOGANO_PLANT_1 = 27398;
    protected static final int BOLOGANO_PLANT_1_WATERED = 27399;
    protected static final int BOLOGANO_PLANT_1_BLIGHTED = 27400;
    protected static final int BOLOGANO_PLANT_2 = 27401;
    protected static final int BOLOGANO_PLANT_2_WATERED = 27402;
    protected static final int BOLOGANO_PLANT_2_BLIGHTED = 27403;
    protected static final int BOLOGANO_PLANT_3 = 27404;
    protected static final int BOLOGANO_PLANT_3_BLIGHTED = 27405;

    // Logavano plants.
    protected static final int LOGAVANO_SEEDLING = 27406;
    protected static final int LOGAVANO_SEEDLING_WATERED = 27407;
    protected static final int LOGAVANO_SEEDLING_BLIGHTED = 27408;
    protected static final int LOGAVANO_PLANT_1 = 27409;
    protected static final int LOGAVANO_PLANT_1_WATERED = 27410;
    protected static final int LOGAVANO_PLANT_1_BLIGHTED = 27411;
    protected static final int LOGAVANO_PLANT_2 = 27412;
    protected static final int LOGAVANO_PLANT_2_WATERED = 27413;
    protected static final int LOGAVANO_PLANT_2_BLIGHTED = 27414;
    protected static final int LOGAVANO_PLANT_3 = 27415;
    protected static final int LOGAVANO_PLANT_3_BLIGHTED = 27416;

    private final TitheConfig config;
    private GameObject state;
    private final Duration CYCLE_DURATION = Duration.ofMinutes(1);
    private Instant cycle;

    public TithePatch(final GameObject seedling, final TitheConfig config) {
        this.config = config;
        setPatchState(seedling);
    }

    public void setPatchState(final GameObject patch) {
        // Set state and reset cycle.
        if (isSeedling(patch) || isDry(patch) || isBlighted(patch) || isGrown(patch)) {
            this.state = patch;
            this.cycle = Instant.now();

        // Set state, but keep the cycle.
        } else if (isWatered(patch)) {
            this.state = patch;
        }
    }

    @Override
    public Dimension render(final Graphics2D graphics) {
        final Color color = getCycleColor();
        if (color != null) renderPie(graphics, state, getCycleColor(), (float) getCycleProgress());
        return null;
    }

    private Color getCycleColor() {
        if (config.highlightPlantsDry() && isDry(state)) {
            return config.getPlantsDryColor();
        } else if (config.highlightPlantsGrown() && isGrown(state)) {
            return config.getPlantsGrownColor();
        } else if (config.highlightPlantsWatered() && isWatered(state)) {
            return config.getPlantsWateredColor();
        } else if (config.highlightPlantsBlighted() && isBlighted(state)) {
            return config.getPlantsBlightedColor();
        }

        return null;
    }

    private double getCycleProgress() {
        final Duration duration = Duration.between(cycle, Instant.now());
        return 1 - (duration.compareTo(CYCLE_DURATION) < 0 ? getCycleDuration() / CYCLE_DURATION.toMillis() : 1);
    }

    protected double getCycleDuration() {
        if (cycle != null) {
            final Duration duration = Duration.between(cycle, Instant.now());
            return duration.toMillis();
        }

        return 0;
    }

    protected static boolean isSeedling(final GameObject patch) {
        final int id = patch.getId();
        return id == GOLOVANOVA_SEEDLING || id == BOLOGANO_SEEDLING || id == LOGAVANO_SEEDLING;
    }

    protected static boolean isDry(final GameObject patch) {
        final int id = patch.getId();
        return (
            id == GOLOVANOVA_SEEDLING
            || id == GOLOVANOVA_PLANT_1
            || id == GOLOVANOVA_PLANT_2
            || id == BOLOGANO_SEEDLING
            || id == BOLOGANO_PLANT_1
            || id == BOLOGANO_PLANT_2
            || id == LOGAVANO_SEEDLING
            || id == LOGAVANO_PLANT_1
            || id == LOGAVANO_PLANT_2
        );
    }

    protected static boolean isWatered(final GameObject patch) {
        final int id = patch.getId();
        return (
            id == GOLOVANOVA_SEEDLING_WATERED
            || id == GOLOVANOVA_PLANT_1_WATERED
            || id == GOLOVANOVA_PLANT_2_WATERED
            || id == BOLOGANO_SEEDLING_WATERED
            || id == BOLOGANO_PLANT_1_WATERED
            || id == BOLOGANO_PLANT_2_WATERED
            || id == LOGAVANO_SEEDLING_WATERED
            || id == LOGAVANO_PLANT_1_WATERED
            || id == LOGAVANO_PLANT_2_WATERED
        );
    }

    protected static boolean isGrown(final GameObject patch) {
        final int id = patch.getId();
        return (
            id == GOLOVANOVA_PLANT_3
            || id == BOLOGANO_PLANT_3
            || id == LOGAVANO_PLANT_3
        );
    }

    protected static boolean isBlighted(final GameObject patch) {
        final int id = patch.getId();
        return (
            id == GOLOVANOVA_SEEDLING_BLIGHTED
            || id == GOLOVANOVA_PLANT_1_BLIGHTED
            || id == GOLOVANOVA_PLANT_2_BLIGHTED
            || id == GOLOVANOVA_PLANT_3_BLIGHTED
            || id == BOLOGANO_SEEDLING_BLIGHTED
            || id == BOLOGANO_PLANT_1_BLIGHTED
            || id == BOLOGANO_PLANT_2_BLIGHTED
            || id == BOLOGANO_PLANT_3_BLIGHTED
            || id == LOGAVANO_SEEDLING_BLIGHTED
            || id == LOGAVANO_PLANT_1_BLIGHTED
            || id == LOGAVANO_PLANT_2_BLIGHTED
            || id == LOGAVANO_PLANT_3_BLIGHTED
        );
    }

    protected static boolean isEmptyPatch(final GameObject patch) {
        return patch.getId() == TITHE_EMPTY_PATCH;
    }

    protected static boolean isPatch(final GameObject patch) {
        return isDry(patch) || isWatered(patch) || isGrown(patch) || isBlighted(patch) || isEmptyPatch(patch);
    }
}
