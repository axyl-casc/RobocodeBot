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

    // --- Simple Q-learning state -----------------------------------------
    private java.util.Map<String, double[]> qTable = new java.util.HashMap<>();
    private String lastState = null;
    private int lastAction = -1;
    private double reward = 0;

    private static final int ACTION_FIRE = 0;
    private static final int ACTION_AHEAD = 1;
    private static final int ACTION_LEFT = 2;
    private static final int ACTION_RIGHT = 3;
    private static final int NUM_ACTIONS = 4;

    private final double alpha = 0.1;  // learning rate
    private final double gamma = 0.9;  // discount
    private final double epsilon = 0.1; // exploration

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
        // Set colors
        setBodyColor(Color.cyan);
        setTurretColor(Color.darkGray);
        setRadarColor(Color.green);
        setBulletColor(Color.orange);
        setScanColor(Color.pink);

        loadQTable();

        while (isRunning()) {
            turnRadarRight(360); // Continuously scan
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
        opponentDistance = distanceTo(e.getX(), e.getY());
        String state = getState(opponentDistance);

        updateQ(state);

        int action = selectAction(state);
        executeAction(action, e);

        lastState = state;
        lastAction = action;
    }

    /**
     * Invoked when the bot is hit by a bullet. Turns perpendicular to the
     * incoming bullet and moves away.
     *
     * @param e HitByBulletEvent e: details about the bullet that hit us
     */
    @Override
    public void onHitByBullet(HitByBulletEvent e) {
        // Penalize when hit and try to dodge
        reward -= 2;
        double bearing = calcBearing(e.getBullet().getDirection());
        turnRight(bearing > 0 ? 90 - bearing : -90 - bearing);
        forward(50);
    }

    @Override
    public void onBulletHit(BulletHitBotEvent e) {
        // Reward when hitting another bot
        reward += 3;
    }

    @Override
    public void onHitWall(HitWallEvent e) {
        // Penalize hitting walls
        reward -= 1;
        back(20);
    }

    @Override
    public void onRoundEnded(RoundEndedEvent e) {
        saveQTable();
    }

    private String getState(double distance) {
        if (distance < 100) {
            return "CLOSE";
        } else if (distance < 300) {
            return "MEDIUM";
        }
        return "FAR";
    }

    private int selectAction(String state) {
        double[] values = qTable.computeIfAbsent(state, k -> new double[NUM_ACTIONS]);
        if (Math.random() < epsilon) {
            return (int) (Math.random() * NUM_ACTIONS);
        }
        int best = 0;
        for (int i = 1; i < values.length; i++) {
            if (values[i] > values[best]) {
                best = i;
            }
        }
        return best;
    }

    private void executeAction(int action, ScannedBotEvent e) {
        switch (action) {
            case ACTION_FIRE:
                if (getGunHeat() == 0) {
                    fire(1);
                }
                break;
            case ACTION_AHEAD:
                forward(50);
                break;
            case ACTION_LEFT:
                turnLeft(30);
                break;
            case ACTION_RIGHT:
                turnRight(30);
                break;
            default:
                break;
        }
    }

    private void updateQ(String newState) {
        if (lastState != null && lastAction >= 0) {
            double[] qValues = qTable.computeIfAbsent(lastState, k -> new double[NUM_ACTIONS]);
            double[] next = qTable.computeIfAbsent(newState, k -> new double[NUM_ACTIONS]);
            double bestNext = next[0];
            for (int i = 1; i < next.length; i++) {
                if (next[i] > bestNext) bestNext = next[i];
            }
            qValues[lastAction] += alpha * (reward + gamma * bestNext - qValues[lastAction]);
            reward = 0;
        }
    }

    private void loadQTable() {
        java.io.File file = new java.io.File("qtable.csv");
        if (!file.exists()) return;
        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == NUM_ACTIONS + 1) {
                    double[] vals = new double[NUM_ACTIONS];
                    for (int i = 0; i < NUM_ACTIONS; i++) {
                        vals[i] = Double.parseDouble(parts[i + 1]);
                    }
                    qTable.put(parts[0], vals);
                }
            }
        } catch (java.io.IOException ignored) {
        }
    }

    private void saveQTable() {
        try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter("qtable.csv"))) {
            for (java.util.Map.Entry<String, double[]> entry : qTable.entrySet()) {
                pw.print(entry.getKey());
                for (double v : entry.getValue()) {
                    pw.print("," + v);
                }
                pw.println();
            }
        } catch (java.io.IOException ignored) {
        }
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
