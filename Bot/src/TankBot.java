import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;

// ------------------------------------------------------------------
// TankBot
// ------------------------------------------------------------------
// A sample bot original made for Robocode by Mathew Nelson.
// Ported to Robocode Tank Royale by Flemming N. Larsen.
//
// Probably the first bot you will learn about.
// Moves in a seesaw motion, and spins the gun around at each end.
// ------------------------------------------------------------------
public class TankBot extends Bot {

    /** Locator used to scan for enemies and handle firing */
    private final TargetLocator locator = new TargetLocator(this);

    // The main method starts our bot
    public static void main(String[] args) {
        new TankBot().start();
    }

    // Constructor, which loads the bot config file
    TankBot() {
        super(BotInfo.fromFile("TankBot.json"));
    }

    // Called when a new round is started -> initialize and do some movement
    @Override
    public void run() {
        // Calculate a reference position about 100px from the middle towards the top left
        double centerX = getArenaWidth() / 2.0;
        double centerY = getArenaHeight() / 2.0;
        double startX = centerX - 100;
        double startY = centerY - 100;

        // Move from the spawn point to the reference start position
        goTo(startX, startY);

        // Continuously move in a square of 100x100 pixels while searching for enemies
        while (isRunning()) {
            goTo(startX + 100, startY);       // Right
            locator.findTarget();
            goTo(startX + 100, startY + 100); // Down
            locator.findTarget();
            goTo(startX, startY + 100);       // Left
            locator.findTarget();
            goTo(startX, startY);             // Up (back to start)
            locator.findTarget();
        }
    }

    /**
     * Helper method to move the bot to an absolute coordinate.
     *
     * @param x target x coordinate
     * @param y target y coordinate
     */
    private void goTo(double x, double y) {
        double bearing = bearingTo(x, y);
        turnRight(bearing);
        forward(distanceTo(x, y));
    }

    // We saw another bot -> fire!
    @Override
    public void onScannedBot(ScannedBotEvent e) {
        locator.updateOnScan(e);
        locator.fireTarget(1.5);
    }

    // We were hit by a bullet -> turn perpendicular to the bullet
    @Override
    public void onHitByBullet(HitByBulletEvent e) {
        // Calculate the bearing to the direction of the bullet
        double bearing = calcBearing(e.getBullet().getDirection());

        // Turn 90 degrees to the bullet direction based on the bearing
        turnRight(90 - bearing);
    }
}
