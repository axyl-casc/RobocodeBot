package infinite.mind;

import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;
import dev.robocode.tankroyale.botapi.graphics.Color;
import java.net.URI;
import javax.swing.JOptionPane;

/**
 * Example bot demonstrating basic movement and target handling using the
 * Tank Royale Bot API.
 */
public class TankBot extends Bot {

    private static final String DEFAULT_URL = "ws://localhost:7654";
    private static final String DEFAULT_SECRET = "Zur2Fpt1ExRc5G3WSO/8oM574f/pmEbZ22bqXHlm4/";

    /** Locator used to scan for enemies and handle firing */
    private final TargetLocator locator = new TargetLocator();

    private double opponentDistance = 0;

    /**
     * Entry point that launches the bot.
     *
     * @param args command line arguments (unused)
     */
    public static void main(String[] args) {
        new TankBot().start();
    }

    /**
     * Constructs the bot and loads configuration from {@code TankBot.json}.
     */
    TankBot() {
        super(BotInfo.fromFile("TankBot.json"), URI.create(DEFAULT_URL), DEFAULT_SECRET);
    }

    TankBot(String serverUrl, String serverSecret) {
        super(BotInfo.fromFile("TankBot.json"), URI.create(serverUrl), serverSecret);
    }

    /**
     * Utility method used to calculate the Euclidean distance between two points.
     */
    private double getDistance(double p1x, double p2x, double p1y, double p2y) {
        return Math.sqrt((p2x - p1x) * (p2x - p1x) + (p2y - p1y) * (p2y - p1y));
    }

    /**
     * Called when a new round is started. Initializes colors and repeatedly
     * searches for opponents while patrolling the arena center.
     */
    @Override
    public void run() {
        // Move in a square pattern while searching for enemies
        double tankXPosition = getX();
        double tankYPosition = getY();
        double arenaWidth = getArenaWidth();
        double arenaHeight = getArenaHeight();
        double centerX = arenaWidth / 2.0;
        double centerY = arenaHeight / 2.0;
        System.out.println("TankBot started at position: (" + tankXPosition + ", " + tankYPosition + ")");

        // https://robocode-dev.github.io/tank-royale/api/java/dev/robocode/tankroyale/botapi/graphics/Color.html
        setBodyColor(Color.ORANGE_RED);
        setTurretColor(Color.GAINSBORO);
        setRadarColor(Color.GREEN_YELLOW);
        setBulletColor(Color.AQUA);
        setScanColor(Color.PINK);

        while (isRunning()) {

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
            locator.findTarget(this);
        }
    }

    /**
     * Fired when another bot is scanned. Updates targeting and fires based on
     * distance and certainty.
     */
    @Override
    public void onScannedBot(ScannedBotEvent e) {
        locator.updateOnScan(e);
        opponentDistance = distanceTo(e.getX(), e.getY());
        if (opponentDistance < 10 && getGunHeat() == 0) {
            fire(3);
        } else {
            if (locator.getCertainty() > 5 && getGunHeat() == 0) {
                if(opponentDistance < 100){
                    fire(1);
                }else{
                    fire(Math.min(Math.round(locator.getCertainty() / 10.0) * 3, 3));
                }
            }
        }

    }

    /**
     * Called when the bot is hit by a bullet. Turns perpendicular to the
     * incoming fire and moves ahead to evade further shots.
     */
    @Override
    public void onHitByBullet(HitByBulletEvent e) {
        // Calculate the bearing to the direction of the bullet
        double bearing = calcBearing(e.getBullet().getDirection());

        // Turn 90 degrees to the bullet direction based on thev bearing
        setTurnRight(bearing + 91);

        // let the locator know we are moving
        locator.turning(bearing + 91);
        locator.moving();
        forward(50);
    }

    /**
     * Moves the bot upward (north). Adjusts orientation before moving.
     */
    private void moveUp(double distance) { // 90°
        double direction = getDirection();
        if (direction > 90) {
            turnRight(direction - 90);
        } else {
            turnLeft(90 - direction);
        }
        forward(distance);
    }

    /**
     * Moves the bot left (west). Adjusts orientation before moving.
     */
    private void moveLeft(double distance) { // 180°
        double direction = getDirection();

        if (direction < 180) {
            turnRight(direction - 180);
        } else {
            turnLeft(180 - direction);
        }

        forward(distance);
    }

    /**
     * Moves the bot down (south). Adjusts orientation before moving.
     */
    private void moveDown(double distance) { // 270°
        double direction = getDirection();
        if (direction < 270) {
            turnRight(direction - 270);
        } else {
            turnLeft(270 - direction);
        }

        forward(distance);
    }

    /**
     * Moves the bot right (east) without adjusting orientation.
     */
    private void moveRight(double distance) {
        double direction = getDirection();
        // if we want to move right, we want the angle to be set at 0
        turnRight(direction);
        forward(distance);
    }

    /**
     * Print simple debug information about the bot state.
     */
    private void printDebugInfo() {
        System.out.println("Debug Info:");
        System.out.println("  X: " + getX() + ", Y: " + getY());
        System.out.println("  Energy: " + getEnergy());
    }

}