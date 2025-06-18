import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;
import java.awt.Color;
import java.util.Random;

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
    private final TargetLocator locator = new TargetLocator();

    private double opponentDistance = 0;

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
        double tankXPosition = getX();
        double tankYPosition = getY();
        System.out.println("TankBot started at position: (" + tankXPosition + ", " + tankYPosition + ")");

        // https://docs.oracle.com/javase/8/docs/api/java/awt/Color.html
        setBodyColor(Color.cyan);
        setTurretColor(Color.darkGray);
        setRadarColor(Color.green);
        setBulletColor(Color.orange);
        setScanColor(Color.pink);

        while (isRunning()) {
            printDebugInfo();
            locator.findTarget(this);

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
        opponentDistance = distanceTo(e.getX(), e.getY());
        if(opponentDistance < 10 && getGunHeat() == 0){
            fire(3);
        }else{
            if(locator.getCertainty() > 5 && getGunHeat() <= 1){
                fire(Math.min(Math.round(locator.getCertainty() / 10.0) * 4, 3));
            }
        }

    }

    // We were hit by a bullet -> turn perpendicular to the bullet
    @Override
    public void onHitByBullet(HitByBulletEvent e) {
        // Calculate the bearing to the direction of the bullet
        double bearing = calcBearing(e.getBullet().getDirection());

        // Turn 90 degrees to the bullet direction based on the bearing
        turnRight(bearing + 91);

        // let the locator know we are moving
        locator.turning(bearing + 91);
        locator.moving();
        forward(50);
    }

    private void printDebugInfo(){
            System.out.println("Debug Info:");
            System.out.println("  X: " + getX() + ", Y: " + getY());
            System.out.println("  Energy: " + getEnergy());
    }

}
