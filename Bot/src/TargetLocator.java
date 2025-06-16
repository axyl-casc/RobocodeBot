import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;

/**
 * Utility class used for scanning and firing at opponents.
 */
public class TargetLocator {

    /** Reference to the hosting bot */
    private final Bot bot;

    /** Smoothed bearing to the last scanned target */
    private double smoothedBearing = 0.0;

    /** Exponential smoothing factor */
    private static final double SMOOTHING = 0.3;

    /** Direction of the next gun sweep, true is right */
    private boolean sweepRight = true;

    /**
     * Creates a new TargetLocator for the specified bot.
     *
     * @param bot The bot using this locator
     */
    public TargetLocator(Bot bot) {
        this.bot = bot;
    }

    /**
     * Updates internal state based on a scanned bot event.
     *
     * @param e The scanned bot event
     */
    public void updateOnScan(ScannedBotEvent e) {
        double bearing = bot.calcBearing(e.getDirection());
        smoothedBearing = SMOOTHING * bearing + (1 - SMOOTHING) * smoothedBearing;
        sweepRight = bearing >= 0;
    }

    /**
     * Sweep the gun to search for a target. The gun sweeps either to the left
     * or right depending on the last known target bearing.
     */
    public void findTarget() {
        if (sweepRight) {
            bot.turnGunRight(20);
        } else {
            bot.turnGunLeft(20);
        }
    }

    /**
     * Predicts the next target location using the smoothed bearing and fires
     * the gun with the specified bullet power.
     *
     * @param power Bullet power for the shot
     */
    public void fireTarget(double power) {
        double absDirection = bot.getDirection() + smoothedBearing;
        double gunBearing = bot.calcGunBearing(absDirection);
        bot.turnGunRight(gunBearing);
        bot.fire(power);
    }
}
