package infinite.mind;

import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;

/**
 * <p>Robocode API functions used in this file:</p>
 * <ul>
 *   <li>{@link Bot#turnGunRight(double)} &ndash; turns the bot's gun.</li>
 *   <li>{@link Bot#turnRight(double)} &ndash; used indirectly for body turning.</li>
 *   <li>{@link ScannedBotEvent} &ndash; event triggered when another bot is seen.</li>
 * </ul>
 */



// note: 90 is up
//      0 is right
//    270 is down
//   180 is left

/**
 * Utility class used for scanning and firing at opponents.
 */
public class TargetLocator {

    /**
     * Indicates if the next sweep should rotate the gun to the right.
     */
    private boolean sweepRight = false; // Start with left
    /**
     * Set to {@code true} when a target has been seen during the last scan.
     */
    private boolean targetFound = false;
    /**
     * Number of scans since the target was last observed.
     */
    private int scansSinceLastSeen = 10;

    /**
     * Accumulated turning amount from the bot's own movement.
     */
    private double turn_delta = 0;

    private double opponentEstimatedAngle = 0;

    private double currentGunDirection = 0;

    /**
     * Called when the bot moves forward. Increases the gun turn adjustment
     * to compensate for our own movement.
     *
     */
    public void moving(){
        System.out.println("Moving forward.");
    }

    /**
     * Notifies the locator that the bot is turning. Moving the tanks physical body will need to inform the turret to adjust accordingly to alwasy face the opponent
     *
     * @param angle double angle: amount the bot has turned
     */
    public void turning(double angle){
        System.out.println("Turning by " + angle + " degrees.");
        turn_delta += angle;
        opponentEstimatedAngle += angle;
        turn_delta = turn_delta % 360; // Normalize to [0, 360)
    }

    /**
     * Updates internal state when a target is scanned.
     *
     * @param e ScannedBotEvent e: the scan event with target information
     */
    public void updateOnScan(ScannedBotEvent e) {
        targetFound = true;
        scansSinceLastSeen = 0;
        turn_delta = 0;
        
    }

    /**
     * Performs a scan for enemy tanks and adjusts the gun position.
     *
     * @param bot Bot bot: reference used to issue turning commands
     * @return boolean indicating if a target was found
     */
    public boolean findTarget(Bot bot) {
        currentGunDirection = bot.getGunDirection();
        targetFound = false; // Reset for next scan
        // Perform gun movement based on current direction
        double turn_angle = Math.max((0.01 * (double) (scansSinceLastSeen * scansSinceLastSeen)), 1) + turn_delta;
        turn_angle = turn_angle % 360;
        System.out.println("Scanning for targets, scans since last seen: " + scansSinceLastSeen + ", turning " + (sweepRight ? "right" : "left") + " by " + turn_angle + " degrees.");
        if (sweepRight) {
            bot.turnGunRight(turn_angle);
        } else {
            bot.turnGunLeft(turn_angle);
        }

        // Update next sweep direction based on scan results and current movement
        if (targetFound) {
            System.out.println("Target spotted!");
            if(turn_angle < 10){
                opponentEstimatedAngle = bot.getGunDirection();
                System.out.println("Estimated opponent angle: " + opponentEstimatedAngle);
            }

        } else {
            scansSinceLastSeen++;
            // If no target was found after several scans, reverse direction
            if(scansSinceLastSeen == 1){
                System.out.println("No target found, reversing direction.");
                sweepRight = !sweepRight;
                turn_delta += 5;
            }
        }
        currentGunDirection = bot.getGunDirection();
        return targetFound;
    }

    /**
     * Provides a simple certainty metric based on how long ago a target was
     * spotted.
     *
     * @return int value from 0 to 10 representing certainty
     */
    public int getCertainty(double direction) {
        if(scansSinceLastSeen < 5){
            return (int) ((90 - Math.abs(currentGunDirection - opponentEstimatedAngle))/90) * 10;
        }
        return 0;
    }

}
