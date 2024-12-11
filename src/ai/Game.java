package ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Lớp đại diện cho trò chơi Bomberman.
 */
public class Game implements Cloneable {
    private Player player;
    private AIPlayer aiPlayer;
    private GameMap gameMap;
    private List<Bomb> bombs;
    private List<Balloon> balloons;
    private boolean gameOver;
    private boolean gameWon;
    private Random rand;
    private int level;

    // Biến để xác định lượt hiện tại: true - người chơi, false - AI
    private boolean isPlayerTurn = true;

    public Game() {
        gameMap = new GameMap(20, 20);
        player = new Player(10, 10);
        bombs = new ArrayList<>();
        balloons = new ArrayList<>();
        gameOver = false;
        gameWon = false;
        rand = new Random();
        level = 1;
        initializeBalloons(3);
        initializeAIPlayer();
        System.out.println("Người chơi được khởi tạo tại (" + player.getX() + ", " + player.getY() + ")");
    }

    private void initializeAIPlayer() {
        // Sử dụng MinimaxStrategy với độ sâu 7, là Maximizing Player
        MovementStrategy aiStrategy = new MinimaxStrategy(7, true);
        aiPlayer = new AIPlayer(5, 5, aiStrategy, this);
    }

    private void initializeBalloons(int count) {
        int placed = 0;
        while (placed < count) {
            int x = rand.nextInt(gameMap.getWidth());
            int y = rand.nextInt(gameMap.getHeight());
            if (gameMap.getTile(x, y) == ' ' && !(x == player.getX() && y == player.getY())) {
                MovementStrategy strategy;
                switch (placed % 3) {
                    case 0:
                        strategy = new RandomMovementStrategy();
                        break;
                    case 1:
                        strategy = new ChasePlayerStrategy(gameMap);
                        break;
                    case 2:
                        // Đường tuần tra đơn giản
                        List<int[]> patrolPath = new ArrayList<>();
                        patrolPath.add(new int[]{x, y});
                        patrolPath.add(new int[]{x + 1, y});
                        patrolPath.add(new int[]{x + 1, y + 1});
                        patrolPath.add(new int[]{x, y + 1});
                        strategy = new PatrolStrategy(patrolPath);
                        break;
                    default:
                        strategy = new RandomMovementStrategy();
                }
                Balloon balloon = new Balloon(x, y, strategy);
                balloons.add(balloon);
                placed++;
            }
        }
        System.out.println(count + " Balloon đã được khởi tạo.");
    }


    // Trong Game.java
    public boolean isPlayerTurn() {
        return isPlayerTurn;
    }

    /**
     * Đặt bom cho một thực thể cụ thể.
     *
     * @param entity Thực thể đặt bom.
     */
    public void placeBomb(Entity entity) {
        // Kiểm tra xem đã có bom tại vị trí này chưa
        for (Bomb bomb : bombs) {
            if (bomb.getX() == entity.getX() && bomb.getY() == entity.getY()) {
                return; // Đã có bom tại vị trí này
            }
        }
        if (entity.placeBomb()) {
            int countdown = 30; // Đặt countdown là 30 để bom nổ sau 3 giây
            Bomb bomb = new Bomb(entity.getX(), entity.getY(), countdown, entity, entity.getExplosionRange());
            addBomb(bomb);
            System.out.println("Bom được đặt tại (" + bomb.getX() + ", " + bomb.getY() + ")");
        }
    }

    /**
     * Cập nhật trạng thái trò chơi.
     * Phương thức này được gọi sau mỗi lượt đi của người chơi hoặc AI.
     */
    public void update() {
        if (gameOver || gameWon) return;
        // Cập nhật bom
        for (int i = bombs.size() - 1; i >= 0; i--) {
            Bomb bomb = bombs.get(i);
            bomb.tick(this);
            if (bomb.isExplosionFinished()) {
                bomb.getOwner().increaseBombCount();
                bombs.remove(i);
            }
        }
        // Cập nhật các Balloon
        for (Balloon balloon : balloons) {
            if (balloon.isAlive()) {
                balloon.update(this);
            }
        }
        // Kiểm tra trạng thái của người chơi
        if (!player.isAlive()) {
            gameOver = true;
            System.out.println("Người chơi đã chết. Game Over!");
        }
        // Kiểm tra trạng thái của AIPlayer
        if (!aiPlayer.isAlive()) {
            gameWon = true;
            System.out.println("AIPlayer đã chết. Bạn thắng!");
        }
    }

    /**
     * Xử lý lượt đi của người chơi.
     *
     * @param action Hành động của người chơi.
     */
    public void playerMove(Action action) {
        if (isGameOver()) return;
        executePlayerAction(action);
        update(); // Cập nhật sau khi người chơi di chuyển
        isPlayerTurn = false; // Chuyển sang lượt AI
    }

    /**
     * Xử lý lượt đi của AI.
     */
    public void aiMove() {
        if (isGameOver()) return;
        aiPlayer.update(this); // AI thực hiện lượt đi
        update(); // Cập nhật sau khi AI di chuyển
        isPlayerTurn = true; // Chuyển sang lượt người chơi
    }

    /**
     * Kiểm tra trạng thái trò chơi.
     *
     * @return true nếu trò chơi kết thúc, false nếu còn tiếp tục.
     */
    public boolean isGameOver() {
        return gameOver || gameWon;
    }

    /**
     * Kiểm tra người chơi có thắng không.
     *
     * @return true nếu người chơi thắng, false nếu chưa.
     */
    public boolean isGameWon() {
        return gameWon;
    }

    /**
     * Lấy danh sách các ô bị nổ của một bom.
     */
    public List<int[]> getExplosionTiles(Bomb bomb) {
        List<int[]> explosionTiles = new ArrayList<>();
        explosionTiles.add(new int[]{bomb.getX(), bomb.getY()});
        int[][] directions = {
                {0, -1},
                {0, 1},
                {-1, 0},
                {1, 0}
        };
        for (int[] dir : directions) {
            for (int i = 1; i <= bomb.getExplosionRange(); i++) {
                int tx = bomb.getX() + dir[0] * i;
                int ty = bomb.getY() + dir[1] * i;
                char tile = gameMap.getTile(tx, ty);
                if (tile == '#') { // Tường không phá hủy
                    break;
                }
                explosionTiles.add(new int[]{tx, ty});
                if (tile == 'D') { // Tường phá hủy
                    break;
                }
            }
        }
        return explosionTiles;
    }

    /**
     * Lấy bản đồ trò chơi.
     */
    public GameMap getGameMap() {
        return gameMap;
    }

    /**
     * Lấy danh sách các bom hiện có.
     */
    public List<Bomb> getBombs() {
        return bombs;
    }

    /**
     * Lấy danh sách Balloon hiện có.
     */
    public List<Balloon> getBalloons() {
        return balloons;
    }

    /**
     * Lấy người chơi.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Lấy AIPlayer.
     */
    public AIPlayer getAiPlayer() {
        return aiPlayer;
    }

    /**
     * Di chuyển một thực thể trong trò chơi.
     *
     * @param entity Thực thể cần di chuyển.
     * @param dx     Số ô di chuyển theo trục X.
     * @param dy     Số ô di chuyển theo trục Y.
     */
    public void moveEntity(Entity entity, int dx, int dy) {
        if (isGameOver()) return;
        int newX = entity.getX() + dx;
        int newY = entity.getY() + dy;
        if (isValidMove(newX, newY, entity)) {
            entity.setX(newX);
            entity.setY(newY);
            // Kiểm tra vật phẩm nếu di chuyển người chơi
            if (entity instanceof Player) {
                Player player = (Player) entity;
                Item item = getItemAt(newX, newY);
                if (item != null) {
                    applyItemEffect(item, player);
                    gameMap.removeItem(item);
                    System.out.println("Người chơi đã nhặt vật phẩm: " + item.getType());
                }
            }
            // Kiểm tra va chạm với Balloon
            for (Balloon balloon : balloons) {
                if (balloon.isAlive() && balloon.getX() == newX && balloon.getY() == newY) {
                    balloon.setAlive(false);
                    System.out.println("Balloon tại (" + newX + ", " + newY + ") đã bị tiêu diệt!");
                }
            }
        }
    }

    /**
     * Kiểm tra xem di chuyển đến vị trí (x, y) có hợp lệ không.
     */
    private boolean isValidMove(int x, int y, Entity entity) {
        if (x < 0 || x >= gameMap.getWidth() || y < 0 || y >= gameMap.getHeight()) {
            return false;
        }
        char tile = gameMap.getTile(x, y);
        if (tile != ' ') {
            return false;
        }
        // Kiểm tra va chạm với các thực thể khác
        for (Balloon balloon : balloons) {
            if (balloon.isAlive() && balloon.getX() == x && balloon.getY() == y) {
                return false;
            }
        }
        if (aiPlayer.isAlive() && aiPlayer.getX() == x && aiPlayer.getY() == y) {
            return false;
        }
        return true;
    }

    /**
     * Thêm bom vào danh sách bom và đăng ký Observer.
     */
    public void addBomb(Bomb bomb) {
        bombs.add(bomb);
        // Đăng ký các thực thể quan tâm đến sự kiện bom nổ
        bomb.attach(player);
        bomb.attach(aiPlayer); // Thêm AIPlayer vào Observer
        for (Balloon balloon : balloons) {
            bomb.attach(balloon);
        }
    }

    /**
     * Lấy vật phẩm tại vị trí (x, y)
     */
    private Item getItemAt(int x, int y) {
        for (Item item : gameMap.getItems()) {
            if (item.getX() == x && item.getY() == y) {
                return item;
            }
        }
        return null;
    }

    /**
     * Áp dụng hiệu ứng của vật phẩm
     */
    private void applyItemEffect(Item item, Player player) {
        if (item.getType() == Item.ItemType.SPEED) {
            player.increaseSpeed();
        } else if (item.getType() == Item.ItemType.EXPLOSION_RANGE) {
            player.increaseExplosionRange();
        }
    }

    /**
     * Thực hiện hành động của người chơi.
     *
     * @param action Hành động của người chơi.
     */
    private void executePlayerAction(Action action) {
        switch (action.getActionType()) {
            case MOVE_UP:
                moveEntity(player, 0, -1);
                System.out.println("Người chơi di chuyển lên.");
                break;
            case MOVE_DOWN:
                moveEntity(player, 0, 1);
                System.out.println("Người chơi di chuyển xuống.");
                break;
            case MOVE_LEFT:
                moveEntity(player, -1, 0);
                System.out.println("Người chơi di chuyển trái.");
                break;
            case MOVE_RIGHT:
                moveEntity(player, 1, 0);
                System.out.println("Người chơi di chuyển phải.");
                break;
            case PLACE_BOMB:
                placeBomb(player);
                break;
            case STAY:
                System.out.println("Người chơi ở lại.");
                break;
        }
    }

    @Override
    public Game clone() {
        try {
            Game cloned = (Game) super.clone();
            cloned.gameMap = this.gameMap.clone();
            cloned.player = this.player.clone();
            cloned.aiPlayer = this.aiPlayer.clone();
            cloned.bombs = new ArrayList<>();
            for (Bomb bomb : this.bombs) {
                cloned.bombs.add(bomb.clone());
            }
            cloned.balloons = new ArrayList<>();
            for (Balloon balloon : this.balloons) {
                cloned.balloons.add(balloon.clone());
            }
            return cloned;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Lấy khóa trạng thái cho Transposition Table.
     */
    public String getStateHash() {
        StringBuilder hash = new StringBuilder();
        hash.append(player.getX()).append(",").append(player.getY()).append(",");
        hash.append(aiPlayer.getX()).append(",").append(aiPlayer.getY()).append(",");
        hash.append(bombs.size()).append(",");
        hash.append(balloons.size());
        return hash.toString();
    }

    /**
     * Đặt vị trí AIPlayer (chỉ để sử dụng trong phương thức MinimaxStrategy).
     */
    public void setAiPlayerPosition(int x, int y) {
        if (aiPlayer != null) {
            aiPlayer.setX(x);
            aiPlayer.setY(y);
        }
    }

    /**
     * Kiểm tra xem AIPlayer có bị dồn vào ngõ cụt không.
     */
    private boolean isCornered(AIPlayer aiPlayer, GameMap map) {
        int x = aiPlayer.getX();
        int y = aiPlayer.getY();
        int walkable = 0;
        int[][] directions = {
                {0, -1}, // Lên
                {0, 1},  // Xuống
                {-1, 0}, // Trái
                {1, 0}   // Phải
        };
        for (int[] dir : directions) {
            int newX = x + dir[0];
            int newY = y + dir[1];
            if (map.isWalkable(newX, newY)) {
                walkable++;
            }
        }
        return walkable <= 1; // Nếu có ít hơn hoặc bằng 1 hướng đi, coi như bị dồn vào ngõ cụt
    }
}
