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
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W: // Phím W
                    case KeyEvent.VK_UP: // Phím mũi tên lên
                        game.moveEntity(game.getPlayer(), 0, -1); // Di chuyển lên
                        break;
                    case KeyEvent.VK_S: // Phím S
                    case KeyEvent.VK_DOWN: // Phím mũi tên xuống
                        game.moveEntity(game.getPlayer(), 0, 1); // Di chuyển xuống
                        break;
                    case KeyEvent.VK_A: // Phím A
                    case KeyEvent.VK_LEFT: // Phím mũi tên trái
                        game.moveEntity(game.getPlayer(), -1, 0); // Di chuyển trái
                        break;
                    case KeyEvent.VK_D: // Phím D
                    case KeyEvent.VK_RIGHT: // Phím mũi tên phải
                        game.moveEntity(game.getPlayer(), 1, 0); // Di chuyển phải
                        break;
                    case KeyEvent.VK_SPACE: // Phím Space
                        game.placeBomb(game.getPlayer()); // Đặt bom
                        break;
                }
                refresh(); // Cập nhật lại giao diện sau khi di chuyển
            }
        });

        // Đăng ký GameFrame làm Observer cho AIPlayer
        game.getAiPlayer().attach(this);
    }

    /**
     * Cập nhật lại giao diện 
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