

package ai;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Lớp chính để chạy trò chơi.
 */
public class Main {
    public static void main(String[] args) {
        Game game = new Game();
        GameFrame gameFrame = new GameFrame(game);
        Timer timer = new Timer(100, e -> {
            // Thực hiện cập nhật trên luồng nền
            new Thread(() -> {
                game.update();
                // Cập nhật giao diện trên luồng EDT
                SwingUtilities.invokeLater(() -> {
                    gameFrame.refresh();
                    // Kiểm tra trạng thái trò chơi
                    if (game.isGameOver()) {
                        ((Timer) e.getSource()).stop();
                    }
                });
            }).start();
        });
        timer.start();

    }
}
