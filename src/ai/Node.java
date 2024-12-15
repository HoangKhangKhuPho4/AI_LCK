package ai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Node {
    private int aiPlayerX;
    private int aiPlayerY;
    private int playerX;
    private int playerY;
    private int bombCount;
    private int[][] gameMap; // 0: empty, 1: obstacle, 2: bomb
    private List<Bomb> bombs; // Danh sách bom trong trò chơi
    private int explosionRange; // Phạm vi nổ của AIPlayer

    // Constructor để khởi tạo Node với danh sách bom và phạm vi nổ
    public Node(int aiPlayerX, int aiPlayerY, int playerX, int playerY, int bombCount, int[][] gameMap, List<Bomb> bombs, int explosionRange) {
        this.aiPlayerX = aiPlayerX;
        this.aiPlayerY = aiPlayerY;
        this.playerX = playerX;
        this.playerY = playerY;
        this.bombCount = bombCount;
        this.gameMap = deepCopyGameMap(gameMap);
        this.bombs = deepCopyBombs(bombs);
        this.explosionRange = explosionRange;
    }

    public Node(int aiPlayerX, int aiPlayerY, int playerX, int playerY, int bombCount, int[][] gameMap) {
        this(aiPlayerX, aiPlayerY, playerX, playerY, bombCount, gameMap, new ArrayList<>(), 1);
    }


    // Getter cho vị trí AI và người chơi
    public int getAIPlayerX() {
        return aiPlayerX;
    }

    public int getAIPlayerY() {
        return aiPlayerY;
    }

    public int getPlayerX() {
        return playerX;
    }

    public int getPlayerY() {
        return playerY;
    }

    // Getter cho số lượng bom và bản đồ
    public int getBombCount() {
        return bombCount;
    }

    public int[][] getGameMap() {
        return deepCopyGameMap(gameMap);
    }

    public List<Bomb> getBombs() {
        return deepCopyBombs(bombs);
    }

    public int getExplosionRange() {
        return explosionRange;
    }

    // Kiểm tra xem một ô có hợp lệ hay không (ô trống và không có chướng ngại vật)
    public boolean isValidMove(int x, int y) {
        return x >= 0 && x < gameMap.length && y >= 0 && y < gameMap[0].length && gameMap[x][y] == 0;
    }

    // Kiểm tra xem AI có thể đặt bom gần người chơi hay không
    public boolean canPlaceBombNearPlayer() {
        int playerX = this.playerX;
        int playerY = this.playerY;

        // Kiểm tra các ô xung quanh người chơi
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if ((dx != 0 || dy != 0) && isValidMove(playerX + dx, playerY + dy)) {
                    return true; // Có thể đặt bom ở vị trí này
                }
            }
        }
        return false; // Không thể đặt bom gần người chơi
    }

    // Phương thức trả về một đối tượng Game (nếu cần)
    public Game getGame() {
        // Tạo đối tượng Game mới
        Game game = new Game();

        // Cập nhật thông tin từ Node vào Game
        game.setAIPlayerX(this.aiPlayerX); // Vị trí của AI
        game.setAIPlayerY(this.aiPlayerY); // Vị trí của AI
        game.setPlayerX(this.playerX);     // Vị trí của người chơi
        game.setPlayerY(this.playerY);     // Vị trí của người chơi
        game.setBombCount(this.bombCount); // Số lượng bom
        game.setGameMap(this.gameMap);     // Bản đồ trò chơi
        game.setBombs(this.bombs);         // Danh sách bom

        return game;
    }

    // Phương thức getStateHash để tạo khóa duy nhất cho Transposition Table
    public String getStateHash() {
        StringBuilder sb = new StringBuilder();
        sb.append(aiPlayerX).append(",").append(aiPlayerY).append("|")
                .append(playerX).append(",").append(playerY).append("|")
                .append(bombCount).append("|");
        for (int[] row : gameMap) {
            for (int cell : row) {
                sb.append(cell);
            }
            sb.append("/");
        }
        // Thêm thông tin về bom vào hash
        for (Bomb bomb : bombs) {
            sb.append(bomb.getX()).append(",").append(bomb.getY()).append(",")
                    .append(bomb.getCountdown()).append("|");
        }
        return sb.toString();
    }

    // Phương thức clone sâu cho gameMap
    private int[][] deepCopyGameMap(int[][] original) {
        if (original == null) return null;
        int[][] copy = new int[original.length][];
        for(int i = 0; i < original.length; i++) {
            copy[i] = original[i].clone();
        }
        return copy;
    }

    // Phương thức clone sâu cho danh sách bom
    private List<Bomb> deepCopyBombs(List<Bomb> original) {
        if (original == null) return null;
        List<Bomb> copy = new ArrayList<>();
        for (Bomb bomb : original) {
            copy.add(bomb.clone()); // Giả sử Bomb có phương thức clone()
        }
        return copy;
    }

    // Phương thức equals và hashCode để sử dụng trong Transposition Table
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Node node = (Node) obj;
        return aiPlayerX == node.aiPlayerX &&
                aiPlayerY == node.aiPlayerY &&
                playerX == node.playerX &&
                playerY == node.playerY &&
                bombCount == node.bombCount &&
                explosionRange == node.explosionRange &&
                Objects.deepEquals(gameMap, node.gameMap) &&
                Objects.equals(bombs, node.bombs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aiPlayerX, aiPlayerY, playerX, playerY, bombCount, explosionRange, Arrays.deepHashCode(gameMap), bombs);
    }
}