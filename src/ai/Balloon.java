

package ai;

/**
 * Lớp đại diện cho Balloon trong trò chơi.
 */
public class Balloon extends Entity implements Cloneable {
    private MovementStrategy movementStrategy;
    private int ticksUntilMove;
    private int moveDelay;

    public Balloon(int startX, int startY, MovementStrategy strategy) {
        this.x = startX;
        this.y = startY;
        this.movementStrategy = strategy;
        this.moveDelay = 4;
        this.ticksUntilMove = moveDelay;
    }

    @Override
    public void update(Game game) {
        if (!alive) {
            System.out.println(this.getClass().getSimpleName() + " không còn sống.");
            return;
        }
        ticksUntilMove--;
        if (ticksUntilMove <= 0) {
            movementStrategy.move(this, game);
            ticksUntilMove = moveDelay;
            System.out.println(this.getClass().getSimpleName() + " đã di chuyển đến (" + x + ", " + y + ").");
        }

        // Kiểm tra va chạm với người chơi
        Player player = game.getPlayer();
        if (this.x == player.getX() && this.y == player.getY() && player.isAlive()) {
            player.setAlive(false);
            System.out.println("Người chơi bị Balloon tiêu diệt!");
        }

        // Kiểm tra va chạm với AIPlayer
        AIPlayer aiPlayer = game.getAiPlayer(); // Sửa tên phương thức ở đây
        if (aiPlayer != null && aiPlayer.isAlive() && this.x == aiPlayer.getX() && this.y == aiPlayer.getY()) {
            aiPlayer.setAlive(false);
            System.out.println("AIPlayer bị Balloon tiêu diệt!");
        }
    }


    @Override
    protected int getExplosionRange() {
        // Không liên quan đến Balloon, trả về 0 hoặc giá trị phù hợp
        return 0;
    }

    @Override
    public Balloon clone() {
        Balloon cloned = (Balloon) super.clone();
        // Clone hoặc sao chép các đối tượng phức tạp nếu cần
        cloned.movementStrategy = this.movementStrategy; // Giả sử MovementStrategy là immutable hoặc được chia sẻ
        return cloned;
    }
}
