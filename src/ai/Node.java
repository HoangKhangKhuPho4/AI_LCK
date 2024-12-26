
package ai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Lớp đại diện cho một trạng thái trò chơi trong thuật toán Minimax.
 */
public class Node implements Cloneable {
    // Vị trí của AIPlayer
    private boolean justPlacedBomb;
    private int aiPlayerX;
    private int aiPlayerY;

    // Vị trí của Player
    private int playerX;
    private int playerY;

    // Số lượng bom còn lại của AIPlayer
    private int bombCount;
    private int playerBombCount;

    // Bản đồ trò chơi: 0 - ô trống, 1 - tường không phá hủy, 2 - tường phá hủy
    private int[][] gameMap;

    // Danh sách bom hiện có trên bản đồ
    private List<Bomb> bombs;

    // Phạm vi nổ của AIPlayer
    private int explosionRange;

    // Biến quản lý lượt trong Minimax
    private boolean isAiTurn;

    // Thuộc tính cho thuật toán Minimax
    private double heuristicValue; // Giá trị heuristic của node
    private Node parent;           // Node cha (nếu cần)
    private double gScore;         // Chi phí từ node bắt đầu đến node này
    private double fScore;         // Tổng của gScore và heuristicValue

    /**
     * Constructor để khởi tạo Node với đầy đủ thông tin, bao gồm cả thông tin lượt chơi.
     *
     * @param aiPlayerX        Tọa độ X của AIPlayer.
     * @param aiPlayerY        Tọa độ Y của AIPlayer.
     * @param playerX          Tọa độ X của Player.
     * @param playerY          Tọa độ Y của Player.
     * @param bombCount        Số lượng bom mà AIPlayer có thể đặt.
     * @param gameMap          Bản đồ trò chơi.
     * @param bombs            Danh sách bom hiện tại trên bản đồ.
     * @param explosionRange   Phạm vi nổ của AIPlayer.
     * @param isAiTurn         Biến quản lý lượt trong Minimax (true nếu là lượt AIPlayer, false nếu là lượt Player).
     */
    public Node(int aiPlayerX, int aiPlayerY, int playerX, int playerY, int bombCount,
                int[][] gameMap, List<Bomb> bombs, int explosionRange, boolean isAiTurn) {
        this.aiPlayerX = aiPlayerX;
        this.aiPlayerY = aiPlayerY;
        this.playerX = playerX;
        this.playerY = playerY;
        this.bombCount = bombCount;
        this.gameMap = deepCopyGameMap(gameMap);
        this.bombs = deepCopyBombs(bombs);
        this.explosionRange = explosionRange;
        this.isAiTurn = isAiTurn;
        this.heuristicValue = 0.0;
        this.parent = null;
        this.gScore = Double.MAX_VALUE;
        this.fScore = Double.MAX_VALUE;
    }


    public void setJustPlacedBomb(boolean justPlacedBomb) {
        this.justPlacedBomb = justPlacedBomb;
    }


    // Getter và Setter cho playerBombCount
    public int getPlayerBombCount() { return playerBombCount; }
    public void setPlayerBombCount(int playerBombCount) { this.playerBombCount = playerBombCount; }
    /**
     * Constructor để khởi tạo Node mà không cần thông tin về bom và phạm vi nổ (sử dụng giá trị mặc định).
     *
     * @param aiPlayerX Tọa độ X của AIPlayer.
     * @param aiPlayerY Tọa độ Y của AIPlayer.
     * @param playerX   Tọa độ X của Player.
     * @param playerY   Tọa độ Y của Player.
     * @param bombCount Số lượng bom mà AIPlayer có thể đặt.
     * @param gameMap   Bản đồ trò chơi.
     * @param bombs     Danh sách bom hiện tại trên bản đồ.
     * @param isAiTurn  Biến quản lý lượt trong Minimax.
     */
    public Node(int aiPlayerX, int aiPlayerY, int playerX, int playerY, int bombCount,
                int[][] gameMap, List<Bomb> bombs, boolean isAiTurn) {
        this(aiPlayerX, aiPlayerY, playerX, playerY, bombCount, gameMap, bombs, 1, isAiTurn);
    }

    // Getter và Setter cho các thuộc tính

    public int getAiPlayerX() {
        return aiPlayerX;
    }

    public void setAiPlayerX(int aiPlayerX) {
        this.aiPlayerX = aiPlayerX;
    }

    public int getAiPlayerY() {
        return aiPlayerY;
    }

    public void setAiPlayerY(int aiPlayerY) {
        this.aiPlayerY = aiPlayerY;
    }

    public int getPlayerX() {
        return playerX;
    }

    public void setPlayerX(int playerX) {
        this.playerX = playerX;
    }

    public int getPlayerY() {
        return playerY;
    }

    public void setPlayerY(int playerY) {
        this.playerY = playerY;
    }

    public int getBombCount() {
        return bombCount;
    }

    public void setBombCount(int bombCount) {
        this.bombCount = bombCount;
    }

    public int[][] getGameMap() {
        return deepCopyGameMap(gameMap);
    }

    public void setGameMap(int[][] gameMap) {
        this.gameMap = deepCopyGameMap(gameMap);
    }

    public List<Bomb> getBombs() {
        return deepCopyBombs(bombs);
    }

    public void setBombs(List<Bomb> bombs) {
        this.bombs = deepCopyBombs(bombs);
    }

    public int getExplosionRange() {
        return explosionRange;
    }

    public void setExplosionRange(int explosionRange) {
        this.explosionRange = explosionRange;
    }

    public boolean isAiTurn() {
        return isAiTurn;
    }

    public void setAiTurn(boolean isAiTurn) {
        this.isAiTurn = isAiTurn;
    }

    public double getHeuristicValue() {
        return heuristicValue;
    }

    public void setHeuristicValue(double heuristicValue) {
        this.heuristicValue = heuristicValue;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public double getGScore() {
        return gScore;
    }

    public void setGScore(double gScore) {
        this.gScore = gScore;
    }

    public double getFScore() {
        return fScore;
    }

    public void setFScore(double fScore) {
        this.fScore = fScore;
    }

    /**
     * Tạo một chuỗi hash duy nhất đại diện cho trạng thái của Node.
     *
     * @return Chuỗi hash đại diện cho trạng thái của Node.
     */
    public String getStateHash() {
        StringBuilder sb = new StringBuilder();
        sb.append(aiPlayerX).append(",").append(aiPlayerY).append("|")
                .append(playerX).append(",").append(playerY).append("|")
                .append(bombCount).append("|");

        // Chuyển đổi gameMap thành chuỗi
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

        // Thêm thông tin lượt chơi
        sb.append(isAiTurn ? "AI|" : "Player|");

        return sb.toString();
    }

    /**
     * Phương thức deep copy cho gameMap.
     *
     * @param original Bản đồ gốc cần sao chép.
     * @return Bản đồ sao chép sâu.
     */
    private int[][] deepCopyGameMap(int[][] original) {
        if (original == null) return null;
        int[][] copy = new int[original.length][];
        for(int i = 0; i < original.length; i++) {
            copy[i] = original[i].clone();
        }
        return copy;
    }

    /**
     * Phương thức deep copy cho danh sách bom.
     *
     * @param original Danh sách bom gốc cần sao chép.
     * @return Danh sách bom sao chép sâu.
     */
    private List<Bomb> deepCopyBombs(List<Bomb> original) {
        if (original == null) return null;
        List<Bomb> copy = new ArrayList<>();
        for (Bomb bomb : original) {
            copy.add(bomb.clone()); // Giả sử Bomb có phương thức clone()
        }
        return copy;
    }

    /**
     * Phương thức clone để tạo bản sao sâu của Node.
     *
     * @return Bản sao của Node.
     */
    @Override
    public Node clone() {
        try {
            Node cloned = (Node) super.clone();
            cloned.gameMap = deepCopyGameMap(this.gameMap);
            cloned.bombs = deepCopyBombs(this.bombs);
            cloned.parent = (this.parent != null) ? this.parent.clone() : null;
            cloned.justPlacedBomb = this.justPlacedBomb;
            return cloned;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Phương thức equals để so sánh hai Node dựa trên trạng thái của chúng.
     *
     * @param obj Đối tượng cần so sánh.
     * @return true nếu hai Node có cùng trạng thái, ngược lại false.
     */
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
                isAiTurn == node.isAiTurn &&
                Objects.deepEquals(gameMap, node.gameMap) &&
                Objects.equals(bombs, node.bombs);
    }

    /**
     * Phương thức hashCode để hỗ trợ việc sử dụng Node trong các cấu trúc dữ liệu như HashMap.
     *
     * @return Giá trị hash của Node.
     */
    @Override
    public int hashCode() {
        return Objects.hash(aiPlayerX, aiPlayerY, playerX, playerY, bombCount, explosionRange, isAiTurn, Arrays.deepHashCode(gameMap), bombs);
    }


    public int getAIPlayerX() {
        return aiPlayerX;
    }

    public int getAIPlayerY() {
        return aiPlayerY;
    }

    public boolean hasJustPlacedBomb() {
        return justPlacedBomb;
    }


}
