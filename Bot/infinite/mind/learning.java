package infinite.mind;

import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;
import java.awt.Color;
import java.util.*;

public class TankBot extends Bot {
    // Neural Network parameters
    private static final int INPUT_SIZE = 8;
    private static final int HIDDEN_SIZE = 16;
    private static final int OUTPUT_SIZE = 8;
    
    // Learning parameters
    private static final double LEARNING_RATE = 0.001;
    private static final double DISCOUNT_FACTOR = 0.95;
    private static final double EXPLORATION_RATE = 0.3;
    private static final int MEMORY_CAPACITY = 1000;
    private static final int BATCH_SIZE = 32;
    
    // Action constants
    private static final int FIRE_HEAVY = 0;
    private static final int MOVE_AHEAD = 1;
    private static final int TURN_LEFT = 2;
    private static final int TURN_RIGHT = 3;
    private static final int AIM_LEFT = 4;
    private static final int AIM_RIGHT = 5;
    private static final int FIRE_LIGHT = 6;
    private static final int MOVE_BACK = 7;
    
    // Neural network
    private double[][] weights1;
    private double[][] weights2;
    private double[] hiddenLayer;
    
    // Reinforcement learning
    private double reward;
    private double lastScannedBearing;
    private double lastScannedDistance;
    private double[] lastState;
    private int lastAction;
    private List<Experience> memory = new ArrayList<>();
    private Random random = new Random();
    
    // Arena info
    private double arenaWidth;
    private double arenaHeight;
    private double maxDistance;
    
    // Experience replay memory
    private static class Experience {
        double[] state;
        int action;
        double reward;
        double[] nextState;
        
        Experience(double[] state, int action, double reward, double[] nextState) {
            this.state = state;
            this.action = action;
            this.reward = reward;
            this.nextState = nextState;
        }
    }

    public static void main(String[] args) {
        new TankBot().start();
    }

    TankBot() {
        super(BotInfo.fromFile("TankBot.json"));
    }

    @Override
    public void run() {
        // Initialize arena dimensions
        arenaWidth = getArenaWidth();
        arenaHeight = getArenaHeight();
        maxDistance = Math.sqrt(arenaWidth * arenaWidth + arenaHeight * arenaHeight);
        

        // Initialize neural network
        initializeNetwork();
        loadModel();
        
        // Main bot loop
        while (isRunning()) {
            turnRadarRight(360); // Continuous scanning
        }
    }

    @Override
    public void onScannedBot(ScannedBotEvent e) {
        // Update target information
        lastScannedDistance = distanceTo(e.getX(), e.getY());
        lastScannedBearing = bearingTo(e.getX(), e.getY());
        
        // Create current state
        double[] currentState = createState();
        
        // Store experience and train
        if (lastState != null) {
            memory.add(new Experience(lastState, lastAction, reward, currentState));
            reward = 0; // Reset reward after storing
            
            // Train when enough experiences
            if (memory.size() >= BATCH_SIZE) {
                trainNetwork();
            }
        }
        
        // Select and execute action
        int action = selectAction(currentState);
        executeAction(action);
        
        // Update for next cycle
        lastState = currentState;
        lastAction = action;
    }

    @Override
    public void onHitByBullet(HitByBulletEvent e) {
        reward -= 5; // Penalize for getting hit
    }

    @Override
    public void onBulletHit(BulletHitBotEvent e) {
        reward += 10; // Reward for hitting opponent
    }

    @Override
    public void onHitWall(HitWallEvent e) {
        reward -= 3; // Penalize for wall collision
    }

    @Override
    public void onRoundEnded(RoundEndedEvent e) {
        saveModel();
    }

    private double[] createState() {
        // Normalized state representation
        return new double[] {
            getX() / arenaWidth,                  // X position (0-1)
            getY() / arenaHeight,                 // Y position (0-1)
            getDirection() / 360.0,               // Facing direction (0-1)
            lastScannedBearing / 180.0,           // Bearing to opponent (-1 to 1)
            lastScannedDistance / maxDistance,    // Distance to opponent (0-1)
            getEnergy() / 100.0,                  // Energy level (0-1)
            getGunHeat(),
            getGunDirection() / 360.0           // Gun direction (0-1)
        };
    }

    private void initializeNetwork() {
        weights1 = new double[INPUT_SIZE][HIDDEN_SIZE];
        weights2 = new double[HIDDEN_SIZE][OUTPUT_SIZE];
        hiddenLayer = new double[HIDDEN_SIZE];
        
        // Initialize weights with small random values
        for (int i = 0; i < INPUT_SIZE; i++) {
            for (int j = 0; j < HIDDEN_SIZE; j++) {
                weights1[i][j] = (random.nextDouble() - 0.5) * 0.2;
            }
        }
        
        for (int i = 0; i < HIDDEN_SIZE; i++) {
            for (int j = 0; j < OUTPUT_SIZE; j++) {
                weights2[i][j] = (random.nextDouble() - 0.5) * 0.2;
            }
        }
    }

    private double[] forwardPass(double[] inputs) {
        // Calculate hidden layer activations
        for (int j = 0; j < HIDDEN_SIZE; j++) {
            hiddenLayer[j] = 0;
            for (int i = 0; i < INPUT_SIZE; i++) {
                hiddenLayer[j] += inputs[i] * weights1[i][j];
            }
            hiddenLayer[j] = relu(hiddenLayer[j]);
        }
        
        // Calculate output layer
        double[] outputs = new double[OUTPUT_SIZE];
        for (int k = 0; k < OUTPUT_SIZE; k++) {
            outputs[k] = 0;
            for (int j = 0; j < HIDDEN_SIZE; j++) {
                outputs[k] += hiddenLayer[j] * weights2[j][k];
            }
        }
        return outputs;
    }

    private double relu(double x) {
        return Math.max(0, x);
    }

    private int selectAction(double[] state) {
        if (random.nextDouble() < EXPLORATION_RATE) {
            return random.nextInt(OUTPUT_SIZE); // Exploration
        }
        
        double[] qValues = forwardPass(state);
        int bestAction = 0;
        for (int i = 1; i < OUTPUT_SIZE; i++) {
            if (qValues[i] > qValues[bestAction]) {
                bestAction = i;
            }
        }
        return bestAction;
    }

    private void executeAction(int action) {
        switch (action) {
            case FIRE_HEAVY:
                if (getGunHeat() == 0) fire(3.0);
                break;
                
            case MOVE_AHEAD:
                forward(100);
                break;
                
            case TURN_LEFT:
                turnLeft(45);
                break;
                
            case TURN_RIGHT:
                turnRight(45);
                break;
                
            case AIM_LEFT:
                turnGunLeft(30);
                break;
                
            case AIM_RIGHT:
                turnGunRight(30);
                break;
                
            case FIRE_LIGHT:
                if (getGunHeat() == 0) fire(1.0);
                break;
                
            case MOVE_BACK:
                back(80);
                break;
        }
    }

    private void trainNetwork() {
        // Create mini-batch
        Collections.shuffle(memory);
        List<Experience> batch = memory.subList(0, Math.min(BATCH_SIZE, memory.size()));
        
        // Train on each experience
        for (Experience exp : batch) {
            // Current Q-values prediction
            double[] currentQ = forwardPass(exp.state);
            
            // Calculate target Q-values
            double[] nextQ = forwardPass(exp.nextState);
            double maxNextQ = Arrays.stream(nextQ).max().orElse(0);
            double target = exp.reward + DISCOUNT_FACTOR * maxNextQ;
            
            // Update the Q-value for the taken action
            currentQ[exp.action] = target;
            
            // Backpropagation to update weights
            backpropagate(exp.state, currentQ);
        }
        
        // Maintain memory size
        if (memory.size() > MEMORY_CAPACITY) {
            memory.subList(0, memory.size() - MEMORY_CAPACITY).clear();
        }
    }

    private void backpropagate(double[] inputs, double[] targets) {
        // Forward pass (already computed in trainNetwork, but we'll recompute for clarity)
        double[] outputs = forwardPass(inputs);
        
        // Calculate output errors
        double[] outputErrors = new double[OUTPUT_SIZE];
        for (int k = 0; k < OUTPUT_SIZE; k++) {
            outputErrors[k] = targets[k] - outputs[k];
        }
        
        // Calculate hidden layer errors
        double[] hiddenErrors = new double[HIDDEN_SIZE];
        for (int j = 0; j < HIDDEN_SIZE; j++) {
            double errorSum = 0;
            for (int k = 0; k < OUTPUT_SIZE; k++) {
                errorSum += outputErrors[k] * weights2[j][k];
            }
            hiddenErrors[j] = hiddenLayer[j] > 0 ? errorSum : 0; // ReLU derivative
        }
        
        // Update weights between hidden and output layers
        for (int j = 0; j < HIDDEN_SIZE; j++) {
            for (int k = 0; k < OUTPUT_SIZE; k++) {
                weights2[j][k] += LEARNING_RATE * outputErrors[k] * hiddenLayer[j];
            }
        }
        
        // Update weights between input and hidden layers
        for (int i = 0; i < INPUT_SIZE; i++) {
            for (int j = 0; j < HIDDEN_SIZE; j++) {
                weights1[i][j] += LEARNING_RATE * hiddenErrors[j] * inputs[i];
            }
        }
    }

    private void saveModel() {
        try {
            System.out.println("Saving model...");
            java.io.File file = new java.io.File("dqn_model.csv");
            try (java.io.PrintWriter writer = new java.io.PrintWriter(file)) {
                // Save input-hidden weights
                for (int i = 0; i < INPUT_SIZE; i++) {
                    for (int j = 0; j < HIDDEN_SIZE; j++) {
                        writer.print(weights1[i][j]);
                        if (j < HIDDEN_SIZE - 1) writer.print(",");
                    }
                    writer.println();
                }
                
                // Save hidden-output weights
                for (int j = 0; j < HIDDEN_SIZE; j++) {
                    for (int k = 0; k < OUTPUT_SIZE; k++) {
                        writer.print(weights2[j][k]);
                        if (k < OUTPUT_SIZE - 1) writer.print(",");
                    }
                    writer.println();
                }
            }
            System.out.println("Model saved successfully to: " + file.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("Error saving model: " + e.getMessage());
        }
    }

    private void loadModel() {
        java.io.File file = new java.io.File("dqn_model.csv");
        if (!file.exists()) {
            System.out.println("No model file found. Using random initialization.");
            return;
        }
        
        try (java.util.Scanner scanner = new java.util.Scanner(file)) {
            System.out.println("Loading model...");
            // Load input-hidden weights
            for (int i = 0; i < INPUT_SIZE; i++) {
                String line = scanner.nextLine();
                String[] values = line.split(",");
                for (int j = 0; j < HIDDEN_SIZE; j++) {
                    weights1[i][j] = Double.parseDouble(values[j]);
                }
            }
            
            // Load hidden-output weights
            for (int j = 0; j < HIDDEN_SIZE; j++) {
                String line = scanner.nextLine();
                String[] values = line.split(",");
                for (int k = 0; k < OUTPUT_SIZE; k++) {
                    weights2[j][k] = Double.parseDouble(values[k]);
                }
            }
            System.out.println("Model loaded successfully.");
        } catch (Exception e) {
            System.err.println("Error loading model: " + e.getMessage());
        }
    }
}