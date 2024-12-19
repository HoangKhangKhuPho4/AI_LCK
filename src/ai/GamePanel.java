


// File: ai/GamePanel.java
package ai;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.List;

public class GamePanel extends JPanel {
    private Game game;
    private final int TILE_SIZE = 40;

    // Images for walls, bombs, players, and items
    private Image wallIndestructibleImage;
    private Image wallDestructibleImage;
    private Image bombImage;
    private Image playerImage;
    private Image aiPlayerImage;
    private Image balloonImage;
    private Image speedItemImage; // Hình ảnh vật phẩm tăng tốc
    private Image explosionRangeItemImage; // Hình ảnh vật phẩm tăng phạm vi nổ
    private ImageIcon explosionGif; // Hình ảnh hiệu ứng nổ

    public GamePanel(Game game) {
        this.game = game;
        this.setPreferredSize(new Dimension(game.getGameMap().getWidth() * TILE_SIZE,
                game.getGameMap().getHeight() * TILE_SIZE));

        // Load images for different wall types
        this.wallIndestructibleImage = loadImage("wallIndestructible.jpg");
        this.wallDestructibleImage = loadImage("wallDestructible.png");
        this.bombImage = loadImage("bomb.jpg");
        this.playerImage = loadImage("player.jpg");
        this.aiPlayerImage = loadImage("ai_player.jpg");
        this.balloonImage = loadImage("ballon.jpg");

        // Load images for items
        this.speedItemImage = loadImage("speedItem.jpg"); // Hình ảnh vật phẩm tăng tốc
        this.explosionRangeItemImage = loadImage("explosionRangeItem.png"); // Hình ảnh vật phẩm tăng phạm vi nổ

        // Tải hiệu ứng nổ động từ tệp .gif
        this.explosionGif = loadGif("dFOsRT.gif");

    }



    private ImageIcon loadGif(String gifName) {
        try {
            java.net.URL gifURL = getClass().getResource("/images/" + gifName);
            if (gifURL != null) {
                return new ImageIcon(gifURL); // Tải GIF
            } else {
                System.err.println("Không thể tìm thấy tệp GIF: " + gifName);
                return null; // Nếu không tải được, trả về null
            }
        } catch (IllegalArgumentException ex) {
            System.err.println("Lỗi khi tải GIF: " + ex.getMessage());
            return null; // Trả về null nếu có lỗi
        }
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
                int xPos = x * TILE_SIZE;
                int yPos = y * TILE_SIZE;

                if (tile == '#') {
                    if (wallIndestructibleImage != null) {
                        g.drawImage(wallIndestructibleImage, xPos, yPos, TILE_SIZE, TILE_SIZE, this);
                    }
                } else if (tile == 'D') {
                    if (wallDestructibleImage != null) {
                        g.drawImage(wallDestructibleImage, xPos, yPos, TILE_SIZE, TILE_SIZE, this);
                    }
                } else {
                    g.setColor(Color.WHITE);
                    g.fillRect(xPos, yPos, TILE_SIZE, TILE_SIZE);
                    g.setColor(Color.LIGHT_GRAY);
                    g.drawRect(xPos, yPos, TILE_SIZE, TILE_SIZE);
                }
            }
        }

        // Vẽ vật phẩm
        for (Item item : map.getItems()) {
            int itemX = item.getX() * TILE_SIZE;
            int itemY = item.getY() * TILE_SIZE;
            if (item.getType() == Item.ItemType.SPEED) {
                if (speedItemImage != null) {
                    g.drawImage(speedItemImage, itemX, itemY, TILE_SIZE, TILE_SIZE, this);
                }
            } else if (item.getType() == Item.ItemType.EXPLOSION_RANGE) {
                if (explosionRangeItemImage != null) {
                    g.drawImage(explosionRangeItemImage, itemX, itemY, TILE_SIZE, TILE_SIZE, this);
                }
            }
        }

        // Vẽ bom và hiệu ứng nổ
        for (Bomb bomb : bombs) {
            int bombX = bomb.getX() * TILE_SIZE;
            int bombY = bomb.getY() * TILE_SIZE;

            if (bomb.isExploded()) {
                // Kiểm tra nếu vị trí nổ trong phạm vi bản đồ
                if (bombX >= 0 && bombX < map.getWidth() * TILE_SIZE && bombY >= 0 && bombY < map.getHeight() * TILE_SIZE) {
                    // Vẽ GIF hiệu ứng nổ tại vị trí bom
                    if (explosionGif != null) {
                        g.drawImage(explosionGif.getImage(), bombX, bombY, TILE_SIZE, TILE_SIZE, this);
                    }

                    // Vẽ hiệu ứng nổ xung quanh (các ô ảnh hưởng của vụ nổ)
                    List<int[]> explosionTiles = game.getExplosionTiles(bomb);
                    for (int[] tile : explosionTiles) {
                        int tx = tile[0] * TILE_SIZE;
                        int ty = tile[1] * TILE_SIZE;
                        // Không còn vẽ màu vàng nữa, chỉ vẽ GIF
                        if (explosionGif != null) {
                            g.drawImage(explosionGif.getImage(), tx, ty, TILE_SIZE, TILE_SIZE, this);
                        }
                    }
                }
            } else {
                if (bombImage != null) {
                    g.drawImage(bombImage, bombX, bombY, TILE_SIZE, TILE_SIZE, this);
                }
            }
        }

        // Vẽ Balloon
        for (Balloon balloon : balloons) {
            if (balloon.isAlive()) {
                int balloonX = balloon.getX() * TILE_SIZE;
                int balloonY = balloon.getY() * TILE_SIZE;
                if (balloonImage != null) {
                    g.drawImage(balloonImage, balloonX, balloonY, TILE_SIZE, TILE_SIZE, this);
                }
            }
        }

        // Vẽ AIPlayer
        if (aiPlayer.isAlive()) {
            if (aiPlayerImage != null) {
                g.drawImage(aiPlayerImage, aiPlayer.getX() * TILE_SIZE, aiPlayer.getY() * TILE_SIZE, TILE_SIZE, TILE_SIZE, this);
            }
        }

        // Vẽ người chơi
        if (playerImage != null) {
            g.drawImage(playerImage, player.getX() * TILE_SIZE, player.getY() * TILE_SIZE, TILE_SIZE, TILE_SIZE, this);
        }

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



    private Image loadImage(String imageName) {
        try {
            // Đường dẫn bắt đầu từ thư mục gốc của classpath
            java.net.URL imgURL = getClass().getResource("/images/" + imageName);
            if (imgURL != null) {
                System.out.println("Tải ảnh thành công: " + imgURL);
                return ImageIO.read(imgURL);
            } else {
                System.err.println("Không thể tìm thấy đường dẫn ảnh: " + imageName);
                return null;
            }
        } catch (IOException | IllegalArgumentException ex) {
            System.err.println("Lỗi khi tải ảnh: " + imageName + ". Sử dụng màu mặc định.");
            return null; // Hoặc trả về một hình ảnh mặc định nếu muốn
        }
    }
}
