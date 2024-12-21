
package ai;
import java.util.*;
public class AIPlayer extends Entity implements Cloneable, Subject {
    private Game game;
    private MovementStrategy movementStrategy;
    public int ticksUntilMove;
    public int moveDelay;
    private int explosionRange = 1;
    private List<Observer> observers = new ArrayList<>();
    private boolean isExecuting = false;
    private List<AIState> stateHistory = new ArrayList<>();
    private int speed = 1;    // Nếu bạn muốn AI cũng có speed

    public AIPlayer(int startX, int startY, MovementStrategy strategy, Game game) {
        this.x = startX;
        this.y = startY;
        this.movementStrategy = strategy;
        this.moveDelay = 3;
        this.ticksUntilMove = moveDelay;
        this.game = game;
        this.bombCount = 1; // Khởi tạo số bom ban đầu
    }
    private int escapeAttempts = 0;
    private final int MAX_ESCAPE_ATTEMPTS = 3;
    @Override
    public void update(Game game) {
        // 1) Nếu chưa đến lượt AI => thoát
        if (!game.isAiTurn) {
            return;
        }

        // 2) Kiểm tra AI còn sống
        if (!alive) {
            return;
        }

        // 3) Logic "đếm thời gian" (nếu có)
        ticksUntilMove--;
        if (ticksUntilMove > 0) {
            return; // Chờ thêm
        }
        ticksUntilMove = moveDelay;

        // 4) Gọi chiến lược di chuyển (Minimax/EscapeBombs/...)
        getMovementStrategy().move(this, game);

        System.out.println("AIPlayer di chuyển trong lượt AI.");
    }

    private boolean isCornered(AIPlayer aiPlayer, GameMap map) {
        int x = aiPlayer.getX();
        int y = aiPlayer.getY();
        int walkable = 0;
        int[][] directions = { {0, -1}, {0, 1}, {-1, 0}, {1, 0} };
        for (int[] dir : directions) {
            int newX = x + dir[0];
            int newY = y + dir[1];
            if (map.isWalkable(newX, newY)) {
                walkable++;
            }
        }
        return walkable <= 1; // Nếu có ít hơn hoặc bằng 1 hướng đi
    }
    public void setExecuting(boolean executing) {
        this.isExecuting = executing;
    }
    @Override
    protected int getExplosionRange() {
        return explosionRange;
    }
    public void predictPlayerActions(Game game) {
        Player player = game.getPlayer();
        int distance = Math.abs(player.getX() - x) + Math.abs(player.getY() - y);
        if (distance <= 3) {
            System.out.println("Dự đoán: Người chơi có thể đặt bom gần!");
        }
    }
    public void predictBombs(Game game) {
        for (Bomb bomb : game.getBombs()) {
            if (!bomb.isExploded()) {
                int countdown = bomb.getCountdown();
                if (countdown <= 5) {
                    System.out.println("Cảnh báo: Tránh xa bom sắp nổ tại (" + bomb.getX() + "," + bomb.getY() + ")");
                }
            }
        }
    }
    public void scanForHazards(Game game, int radius) {
        List<int[]> visibleArea = getVisibleArea(radius);
        for (int[] coord : visibleArea) {
            int newX = coord[0];
            int newY = coord[1];
            for (Bomb bomb : game.getBombs()) {
                int distance = Math.abs(bomb.getX() - newX) + Math.abs(bomb.getY() - newY);
                if (distance <= bomb.getExplosionRange()) {
                    System.out.println("Bom gần ở: " + newX + "," + newY);
                }
            }
            for (Balloon balloon : game.getBalloons()) {
                int distance = Math.abs(balloon.getX() - newX) + Math.abs(balloon.getY() - newY);
                if (distance <= 2) {
                    System.out.println("Balloon gần ở: " + newX + "," + newY);
                }
            }
        }
    }
    public void recordState(Game game) {
        AIState state = new AIState(x, y, bombCount, game.getBalloons().size());
        stateHistory.add(state);
        System.out.println("Lưu trạng thái AI tại (" + x + ", " + y + ")");
    }
    public void updateStrategy() {
        System.out.println("Cập nhật chiến lược dựa trên lịch sử trạng thái.");
        // Logic cập nhật chiến lược có thể thêm vào đây
    }
    public List<int[]> getVisibleArea(int radius) {
        List<int[]> visibleArea = new ArrayList<>();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                int newX = x + dx;
                int newY = y + dy;
                if (game.getGameMap().isValidCoordinate(newX, newY)) {
                    visibleArea.add(new int[]{newX, newY});
                }
            }
        }
        return visibleArea;
    }
    public void setMovementStrategy(MovementStrategy strategy) {
        this.movementStrategy = strategy;
    }
    @Override
    public AIPlayer clone() {
        AIPlayer cloned = (AIPlayer) super.clone();
        cloned.movementStrategy = this.movementStrategy;
        cloned.stateHistory = new ArrayList<>(this.stateHistory);
        return cloned;
    }
    @Override
    public void attach(Observer observer) { observers.add(observer); }
    @Override
    public void detach(Observer observer) { observers.remove(observer); }
    @Override
    public void notifyObservers(Event event) {
        for (Observer observer : observers) {
            observer.update(event);
        }
    }
    // Phương thức để lấy chiến lược di chuyển
    public MovementStrategy getMovementStrategy() { return movementStrategy; }
    // Hàm Heuristic
    private int heuristic(Node state) {
// Giả định rằng Node chứa thông tin về vị trí của AI và người chơi
        int aiX = state.getAIPlayerX();
        int aiY = state.getAIPlayerY();
        int playerX = state.getPlayerX();
        int playerY = state.getPlayerY();
// Tính toán khoảng cách Manhattan giữa AI và người chơi
        int distanceToPlayer = Math.abs(aiX - playerX) + Math.abs(aiY - playerY);
// Nếu AI ở gần người chơi, trả về giá trị âm (AI có nguy cơ)
        if (distanceToPlayer < 3) {
            return -100 + distanceToPlayer; // Giá trị âm lớn hơn nếu gần người chơi
        }
// Nếu AI có thể đặt bom và gây nổ gần người chơi
        if (canPlaceBombNearPlayer(state)) {
            return 100; // Giá trị dương nếu có cơ hội tấn công
        }
// Trả về giá trị dựa trên khoảng cách an toàn từ bom hoặc các mối nguy hiểm khác
        return distanceToPlayer; // Giá trị trung bình dựa trên khoảng cách an toàn
    }
    private boolean canPlaceBombNearPlayer(Node state) {
// Logic kiểm tra xem AI có thể đặt bom gần vị trí của người chơi không.
// Ví dụ: kiểm tra các ô xung quanh người chơi để xem có thể đặt bom hay không.
        return false; // Cần triển khai logic thực tế ở đây.
    }

//    public void increaseSpeed() {
//
//        if (speed < 5) {
//            speed++;
//            System.out.println("AIPlayer speed đã tăng lên: " + speed);
//            // Hoặc bạn có thể giảm moveDelay để AI nhanh hơn
//            // this.moveDelay = Math.max(1, moveDelay - 1);
//        } else {
//            System.out.println("AIPlayer speed đạt tối đa.");
//        }
//    }

    // Giả sử AIPlayer cũng có speed, explosionRange
    public void increaseSpeed() {
        // Tăng speed, hoặc giảm moveDelay, tuỳ logic
        // Ví dụ:
        if (moveDelay > 1) {
            moveDelay -= 1;
            System.out.println("AIPlayer moveDelay giảm còn: " + moveDelay);
        }
    }

    public void increaseExplosionRange() {
        explosionRange++;
        System.out.println("AIPlayer explosionRange đã tăng lên: " + explosionRange);
    }
}