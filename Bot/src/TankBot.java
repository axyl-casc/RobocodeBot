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
        // Move in a square pattern while searching for enemies
        while (isRunning()) {
            for (int i = 0; i < 4; i++) {
                forward(150);
                locator.findTarget();
                turnRight(90);
            }
        }
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
        var bearing = calcBearing(e.getBullet().getDirection());

        // Turn 90 degrees to the bullet direction based on the bearing
        turnRight(90 - bearing);
    }
}
