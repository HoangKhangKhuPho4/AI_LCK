package ai;

import java.util.*;

/**
 * Lớp đại diện cho AI Player trong trò chơi.
 */
public class AIPlayer extends Entity implements Cloneable, Subject {
    private Game game;
    private MovementStrategy movementStrategy;
    public int ticksUntilMove;
    public int moveDelay;
    private int explosionRange = 1;
    private List<Observer> observers = new ArrayList<>();
    private boolean isExecuting = false;

    // Lưu trạng thái và hành động của AI trong quá khứ
    private List<AIState> stateHistory = new ArrayList<>();

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


    // Trong AIPlayer.java
// Trong AIPlayer.java, trong hàm update
    // Trong AIPlayer.java, trong hàm update
    @Override
    public void update(Game game) {
        if (!alive) {
            System.out.println(this.getClass().getSimpleName() + " không còn sống.");
            return;
        }
        ticksUntilMove--;
        if (ticksUntilMove <= 0 && !isExecuting) {
            isExecuting = true;
            System.out.println("AIPlayer bắt đầu cập nhật...");
            scanForHazards(game, 3);  // Tầm nhìn trong bán kính 3 ô
            predictBombs(game);
            predictPlayerActions(game);

            // Lưu trạng thái hiện tại để học hỏi
            recordState(game);

            // Cập nhật chiến lược dựa trên lịch sử trạng thái
            updateStrategy();

            // Đặt lại ticksUntilMove trước khi khởi tạo AIWorker
            ticksUntilMove = moveDelay;

            // Kiểm tra xem AI có bị dồn vào ngõ cụt không
            if (isCornered(this, game.getGameMap())) {
                escapeAttempts++;
                System.out.println("AIPlayer đang bị dồn vào ngõ cụt, cố gắng thoát lần " + escapeAttempts);
                if (escapeAttempts >= MAX_ESCAPE_ATTEMPTS) {
                    System.out.println("AIPlayer đã cố gắng thoát nhiều lần, chuyển sang chiến lược Minimax.");
                    setMovementStrategy(new MinimaxStrategy(7, true));
                    escapeAttempts = 0;
                } else {
                    setMovementStrategy(new EscapeBombsStrategy(game.getGameMap()));
                }
            } else {
                setMovementStrategy(new MinimaxStrategy(7, true)); // Hoặc chiến lược hiện tại
                escapeAttempts = 0;
            }

            // Sử dụng AIWorker để thực hiện di chuyển
            AIWorker aiWorker = new AIWorker(this, game);
            aiWorker.execute();
            System.out.println("AIWorker đã được gọi để thực hiện di chuyển.");
        }
    }


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



    public void setExecuting(boolean executing) {
        this.isExecuting = executing;
    }


    @Override
    protected int getExplosionRange() {
        return explosionRange; // Trả về phạm vi nổ thực tế
    }

    // Dự đoán hành động của người chơi
    public void predictPlayerActions(Game game) {
        Player player = game.getPlayer();
        int distance = Math.abs(player.getX() - x) + Math.abs(player.getY() - y);
        if (distance <= 3) {
            System.out.println("Dự đoán: Người chơi có thể đặt bom gần!");
        }
    }

    // Dự đoán bom sắp nổ
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

    // Phân tích các vật thể trong tầm nhìn (bom và balloon)
    public void scanForHazards(Game game, int radius) {
        List<int[]> visibleArea = getVisibleArea(radius);
        for (int[] coord : visibleArea) {
            int newX = coord[0];
            int newY = coord[1];

            // Kiểm tra bom
            for (Bomb bomb : game.getBombs()) {
                int distance = Math.abs(bomb.getX() - newX) + Math.abs(bomb.getY() - newY);
                if (distance <= bomb.getExplosionRange()) {
                    System.out.println("Bom gần ở: " + newX + "," + newY);
                }
            }

            // Kiểm tra Balloon
            for (Balloon balloon : game.getBalloons()) {
                int distance = Math.abs(balloon.getX() - newX) + Math.abs(balloon.getY() - newY);
                if (distance <= 2) {
                    System.out.println("Balloon gần ở: " + newX + "," + newY);
                }
            }
        }
    }

    // Lưu lại trạng thái của AI và kết quả
    public void recordState(Game game) {
        // Lưu trạng thái (vị trí, số bom, số lượng Balloon)
        AIState state = new AIState(x, y, bombCount, game.getBalloons().size());
        stateHistory.add(state);
        System.out.println("Lưu trạng thái AI tại (" + x + ", " + y + ")");
    }

    // Cập nhật chiến lược sau mỗi trải nghiệm (học máy)
    public void updateStrategy() {
        // Giả sử bạn sử dụng thuật toán học máy để tối ưu chiến lược
        // Cập nhật chiến lược dựa trên lịch sử trạng thái
        System.out.println("Cập nhật chiến lược dựa trên lịch sử trạng thái.");
        // Ở đây, bạn có thể thêm mã để phân tích stateHistory và điều chỉnh movementStrategy
        // Ví dụ: sử dụng dữ liệu để thay đổi MovementStrategy hoặc cải thiện thuật toán Minimax
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

    // Cập nhật chiến lược di chuyển
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

    // Implement Subject interface
    @Override
    public void attach(Observer observer) {
        observers.add(observer);
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

    // Phương thức để lấy chiến lược di chuyển
    public MovementStrategy getMovementStrategy() {
        return movementStrategy;
    }
}
