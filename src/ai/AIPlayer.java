
package ai;

import javax.swing.SwingWorker;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp đại diện cho AI Player trong trò chơi.
 */
public class AIPlayer extends Entity implements Cloneable, Subject {
    private MovementStrategy movementStrategy;
    public int ticksUntilMove;
    public int moveDelay;
    private int explosionRange = 1; // Thêm thuộc tính phạm vi nổ
    private List<Observer> observers = new ArrayList<>();

    public AIPlayer(int startX, int startY, MovementStrategy strategy) {
        this.x = startX;
        this.y = startY;
        this.movementStrategy = strategy;
        this.moveDelay = 3;
        this.ticksUntilMove = moveDelay;
    }

    public MovementStrategy getMovementStrategy() {
        return movementStrategy;
    }

    @Override
    public void update(Game game) {
        if (!alive) {
            System.out.println(this.getClass().getSimpleName() + " không còn sống.");
            return;
        }
        ticksUntilMove--;
        if (ticksUntilMove <= 0) {
            // Kiểm tra xem AI có nên đặt bom không
            if (shouldPlaceBomb(game)) {
                placeBomb(game);
            }

            // Sử dụng AIWorker để thực hiện di chuyển AI trên một luồng riêng
            AIWorker aiWorker = new AIWorker(this, game);
            aiWorker.execute();
        }
    }

    private boolean shouldPlaceBomb(Game game) {
        Player player = game.getPlayer();
        int distance = Math.abs(player.getX() - x) + Math.abs(player.getY() - y);
        // Đặt bom nếu người chơi ở gần (có thể điều chỉnh khoảng cách)
        return distance <= 2 && bombCount > 0;
    }

    private void placeBomb(Game game) {
        if (placeBomb()) { // Gọi phương thức placeBomb() từ lớp Entity
            int countdown = 30; // Đếm ngược 3 giây
            Bomb bomb = new Bomb(x, y, countdown, this, explosionRange);
            game.addBomb(bomb);
        }
    }

    public int getExplosionRange() {
        return explosionRange;
    }

    public void increaseExplosionRange() {
        explosionRange++;
    }

    /**
     * Thiết lập chiến lược di chuyển mới cho AI.
     * @param strategy Chiến lược di chuyển mới.
     */
    public void setMovementStrategy(MovementStrategy strategy) {
        this.movementStrategy = strategy;
    }

    @Override
    public AIPlayer clone() {
        AIPlayer cloned = (AIPlayer) super.clone();
        // Clone hoặc sao chép các đối tượng phức tạp nếu cần
        cloned.movementStrategy = this.movementStrategy; // Giả sử MovementStrategy là immutable hoặc được chia sẻ
        cloned.observers = new ArrayList<>(); // Observers không được clone
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
}
