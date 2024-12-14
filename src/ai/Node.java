package ai;

public class Node {
    private int aiPlayerX;
    private int aiPlayerY;
    private int playerX;
    private int playerY;
    private int bombCount;
    private int[][] gameMap; // Bản đồ trò chơi, 0 là ô trống, 1 là chướng ngại vật, 2 là bom

    // Constructor để khởi tạo Node
    public Node(int aiPlayerX, int aiPlayerY, int playerX, int playerY, int bombCount, int[][] gameMap) {
        this.aiPlayerX = aiPlayerX;
        this.aiPlayerY = aiPlayerY;
        this.playerX = playerX;
        this.playerY = playerY;
        this.bombCount = bombCount;
        this.gameMap = gameMap; // Lưu trạng thái bản đồ
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
        return gameMap;
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

    // Phương thức trả về một đối tượng Game
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

        return game;
    }
}
