import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;

/**
 * Utility class used for scanning and firing at opponents.
 */
public class TargetLocator {

    private boolean sweepRight = false; // Start with left
    private boolean targetFound = false;
    private int scansSinceLastSeen = 10;

    private double turn_delta = 0;

    public void moving(){
        System.out.println("Moving forward.");
        turn_delta += 2.5;
    }

    public void turning(double angle){
        System.out.println("Turning by " + angle + " degrees.");
        turn_delta += angle;
    }

    public void updateOnScan(ScannedBotEvent e) {

        targetFound = true;
        scansSinceLastSeen = 0;
        turn_delta = 0;
        
    }

    public boolean findTarget(Bot bot) {
        targetFound = false; // Reset for next scan
        // Perform gun movement based on current direction
        double turn_angle = Math.max(0.1 * scansSinceLastSeen*scansSinceLastSeen, 1) + turn_delta;
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

    public int getCertainty() {

        return Math.max(0, 10 - scansSinceLastSeen);
    }

}
