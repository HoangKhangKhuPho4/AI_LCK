
package ai;
import java.util.List;
import javax.swing.JOptionPane;

/**
 * Lớp đại diện cho người chơi trong trò chơi.
 */
public class Player extends Entity implements Observer, Cloneable {
    private int speed;
    private int explosionRange;
    private boolean alive;
    private int bombCount; // Số lượng bom mà người chơi có thể đặt
    /**
     * Constructor khởi tạo người chơi với vị trí ban đầu.
     *
     * @param startX Tọa độ X ban đầu của người chơi.
     * @param startY Tọa độ Y ban đầu của người chơi.
     */
    public Player(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        this.speed = 1;
        this.explosionRange = 1;
        this.alive = true;
        this.bombCount = 1; // Giá trị mặc định, có thể điều chỉnh theo yêu cầu
    }

    /**
     * Triển khai phương thức clone để tạo bản sao sâu của người chơi.
     *
     * @return Bản sao của người chơi.
     */
    @Override
    public Player clone() {
        Player clonedPlayer = (Player) super.clone();
        // Nếu có các đối tượng phức tạp cần clone sâu, thực hiện ở đây
        return clonedPlayer;
    }

    /**
     * Triển khai phương thức update từ giao diện Observer.
     *
     * @param event Sự kiện cần xử lý.
     */
    @Override
    public void update(Event event) {
        if (event instanceof BombExplodedEvent) {
            BombExplodedEvent bombEvent = (BombExplodedEvent) event;
            Bomb explodedBomb = bombEvent.getBomb();

            // Check nếu bomb này do Player đặt
            if (explodedBomb.getOwner() == this) {
                this.increaseBombCount();
                System.out.println("Bom của Player đã nổ, tăng bombCount lên: "
                        + this.getBombCount());
            }

            // Vẫn xử lý vụ Player có bị nổ không
            List<int[]> explosionTiles = bombEvent.getExplosionTiles();
            for (int[] tile : explosionTiles) {
                if (this.getX() == tile[0] && this.getY() == tile[1]) {
                    this.alive = false;
                    System.out.println("Người chơi đã chết do bom nổ!");
                    break;
                }
            }
        }
    }


    // Các phương thức liên quan đến tốc độ và phạm vi nổ

    /**
     * Tăng tốc độ của người chơi lên tối đa 5.
     */
    public void increaseSpeed() {
        if (speed < 5) {
            speed++;
            System.out.println("Tốc độ tăng lên: " + speed);
        } else {
            System.out.println("Tốc độ đã đạt mức tối đa.");
        }
    }

    /**
     * Lấy giá trị tốc độ hiện tại của người chơi.
     *
     * @return Giá trị tốc độ.
     */
    public int getSpeed() {
        return speed;
    }

    /**
     * Tăng phạm vi nổ của người chơi.
     */
    public void increaseExplosionRange() {
        explosionRange++;
        System.out.println("Phạm vi nổ tăng lên: " + explosionRange);
    }

    /**
     * Lấy giá trị phạm vi nổ hiện tại của người chơi.
     *
     * @return Giá trị phạm vi nổ.
     */
    public int getExplosionRange() {
        return explosionRange;
    }

    // Phương thức để thiết lập số lượng bom

    /**
     * Đặt lại số lượng bom mà người chơi có thể đặt.
     *
     * @param count Số lượng bom mới.
     */
    public void setBombCount(int count) {
        if (count >= 0) { // Kiểm tra nếu count không âm
            this.bombCount = count;
            System.out.println("Số lượng bom được đặt lại thành: " + bombCount);
        } else {
            System.out.println("Số lượng bom không hợp lệ. Giá trị phải lớn hơn hoặc bằng 0.");
        }
    }

    // Phương thức để lấy số lượng bom hiện tại

    /**
     * Lấy số lượng bom mà người chơi có thể đặt hiện tại.
     *
     * @return Số lượng bom.
     */
    public int getBombCount() {
        return bombCount;
    }

    /**
     * Phương thức di chuyển của người chơi.
     *
     * @param dx      Số bước di chuyển theo trục X.
     * @param dy      Số bước di chuyển theo trục Y.
     * @param gameMap Bản đồ trò chơi.
     * @param bombs   Danh sách các bom hiện tại.
     */
    public void move(int dx, int dy, GameMap gameMap, List<Bomb> bombs) {
        int newX = this.x + dx;
        int newY = this.y + dy;
        if (gameMap.isWalkable(newX, newY)) {
            this.x = newX;
            this.y = newY;
            System.out.println("Người chơi đã di chuyển đến (" + newX + ", " + newY + ").");
        } else {
            System.out.println("Người chơi không thể di chuyển đến (" + newX + ", " + newY + ").");
        }
    }

    /**
     * Phương thức đặt bom bởi người chơi.
     *
     * @param game Trạng thái trò chơi hiện tại.
     */
    public void placeBomb(Game game) {
        if (bombCount > 0) {
            game.placeBomb(this);
            bombCount--;
            System.out.println("Đã đặt bom tại (" + this.x + ", " + this.y + "). Số lượng bom còn lại: " + bombCount);
        } else {
            System.out.println("Không còn bom để đặt.");
        }
    }

    /**
     * Kiểm tra xem người chơi còn sống hay không.
     *
     * @return true nếu còn sống, false nếu đã chết.
     */
    public boolean isAlive() {
        return alive;
    }

    /**
     * Đặt trạng thái sống của người chơi.
     *
     * @param alive Trạng thái sống mới.
     */
    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    @Override
    public void update(Game game) {
    }
}
