package ai;

import javax.swing.*;

/**
 * Lớp chính để chạy trò chơi.
 */
public class Main {
    public static void main(String[] args) {
        Game game = new Game();
        SwingUtilities.invokeLater(() -> {
            new GameFrame(game);
        });
    }
}
