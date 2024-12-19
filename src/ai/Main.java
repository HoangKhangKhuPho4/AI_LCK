
package ai;

import ai.Game;
import ai.GameFrame;

import javax.swing.*;

// Trong lớp Main
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Game game = new Game();
            GameFrame gameFrame = new GameFrame(game);
            // Không sử dụng Timer cho cơ chế turn-based
        });
    }
}
