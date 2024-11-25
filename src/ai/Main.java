// File: ai/Main.java
package ai;

import javax.swing.Timer;

public class Main {
    public static void main(String[] args) {
        Game game = new Game();
        GameFrame gameFrame = new GameFrame(game);
        // Vòng lặp cập nhật và làm mới màn hình
        Timer timer = new Timer(100, e -> { // Tốc độ cập nhật: 0.1 giây mỗi tick
            game.update();
            gameFrame.refresh();
            // Nếu trò chơi kết thúc, dừng Timer
            if (game.isGameOver()) {
                ((Timer) e.getSource()).stop();
            }
        });
        timer.start();
    }
}