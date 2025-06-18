import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;
import java.awt.Color;
import java.util.Random;

/**
 * <p>Robocode API functions used in this file:</p>
 * <ul>
 *   <li>{@link Bot#start()} &ndash; begins execution of the bot.</li>
 *   <li>{@link Bot#getX()} / {@link Bot#getY()} &ndash; retrieve the current coordinates.</li>
 *   <li>{@link Bot#setBodyColor(Color)} &ndash; sets the body color of the tank.</li>
 *   <li>{@link Bot#setTurretColor(Color)} &ndash; sets the turret color.</li>
 *   <li>{@link Bot#setRadarColor(Color)} &ndash; sets the radar color.</li>
 *   <li>{@link Bot#setBulletColor(Color)} &ndash; sets the bullet color.</li>
 *   <li>{@link Bot#setScanColor(Color)} &ndash; sets the color of the radar arc.</li>
 *   <li>{@link Bot#isRunning()} &ndash; checks whether the round is still in progress.</li>
 *   <li>{@link Bot#bearingTo(double,double)} &ndash; calculates the bearing to a point.</li>
 *   <li>{@link Bot#turnRight(double)} &ndash; turns the bot body right.</li>
 *   <li>{@link Bot#forward(double)} &ndash; moves the bot forward.</li>
 *   <li>{@link Bot#fire(double)} &ndash; fires the gun with the specified power.</li>
 *   <li>{@link Bot#distanceTo(double,double)} &ndash; computes distance to a point.</li>
 *   <li>{@link Bot#getGunHeat()} &ndash; obtains the current gun heat.</li>
 *   <li>{@link Bot#calcBearing(double)} &ndash; converts a heading to a bearing.</li>
 * </ul>
 */

// ------------------------------------------------------------------
// TankBot
// ------------------------------------------------------------------
// A sample bot originally made for Robocode by Mathew Nelson.
// Ported to Robocode Tank Royale by Flemming N. Larsen.
//
// Probably the first bot you will learn about.
// Moves in a seesaw motion, and spins the gun around at each end.
// ------------------------------------------------------------------

/**
 * Basic tank bot demonstrating simple movement and firing logic.
 */
public class TankBot extends Bot {

    /**
     * Locator used to scan for enemies and handle firing logic.
     */
    private final TargetLocator locator = new TargetLocator();

    /**
     * Distance to the last scanned opponent. Used to decide fire power.
     */
    private double opponentDistance = 0;

    /**
     * Entry point for the program. Creates and starts the bot.
     *
     * @param args String[] args: command line arguments (unused)
     * @return void
     */
    public static void main(String[] args) {
        new TankBot().start();
    }

    /**
     * Constructs a new {@code TankBot} and loads its configuration.
     *
     * @return void
     */
    TankBot() {
        super(BotInfo.fromFile("TankBot.json"));
    }

    /**
     * Called by the game engine when a new round starts. Initializes colors,
     * prints debug info and continuously scans for enemies.
     *
     * @return void
     */
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
     * @param x double x: target x coordinate
     * @param y double y: target y coordinate
     * @return void
     */
    private void goTo(double x, double y) {
        double bearing = bearingTo(x, y);
        turnRight(bearing);
        forward(distanceTo(x, y));
    }

    /**
     * Fired when another bot is scanned by the radar. Updates target
     * information and handles shooting decisions.
     *
     * @param e ScannedBotEvent e: event describing the scanned bot
     * @return void
     */
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

    /**
     * Invoked when the bot is hit by a bullet. Turns perpendicular to the
     * incoming bullet and moves away.
     *
     * @param e HitByBulletEvent e: details about the bullet that hit us
     * @return void
     */
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

    /**
     * Outputs the bot's current position and energy to the console.
     *
     * @return void
     */
    private void printDebugInfo(){
            System.out.println("Debug Info:");
            System.out.println("  X: " + getX() + ", Y: " + getY());
            System.out.println("  Energy: " + getEnergy());
    }

}
