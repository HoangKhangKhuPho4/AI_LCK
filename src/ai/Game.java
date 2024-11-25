// File: ai/Game.java
package ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
        MovementStrategy aiStrategy = new ChasePlayerStrategy();
        aiPlayer = new AIPlayer(5, 5, aiStrategy);
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
                        strategy = new ChasePlayerStrategy();
                        break;
                    case 2:
                        // Ví dụ đường tuần tra đơn giản
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

    // File: ai/Game.java

    public void placeBomb() {
        // Kiểm tra xem người chơi đã đặt bom tại vị trí này chưa
        for (Bomb bomb : bombs) {
            if (bomb.getX() == player.getX() && bomb.getY() == player.getY()) {
                return; // Đã có bom tại vị trí này
            }
        }
        if (player.placeBomb()) {
            int countdown = 30; // Đặt countdown là 30 để bom nổ sau 3 giây
            Bomb bomb = new Bomb(player.getX(), player.getY(), countdown, player, player.getExplosionRange());
            addBomb(bomb);
        }
    }


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

        // Cập nhật các thực thể
        for (Balloon balloon : balloons) {
            if (balloon.isAlive()) {
                balloon.update(this);
            }
        }

        if (aiPlayer.isAlive()) {
            aiPlayer.update(this);
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
     * Kiểm tra trạng thái trò chơi 
     */
    public boolean isGameOver() {
        return gameOver || gameWon;
    }

    /**
     * Kiểm tra người chơi có thắng không 
     */
    public boolean isGameWon() {
        return gameWon;
    }

    /**
     * Lấy danh sách các ô bị nổ của một bom 
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
    public AIPlayer getAIPlayer() {
        return aiPlayer;
    }

    /**
     * Di chuyển người chơi 
     */
    public void movePlayer(int dx, int dy) {
        if (gameOver || gameWon) return;
        int newX = player.getX() + dx;
        int newY = player.getY() + dy;
        if (newX >= 0 && newX < gameMap.getWidth() && newY >= 0 && newY < gameMap.getHeight()) {
            char tile = gameMap.getTile(newX, newY);
            boolean blocked = false;
            // Kiểm tra va chạm với Balloon
            for (Balloon balloon : balloons) {
                if (balloon.isAlive() && balloon.getX() == newX && balloon.getY() == newY) {
                    blocked = true;
                    break;
                }
            }
            // Kiểm tra va chạm với AIPlayer
            if (aiPlayer.isAlive() && aiPlayer.getX() == newX && aiPlayer.getY() == newY) {
                blocked = true;
            }
            if (tile == ' ' && !blocked) {
                player.setX(newX);
                player.setY(newY);
                // Kiểm tra vật phẩm
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
     * Thêm bom vào danh sách bom và đăng ký Observer 
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
} 