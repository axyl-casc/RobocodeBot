package infinite.mind;

import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;
import java.awt.Color;
import java.util.Random;


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
    private double getDistance(double p1x, double p2x, double p1y, double p2y) {
        return Math.sqrt((p2x - p1x) * (p2x - p1x) + (p2y - p1y) * (p2y - p1y));
    }
    // Called when a new round is started -> initialize and do some movement
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

        // https://docs.oracle.com/javase/8/docs/api/java/awt/Color.html
        setBodyColor(Color.cyan);
        setTurretColor(Color.darkGray);
        setRadarColor(Color.green);
        setBulletColor(Color.orange);
        setScanColor(Color.pink);

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

    // We saw another bot -> fire!
    @Override
    public void onScannedBot(ScannedBotEvent e) {
        locator.updateOnScan(e);
        opponentDistance = distanceTo(e.getX(), e.getY());
        if(opponentDistance < 10 && getGunHeat() == 0){
            fire(3);
        }else{
            if(locator.getCertainty() > 5 && getGunHeat() == 0){
                fire(Math.min(Math.round(locator.getCertainty() / 10.0) * 3, 3));
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
    private void printDebugInfo(){
            System.out.println("Debug Info:");
            System.out.println("  X: " + getX() + ", Y: " + getY());
            System.out.println("  Energy: " + getEnergy());
    }

}