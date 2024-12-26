
// Add replay functionality to Bomberman UI
package ai;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class GameFrame extends JFrame implements Observer {
    private Game game;
    private GamePanel gamePanel;
    private JLabel scoreLabel; // Label to display the score
    private JLabel timeLabel;  // Label to display the remaining time
    private JLabel livesLabel; // Label to display the player's lives
    private int score = 0; // Initialize the score to 0
    private int timeLeft = 600; // Time in seconds (3 minutes)
    private int lives = 1; // Default lives
    private Timer gameTimer;

    public GameFrame(Game game) {
        this.game = game;
        this.gamePanel = new GamePanel(game);
        this.setTitle("BomberMan AI");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create a container to hold the game panel and status panel
        JPanel container = new JPanel(new BorderLayout());

        // Add the game panel to the center
        container.add(gamePanel, BorderLayout.CENTER);

        // Create a status panel for score, time, and lives
        JPanel statusPanel = new JPanel(new GridLayout(1, 3));
        statusPanel.setBackground(Color.BLACK);
        statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        statusPanel.setPreferredSize(new Dimension(container.getWidth(), 40)); // Smaller height

        // Add score label
        scoreLabel = new JLabel("Score: 0", SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        scoreLabel.setForeground(Color.WHITE);
        statusPanel.add(scoreLabel);

        // Add time label
        timeLabel = new JLabel("Time: 03:00", SwingConstants.CENTER);
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        timeLabel.setForeground(Color.WHITE);
        statusPanel.add(timeLabel);

        // Add lives label
        livesLabel = new JLabel("Lives: 1", SwingConstants.CENTER);
        livesLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        livesLabel.setForeground(Color.WHITE);
        statusPanel.add(livesLabel);

        // Add the status panel to the top
        container.add(statusPanel, BorderLayout.NORTH);

        // Set the container as the content pane
        this.setContentPane(container);

        this.pack();
        this.setLocationRelativeTo(null); // Center the frame
        this.setVisible(true);

        // Set up the timer for the game countdown
        setupGameTimer();

        // Trong lớp GameFrame
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (game.isGameOver()) return; // Nếu trò chơi kết thúc, không xử lý phím
                Action playerAction = null;
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W:
                    case KeyEvent.VK_UP:
                        playerAction = new Action(ActionType.MOVE_UP);
                        break;
                    case KeyEvent.VK_S:
                    case KeyEvent.VK_DOWN:
                        playerAction = new Action(ActionType.MOVE_DOWN);
                        break;
                    case KeyEvent.VK_A:
                    case KeyEvent.VK_LEFT:
                        playerAction = new Action(ActionType.MOVE_LEFT);
                        break;
                    case KeyEvent.VK_D:
                    case KeyEvent.VK_RIGHT:
                        playerAction = new Action(ActionType.MOVE_RIGHT);
                        break;
                    case KeyEvent.VK_SPACE:
                        playerAction = new Action(ActionType.PLACE_BOMB, game.getPlayer().getX(), game.getPlayer().getY());
                        break;
                }
                if (playerAction != null) {
                    game.playerMove(playerAction);
                    refresh(); // Cập nhật lại giao diện sau khi di chuyển
                }
            }
        });


        // Register GameFrame as an observer for AIPlayer
        game.getAiPlayer().attach(this);
    }

    /**
     * Refresh the game interface.
     */
    public void refresh() {
        gamePanel.repaint(); // Update the game panel
        updateScore(); // Update the score display
        updateLives(); // Update lives display
    }

    /**
     * Update the score display.
     */
    private void updateScore() {
        score = calculateScore();
        scoreLabel.setText("Score: " + score);
    }

    /**
     * Update the lives display.
     */
    private void updateLives() {
        livesLabel.setText("Lives: " + lives);
    }

    /**
     * Calculate the current score based on the game state.
     */
    private int calculateScore() {
        int score = 0;

        // Example: Add points for destroyed balloons
        for (Balloon balloon : game.getBalloons()) {
            if (!balloon.isAlive()) {
                score += 100; // Example: 100 points per destroyed balloon
            }
        }

        // Example: Add points for destroyed walls
        for (int x = 0; x < game.getGameMap().getWidth(); x++) {
            for (int y = 0; y < game.getGameMap().getHeight(); y++) {
                if (game.getGameMap().getTile(x, y) == ' ') { // Check for destroyed destructible walls
                    score += 20; // Example: 20 points per destroyed wall
                }
            }
        }

        // Example: Add points for collected items
        for (Item item : game.getGameMap().getItems()) {
            if (!game.getPlayer().isAlive()) { // Check if the item is collected (not on the map)
                score += 50; // Example: 50 points per collected item
            }
        }

        return score;
    }

    /**
     * Set up the game timer.
     */
    private void setupGameTimer() {
        gameTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (timeLeft > 0) {
                    timeLeft--;
                    updateTimeDisplay();
                } else {
                    ((Timer) e.getSource()).stop();
                    endGame();
                }
            }
        });
        gameTimer.start();
    }

    /**
     * Update the time display.
     */
    private void updateTimeDisplay() {
        int minutes = timeLeft / 60;
        int seconds = timeLeft % 60;
        timeLabel.setText(String.format("Time: %02d:%02d", minutes, seconds));
    }

    /**
     * End the game when the time is up.
     */
    private void endGame() {
        // Stop the game timer
        gameTimer.stop();

        // Display a panel with a replay button
        JPanel endPanel = new JPanel(new BorderLayout());
        JLabel endLabel = new JLabel("Game Over! Time's up!", SwingConstants.CENTER);
        endLabel.setFont(new Font("Arial", Font.BOLD, 16));
        endPanel.add(endLabel, BorderLayout.CENTER);

        JButton replayButton = new JButton("Replay");
        replayButton.setFont(new Font("Arial", Font.PLAIN, 14));
        replayButton.addActionListener(e -> restartGame());
        endPanel.add(replayButton, BorderLayout.SOUTH);

        // Show the end panel in a dialog
        JOptionPane.showMessageDialog(this, endPanel, "Game Over", JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Restart the game by resetting the state.
     */
    private void restartGame() {
        // Dispose the current frame and start a new game
        this.dispose();
        Game newGame = new Game(); // Create a new instance of the game
        GameFrame newFrame = new GameFrame(newGame); // Create a new frame
        newFrame.setVisible(true); // Show the new frame
    }

    @Override
    public void update(Event event) {
        if (event instanceof AIPlayerMovedEvent) {
            refresh();
        }
    }
}
