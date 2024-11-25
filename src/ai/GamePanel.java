// File: ai/GamePanel.java
package ai;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class GamePanel extends JPanel {
    private Game game;
    private final int TILE_SIZE = 40;

    public GamePanel(Game game) {
        this.game = game;
        this.setPreferredSize(new Dimension(game.getGameMap().getWidth() * TILE_SIZE,
                game.getGameMap().getHeight() * TILE_SIZE));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        GameMap map = game.getGameMap();
        Player player = game.getPlayer();
        AIPlayer aiPlayer = game.getAiPlayer();
        List<Bomb> bombs = game.getBombs();
        List<Balloon> balloons = game.getBalloons();
        // Vẽ bản đồ
        for (int x = 0; x < map.getWidth(); x++) {
            for (int y = 0; y < map.getHeight(); y++) {
                char tile = map.getTile(x, y);
                if (tile == '#') {
                    g.setColor(Color.BLACK); // Tường không phá hủy
                    g.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                } else if (tile == 'D') {
                    g.setColor(Color.GRAY); // Tường phá hủy
                    g.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                } else {
                    g.setColor(Color.WHITE); // Ô trống
                    g.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                    g.setColor(Color.LIGHT_GRAY);
                    g.drawRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }
        }
        // Vẽ vật phẩm
        for (Item item : map.getItems()) {
            int itemX = item.getX() * TILE_SIZE;
            int itemY = item.getY() * TILE_SIZE;
            if (item.getType() == Item.ItemType.SPEED) {
                g.setColor(Color.GREEN); // Màu xanh cho vật phẩm tăng tốc
            } else if (item.getType() == Item.ItemType.EXPLOSION_RANGE) {
                g.setColor(Color.ORANGE); // Màu cam cho vật phẩm tăng phạm vi nổ
            }
            g.fillOval(itemX + 10, itemY + 10, TILE_SIZE - 20, TILE_SIZE - 20);
        }
        // Vẽ bom và hiệu ứng nổ
        for (Bomb bomb : bombs) {
            int bombX = bomb.getX() * TILE_SIZE;
            int bombY = bomb.getY() * TILE_SIZE;
            if (bomb.isExploded()) {
                g.setColor(Color.YELLOW); // Hiệu ứng nổ màu vàng
                g.fillRect(bombX, bombY, TILE_SIZE, TILE_SIZE);
                // Vẽ hiệu ứng nổ các ô xung quanh với phạm vi nổ
                List<int[]> explosionTiles = game.getExplosionTiles(bomb);
                for (int[] tile : explosionTiles) {
                    int tx = tile[0] * TILE_SIZE;
                    int ty = tile[1] * TILE_SIZE;
                    g.fillRect(tx, ty, TILE_SIZE, TILE_SIZE);
                }
            } else {
                g.setColor(Color.RED); // Bom chưa nổ màu đỏ
                g.fillOval(bombX + 5, bombY + 5, TILE_SIZE - 10, TILE_SIZE - 10);
            }
        }
        // Vẽ Balloon
        for (Balloon balloon : balloons) {
            if (balloon.isAlive()) {
                int balloonX = balloon.getX() * TILE_SIZE;
                int balloonY = balloon.getY() * TILE_SIZE;
                g.setColor(Color.MAGENTA); // Màu Balloon
                g.fillOval(balloonX + 5, balloonY + 5, TILE_SIZE - 10, TILE_SIZE - 10);
            }
        }
        // Vẽ AIPlayer
        if (aiPlayer.isAlive()) {
            g.setColor(Color.RED); // Màu đỏ cho AIPlayer
            g.fillRect(aiPlayer.getX() * TILE_SIZE, aiPlayer.getY() * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        }
        // Vẽ người chơi
        g.setColor(Color.BLUE);
        g.fillRect(player.getX() * TILE_SIZE, player.getY() * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        // Nếu trò chơi kết thúc, hiển thị thông báo
        if (game.isGameOver() || game.isGameWon()) {
            g.setColor(new Color(0, 0, 0, 150)); // Màu đen trong suốt
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            String message = "";
            if (game.isGameWon()) {
                message = "Bạn đã thắng!";
            } else {
                message = "Game Over!";
            }
            FontMetrics fm = g.getFontMetrics();
            int msgWidth = fm.stringWidth(message);
            int msgAscent = fm.getAscent();
            g.drawString(message, (getWidth() - msgWidth) / 2, (getHeight() + msgAscent) / 2);
        }


    }
}  