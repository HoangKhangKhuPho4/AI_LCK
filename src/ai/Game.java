
// File: ai/Game.java
package ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Lớp đại diện cho trò chơi Bomberman.
 */
public class Game implements Cloneable, Subject {
    private Player player;
    private AIPlayer aiPlayer;
    private GameMap gameMap;
    private List<Bomb> bombs;
    private List<Balloon> balloons;
    private boolean gameOver;
    private boolean gameWon;
    private Random rand;
    private int level;
    private boolean isRunning;
    private boolean isPlayerTurn; // Biến quản lý lượt chơi

    // Danh sách các Observer
    private List<Observer> observers = new ArrayList<>();

    /**
     * Constructor khởi tạo trò chơi với bản đồ, người chơi, AIPlayer và các Balloon.
     */
    public Game() {
        gameMap = new GameMap(19, 19);
        player = new Player(10, 10);
        bombs = new ArrayList<>();
        isRunning = true; // Gán giá trị cho biến thành viên
        balloons = new ArrayList<>();
        gameOver = false;
        gameWon = false;
        rand = new Random();
        level = 1;
        isPlayerTurn = true; // Ban đầu là lượt người chơi
        initializeBalloons(3);
        initializeAIPlayer();
        System.out.println("Người chơi được khởi tạo tại (" + player.getX() + ", " + player.getY() + ")");
    }

    /**
     * Khởi tạo AIPlayer với chiến lược Minimax.
     */
    private void initializeAIPlayer() {
        // Sử dụng MinimaxStrategy với độ sâu 7, là Maximizing Player
        MovementStrategy aiStrategy = new MinimaxStrategy(7, true);
        aiPlayer = new AIPlayer(5, 5, aiStrategy, this);
    }

    /**
     * Kiểm tra xem trò chơi có kết thúc không.
     *
     * @return True nếu trò chơi kết thúc, ngược lại false.
     */
    public boolean isGameOver() {
        return gameOver;
    }

    /**
     * Kiểm tra xem người chơi có thắng không.
     *
     * @return True nếu người chơi thắng, ngược lại false.
     */
    public boolean isGameWon() {
        return gameWon;
    }

    /**
     * Kiểm tra xem trò chơi đã kết thúc chưa và thiết lập các cờ gameOver và gameWon.
     *
     * @return True nếu trò chơi đã kết thúc, ngược lại false.
     */
    private boolean isOver() {
        // Kiểm tra nếu người chơi hoặc AIPlayer đã không còn sống
        if (!player.isAlive()) {
            gameOver = true;
            gameWon = false;
            return true;
        }
        if (!aiPlayer.isAlive()) {
            gameOver = true;
            gameWon = true;
            return true;
        }

        // Kiểm tra nếu đạt được mục tiêu thắng (ví dụ: phá hủy tất cả Balloon)
        boolean allBalloonsDestroyed = true;
        for (Balloon balloon : balloons) {
            if (balloon.isAlive()) {
                allBalloonsDestroyed = false;
                break;
            }
        }
        if (allBalloonsDestroyed) {
            gameOver = true;
            gameWon = true;
            return true;
        }

        // Kiểm tra các điều kiện khác nếu có
        return false;
    }

    /**
     * Khởi tạo các Balloon với các chiến lược di chuyển khác nhau.
     *
     * @param count Số lượng Balloon cần khởi tạo.
     */
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
            int countdown = 30; // Đặt countdown là 30 để bom nổ sau 3 giây (tương ứng với 30 lượt)
            Bomb bomb = new Bomb(entity.getX(), entity.getY(), countdown, entity, entity.getExplosionRange());
            addBomb(bomb);
            System.out.println("Bom được đặt tại (" + bomb.getX() + ", " + bomb.getY() + ")");
        }
    }

    /**
     * Lấy danh sách các ô bị nổ của một bom
     *
     * @param bomb Bom cần lấy danh sách ô bị nổ
     * @return Danh sách các ô bị nổ
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
                if (tile == '#') {
                    // Gặp tường không phá hủy, ngừng lan
                    break;
                }
                explosionTiles.add(new int[]{tx, ty});
                if (tile == 'D') {
                    // Gặp tường phá hủy, thêm vào danh sách và ngừng lan
                    break;
                }
            }
        }
        return explosionTiles;
    }

    /**
     * Lấy bản đồ trò chơi
     */
    public GameMap getGameMap() {
        return gameMap;
    }

    /**
     * Lấy danh sách các bom hiện có
     */
    public List<Bomb> getBombs() {
        return bombs;
    }

    /**
     * Lấy danh sách Balloon hiện có
     */
    public List<Balloon> getBalloons() {
        return balloons;
    }

    /**
     * Lấy người chơi
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Lấy AIPlayer
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
        if (gameOver || gameWon) return;
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
        }
    }

    /**
     * Kiểm tra xem di chuyển đến vị trí (x, y) có hợp lệ không.
     *
     * @param x      Tọa độ X mới.
     * @param y      Tọa độ Y mới.
     * @param entity Thực thể đang di chuyển.
     * @return True nếu di chuyển hợp lệ, ngược lại false.
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
     *
     * @param bomb Bom cần thêm.
     */
    public void addBomb(Bomb bomb) {
        bombs.add(bomb);
        // Đăng ký các thực thể quan tâm đến sự kiện bom nổ
        bomb.attach(player);
        bomb.attach(aiPlayer);
        for (Balloon balloon : balloons) {
            bomb.attach(balloon);
        }
    }

    /**
     * Lấy vật phẩm tại vị trí (x, y)
     *
     * @param x Tọa độ X của vật phẩm.
     * @param y Tọa độ Y của vật phẩm.
     * @return Vật phẩm tại vị trí đó hoặc null nếu không có.
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
     * Áp dụng hiệu ứng của vật phẩm lên người chơi.
     *
     * @param item   Vật phẩm được nhặt.
     * @param player Người chơi nhận hiệu ứng.
     */
    private void applyItemEffect(Item item, Player player) {
        if (item.getType() == Item.ItemType.SPEED) {
            player.increaseSpeed();
        } else if (item.getType() == Item.ItemType.EXPLOSION_RANGE) {
            player.increaseExplosionRange();
        }
    }

    /**
     * Phương thức clone để tạo bản sao sâu của trò chơi.
     */
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
            cloned.observers = new ArrayList<>(); // Không clone các Observer
            return cloned;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Lấy mã hash của trạng thái trò chơi.
     *
     * @return Chuỗi hash đại diện cho trạng thái trò chơi.
     */
    public String getStateHash() {
        StringBuilder hash = new StringBuilder();
        hash.append(player.getX()).append(player.getY());
        for (Balloon balloon : balloons) {
            hash.append(balloon.getX()).append(balloon.getY());
        }
        return hash.toString();
    }

    /**
     * Đặt vị trí cho AIPlayer.
     *
     * @param x Tọa độ X mới.
     * @param y Tọa độ Y mới.
     */
    public void setAiPlayerPosition(int x, int y) {
        if (aiPlayer != null) {
            aiPlayer.setX(x);
            aiPlayer.setY(y);
        }
    }

    /**
     * Đặt tọa độ X cho AIPlayer.
     *
     * @param x Tọa độ X mới.
     */
    public void setAIPlayerX(int x) {
        if (aiPlayer != null) {
            aiPlayer.setX(x);
        }
    }

    /**
     * Đặt tọa độ Y cho AIPlayer.
     *
     * @param y Tọa độ Y mới.
     */
    public void setAIPlayerY(int y) {
        if (aiPlayer != null) {
            aiPlayer.setY(y);
        }
    }

    /**
     * Đặt tọa độ X cho người chơi.
     *
     * @param x Tọa độ X mới.
     */
    public void setPlayerX(int x) {
        if (player != null) {
            player.setX(x);
        }
    }

    /**
     * Đặt tọa độ Y cho người chơi.
     *
     * @param y Tọa độ Y mới.
     */
    public void setPlayerY(int y) {
        if (player != null) {
            player.setY(y);
        }
    }

    /**
     * Đặt số lượng bom cho người chơi.
     *
     * @param count Số lượng bom mới.
     */
    public void setBombCount(int count) {
        if (player != null) {
            player.setBombCount(count);
        }
    }

    /**
     * Đặt lại bản đồ trò chơi.
     *
     * @param gameMapData Mảng 2 chiều đại diện cho bản đồ mới.
     */
    public void setGameMap(int[][] gameMapData) {
        if (gameMap != null) {
            gameMap.setMap(gameMapData);
        }
    }

    /**
     * Đặt danh sách bom mới.
     *
     * @param bombs Danh sách bom mới.
     */
    public void setBombs(List<Bomb> bombs) {
        this.bombs = new ArrayList<>(bombs);
    }

    /**
     * Đặt lại số lượng Balloon.
     *
     * @param count Số lượng Balloon mới.
     */
    public void setBalloonCount(int count) {
        if (balloons != null) {
            balloons.clear();
            initializeBalloons(count);
        }
    }

    /**
     * Đặt trạng thái kết thúc trò chơi.
     *
     * @param b Trạng thái kết thúc trò chơi.
     */
    public void setGameOver(boolean b) {
        this.gameOver = b;
    }

    /**
     * Phương thức người chơi thực hiện lượt di chuyển hoặc đặt bom.
     *
     * @param action Hành động của người chơi.
     */
    public void playerMove(Action action) {
        if (isPlayerTurn && player.isAlive()) {
            executeAction(player, action);
            updateGame(); // Cập nhật bom và thực thể sau hành động của người chơi
            isPlayerTurn = false;
            notifyObservers(new PlayerMovedEvent(player.getX(), player.getY()));
            // Sau khi người chơi di chuyển, gọi lượt của AI
            aiMove();
        }
    }

    /**
     * Thực hiện hành động trên một thực thể trong trò chơi.
     *
     * @param entity Thực thể cần thực hiện hành động.
     * @param action Hành động cần thực hiện.
     */
    public void executeAction(Entity entity, Action action) {
        switch (action.getActionType()) {
            case MOVE_UP:
                moveEntity(entity, 0, -1);
                break;
            case MOVE_DOWN:
                moveEntity(entity, 0, 1);
                break;
            case MOVE_LEFT:
                moveEntity(entity, -1, 0);
                break;
            case MOVE_RIGHT:
                moveEntity(entity, 1, 0);
                break;
            case PLACE_BOMB:
                placeBomb(entity);
                break;
            case STAY:
                // Không thực hiện gì
                break;
        }
    }

    /**
     * AIPlayer thực hiện lượt của mình.
     */
    public void aiMove() {
        if (!isPlayerTurn && aiPlayer.isAlive()) {
            aiPlayer.update(this); // AIPlayer thực hiện hành động
            updateGame(); // Cập nhật bom và thực thể sau hành động của AI
            isPlayerTurn = true;
            notifyObservers(new AIPlayerMovedEvent(aiPlayer.getX(), aiPlayer.getY()));
        }
    }

    /**
     * Triển khai các phương thức của giao diện Subject
     */
    @Override
    public void attach(Observer observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void detach(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(Event event) {
        for (Observer observer : observers) {
            observer.update(event);
        }
    }

    /**
     * Cập nhật trạng thái của các bom trong trò chơi.
     */
    public void updateBombs() {
        List<Bomb> explodedBombs = new ArrayList<>();
        for (Bomb bomb : bombs) {
            bomb.tick(this);
            if (bomb.isExplosionFinished()) {
                explodedBombs.add(bomb);
            }
        }
        // Loại bỏ các bom đã hoàn thành hiệu ứng nổ
        bombs.removeAll(explodedBombs);
    }

    /**
     * Cập nhật các thực thể trong trò chơi như Balloon và AIPlayer.
     */
    public void updateEntities() {
        for (Balloon balloon : balloons) {
            balloon.update(this);
        }
        if (aiPlayer.isAlive()) {
            aiPlayer.update(this);
        }
        // Thêm các thực thể khác nếu cần
    }

    /**
     * Cập nhật trạng thái tổng thể của trò chơi, bao gồm các bom và thực thể.
     */
    public void updateGame() {
        updateBombs();
        updateEntities();
        // Kiểm tra điều kiện kết thúc trò chơi
        if (isOver()) {
            gameOver = true;
            if (gameWon) {
                System.out.println("Bạn đã thắng!");
            } else {
                System.out.println("Game Over!");
            }
        }
    }

    // Trong lớp Game.java

    /**
     * Kiểm tra xem thực thể có thể thoát khỏi vùng nổ sau khi đặt bom tại (bombX, bombY) không.
     *
     * @param entity Thực thể đặt bom.
     * @param bombX  Tọa độ X của bom.
     * @param bombY  Tọa độ Y của bom.
     * @return True nếu có đường thoát an toàn, false nếu không.
     */
    public boolean canEscape(Entity entity, int bombX, int bombY) {
        // Clone trò chơi để mô phỏng sau khi đặt bom
        Game clonedGame = this.clone();
        if (clonedGame == null) return false;

        // Đặt bom trong trò chơi clone
        clonedGame.placeBomb(entity);

        // Giả sử bạn có một chiến lược di chuyển để tìm đường thoát
        // Sử dụng Pathfinding để kiểm tra xem có đường thoát an toàn nào không
        Pathfinding pathfinding = new Pathfinding(clonedGame.getGameMap());
        List<int[]> path = pathfinding.findSafePath(entity.getX(), entity.getY(), entity.getX(), entity.getY(), clonedGame);

        return !path.isEmpty();
    }

}
