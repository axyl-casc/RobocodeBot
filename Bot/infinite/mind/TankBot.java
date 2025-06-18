package infinite.mind;

import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;
import java.awt.Color;

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

/**
 * Basic tank bot demonstrating simple movement and firing logic.
 */
public class TankBot extends Bot {

    /**
     * Distance to the last scanned opponent. Used to decide fire power.
     */
    private double opponentDistance = 0;

    /**
     * Entry point for the program. Creates and starts the bot.
     *
     * @param args String[] args: command line arguments (unused)
     */
    public static void main(String[] args) {
        new TankBot().start();
    }

    /**
     * Constructs a new {@code TankBot} and loads its configuration.
     *
     */
    TankBot() {
        super(BotInfo.fromFile("TankBot.json"));
    }

    /**
     * Called by the game engine when a new round starts. Initializes colors,
     * prints debug info and continuously scans for enemies.
     *
     */

    @Override
    public void run() {
        double arenaWidth = getArenaWidth();
        double arenaHeight = getArenaHeight();
        double centerX = arenaWidth / 2.0;
        double centerY = arenaHeight / 2.0;

        // Set colors
        setBodyColor(Color.cyan);
        setTurretColor(Color.darkGray);
        setRadarColor(Color.green);
        setBulletColor(Color.orange);
        setScanColor(Color.pink);

        while (isRunning()) {
            System.out.println("Distance to center: " + getDistance(getX(), getY(), centerX, centerY));
            // move near the center of the arena if we are too far away
            if (getDistance(getX(), getY(), centerX, centerY) > Math.min(arenaWidth, arenaHeight) / 2.0) {
                if (getX() > centerX) {
                    moveLeft(getX() - centerX);
                } else {
                    moveRight(centerX - getX());
                }
                if (getY() > centerY) {
                    moveDown(getY() - centerY);
                } else {
                    moveUp(centerY - getY());
                }
            }

            printDebugInfo();
        }
    }

    private double getDistance(double p1x, double p2x, double p1y, double p2y) {
        return Math.sqrt((p2x - p1x) * (p2x - p1x) + (p2y - p1y) * (p2y - p1y));
    }

    private void moveUp(double distance) { // 90°
        double direction = getDirection();
        if (direction > 90) {
            turnRight(direction - 90);
        } else {
            turnLeft(90 - direction);
        }
        forward(distance);
    }

    private void moveLeft(double distance) { // 180°
        double direction = getDirection();

        if (direction < 180) {
            turnRight(direction - 180);
        } else {
            turnLeft(180 - direction);
        }

        forward(distance);
    }

    private void moveDown(double distance) { // 270°
        double direction = getDirection();
        if (direction < 270) {
            turnRight(direction - 270);
        } else {
            turnLeft(270 - direction);
        }

        forward(distance);
    }

    private void moveRight(double distance) {
        double direction = getDirection();
        // if we want to move right, we want the angle to be set at 0
        turnRight(direction);
        forward(distance);
    }

    /**
     * Fired when another bot is scanned by the radar. Updates target
     * information and handles shooting decisions.
     *
     * @param e ScannedBotEvent e: event describing the scanned bot
     */
    @Override
    public void onScannedBot(ScannedBotEvent e) {

    }

    /**
     * Invoked when the bot is hit by a bullet. Turns perpendicular to the
     * incoming bullet and moves away.
     *
     * @param e HitByBulletEvent e: details about the bullet that hit us
     */
    @Override
    public void onHitByBullet(HitByBulletEvent e) {
        // Calculate the bearing to the direction of the bullet
        double bearing = calcBearing(e.getBullet().getDirection());

    }

    /**
     * Outputs the bot's current position and energy to the console.
     *
     */
    private void printDebugInfo() {
        System.out.println("Debug Info:");
        System.out.println("  X: " + getX() + ", Y: " + getY());
        System.out.println("  Energy: " + getEnergy());
        System.out.println("  Opponent Distance: " + opponentDistance);
        System.out.println("  Gun Heat: " + getGunHeat());
        System.out.println("  Facing Direction: " + getDirection());
    }

}
