package ai;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.*;

/**
 * Lớp giao diện trò chơi.
 */
public class GameFrame extends JFrame implements Observer {
    private Game game;
    private GamePanel gamePanel;
    private boolean playerTurn = true; // Biến để theo dõi lượt đi

    public GameFrame(Game game) {
        this.game = game;
        this.gamePanel = new GamePanel(game);
        this.setTitle("BomberMan AI");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.add(gamePanel);
        this.pack();
        this.setLocationRelativeTo(null); // Hiển thị giữa màn hình
        this.setVisible(true);

        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (game.isGameOver()) return; // Nếu trò chơi kết thúc, không xử lý phím
                if (!playerTurn) return; // Nếu không phải lượt của người chơi, không xử lý phím

                Action playerAction = null;
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W: // Phím W
                    case KeyEvent.VK_UP: // Phím mũi tên lên
                        playerAction = new Action(ActionType.MOVE_UP);
                        break;
                    case KeyEvent.VK_S: // Phím S
                    case KeyEvent.VK_DOWN: // Phím mũi tên xuống
                        playerAction = new Action(ActionType.MOVE_DOWN);
                        break;
                    case KeyEvent.VK_A: // Phím A
                    case KeyEvent.VK_LEFT: // Phím mũi tên trái
                        playerAction = new Action(ActionType.MOVE_LEFT);
                        break;
                    case KeyEvent.VK_D: // Phím D
                    case KeyEvent.VK_RIGHT: // Phím mũi tên phải
                        playerAction = new Action(ActionType.MOVE_RIGHT);
                        break;
                    case KeyEvent.VK_SPACE: // Phím Space
                        playerAction = new Action(ActionType.PLACE_BOMB);
                        break;
                }

                if (playerAction != null) {
                    game.playerMove(playerAction); // Người chơi thực hiện hành động
                    refresh(); // Cập nhật lại giao diện sau khi di chuyển
                    playerTurn = false; // Chuyển lượt cho AI

                    // Nếu trò chơi chưa kết thúc, AI sẽ thực hiện lượt đi của mình
                    if (!game.isGameOver()) {
                        SwingUtilities.invokeLater(() -> {
                            game.aiMove(); // AI thực hiện hành động
                            refresh(); // Cập nhật lại giao diện sau khi AI di chuyển
                            playerTurn = true; // Chuyển lượt lại cho người chơi
                        });
                    }
                }
            }
        });

        // Đăng ký GameFrame làm Observer cho AIPlayer (nếu cần)
        game.getAiPlayer().attach(this);
    }

    /**
     * Cập nhật lại giao diện.
     */
    public void refresh() {
        gamePanel.repaint(); // Cập nhật lại màn hình
    }

    @Override
    public void update(Event event) {
        if (event instanceof AIPlayerMovedEvent) {
            refresh();
        }
    }
}