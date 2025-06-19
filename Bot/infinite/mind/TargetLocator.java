package infinite.mind;

import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;

/**
 * Utility class used for scanning the arena and aiming at opponents.
 * <p>
 * This helper is responsible for sweeping the gun, updating internal
 * tracking state when a target is detected, and exposing a certainty
 * level used by {@link TankBot} when deciding to fire.
 */
public class TargetLocator {

    private boolean sweepRight = false; // Start with left
    private boolean targetFound = false;
    private int scansSinceLastSeen = 10;

    private double turn_delta = 0;

    /**
     * Creates a new locator with default scanning parameters.
     */
    public TargetLocator() {
    }

    /**
     * Notify the locator that the bot moved forward. This increases the next
     * gun sweep slightly to help cover more area while moving.
     */
    public void moving() {
        System.out.println("Moving forward.");
        turn_delta += 2.5;
    }

    /**
     * Notify the locator that the bot turned by the specified angle.
     *
     * @param angle degrees the bot has turned
     */
    public void turning(double angle) {
        System.out.println("Turning by " + angle + " degrees.");
        turn_delta += angle;
    }

    /**
     * Called when a scan detects another bot. Resets sweep state and records
     * the detection.
     *
     * @param e event containing information about the scanned bot
     */
    public void updateOnScan(ScannedBotEvent e) {
        targetFound = true;
        scansSinceLastSeen = 0;
        turn_delta = 0;
    }

    /**
     * Sweep the gun looking for a target.
     *
     * @param bot reference to the controlling bot used for turning the gun
     * @return {@code true} if a target was found during the last scan
     */
    public boolean findTarget(Bot bot) {
        targetFound = false; // Reset for next scan
        // Perform gun movement based on current direction
        double turn_angle = Math.max(0.1 * scansSinceLastSeen * scansSinceLastSeen, 1) + turn_delta;
        System.out.println("Scanning for targets, scans since last seen: " + scansSinceLastSeen + ", turning " + (sweepRight ? "right" : "left") + " by " + turn_angle + " degrees.");
        if (sweepRight) {
            bot.turnGunRight(turn_angle);
        } else {
            bot.turnGunRight(-turn_angle);
        }

        // Update next sweep direction based on scan results and current movement
        if (targetFound) {
            System.out.println("Target spotted!");
            // Maintain current direction if target was found
            // (sweepRight remains unchanged)
        } else {
            scansSinceLastSeen++;
            // If no target was found after several scans, reverse direction
            if(scansSinceLastSeen == 1){
                System.out.println("No target found, reversing direction.");
                sweepRight = !sweepRight;
                turn_delta += 5;
            }
        }
        return targetFound;
    }

    /**
     * Returns how certain the locator is that an enemy is nearby.
     *
     * @return value between 0 and 10 where higher means more certain
     */
    public int getCertainty() {
        return Math.max(0, 10 - scansSinceLastSeen);
    }

}