
package ai;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
/**
 * Lớp đại diện cho bom trong trò chơi.
 */
public class Bomb implements Subject, Cloneable {
    private int x, y; // Tọa độ của bom trên bản đồ
    private int countdown; // Số lượt trước khi bom nổ
    private boolean exploded; // Trạng thái đã nổ hay chưa
    private int explosionDuration; // Thời gian hiệu ứng nổ kéo dài
    private Entity owner; // Thực thể sở hữu bom (Player hoặc AIPlayer)
    private int explosionRange; // Phạm vi nổ của bom
    private List<Observer> observers; // Danh sách các Observer
    /**
     * Constructor khởi tạo bom với vị trí, countdown, chủ nhân và phạm vi nổ.
     *
     * @param x Tọa độ X của bom.
     * @param y Tọa độ Y của bom.
     * @param countdown Số lượt trước khi bom nổ.
     * @param owner Thực thể sở hữu bom.
     * @param explosionRange Phạm vi nổ của bom.
     */
    public Bomb(int x, int y, int countdown, Entity owner, int explosionRange) {
        this.x = x;
        this.y = y;
        this.countdown = countdown;
        this.exploded = false;
        this.explosionDuration = 1; // Hiệu ứng nổ kéo dài 1 lượt
        this.owner = owner;
        this.explosionRange = explosionRange;
        this.observers = new ArrayList<>();
    }

    public Bomb(int aiPlayerX, int aiPlayerY, int countdown, String aiPlayer, int explosionRange) {
    }

    // Các phương thức getter và setter
    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
    public int getCountdown() {
        return countdown;
    }
    public Entity getOwner() {
        return owner;
    }
    public int getExplosionRange() {
        return explosionRange;
    }
    public boolean isExploded() {
        return exploded;
    }
    public boolean isExplosionFinished() {
        return exploded && explosionDuration == 0;
    }
    public void setX(int x) {
        this.x = x;
    }
    public void setY(int y) {
        this.y = y;
    }
    /**
     * Đặt lại countdown của bom.
     *
     * @param countdown Giá trị countdown mới.
     */
    public void setCountdown(int countdown) {
        this.countdown = countdown;
    }
    /**
     * Triển khai phương thức attach từ giao diện Subject.
     *
     * @param observer Observer cần đăng ký.
     */
    @Override
    public void attach(Observer observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }
    /**
     * Triển khai phương thức detach từ giao diện Subject.
     *
     * @param observer Observer cần hủy đăng ký.
     */
    @Override
    public void detach(Observer observer) {
        observers.remove(observer);
    }
    /**
     * Triển khai phương thức notifyObservers từ giao diện Subject.
     *
     * @param event Sự kiện cần thông báo.
     */
    @Override
    public void notifyObservers(Event event) {
        for (Observer observer : observers) {
            observer.update(event);
        }
    }
    /**
     * Cập nhật trạng thái của bom. Được gọi mỗi lượt chơi.
     *
     * @param game Trạng thái trò chơi hiện tại.
     */
    /**
     * Cập nhật trạng thái của bom. Được gọi mỗi lượt chơi.
     *
     * @param game Trạng thái trò chơi hiện tại.
     */
    public void tick(Game game) {
        if (!exploded) {
            if (countdown > 0) {
                countdown--;
                // Debug: In countdown mỗi lượt
                System.out.println("Bom tại (" + x + ", " + y + ") còn " + countdown + " lượt trước khi nổ.");
            }
            if (countdown == 0) {
                exploded = true;
                // Xử lý nổ bom
                processExplosion(game);
                // Thông báo sự kiện nổ bom cho các Observer
                List<int[]> explosionTiles = game.getExplosionTiles(this);
                notifyObservers(new BombExplodedEvent(explosionTiles));
                // Debug: Thông báo bom đã nổ
                System.out.println("Bom tại (" + x + ", " + y + ") đã nổ.");
            }
        } else {
            if (explosionDuration > 0) {
                explosionDuration--;
                // Debug: In explosion duration mỗi lượt
                System.out.println("Bom tại (" + x + ", " + y + ") đang trong hiệu ứng nổ, còn " + explosionDuration + " lượt.");
            }
        }
    }



    /**
     * Xử lý hiệu ứng nổ của bom, bao gồm phá hủy tường phá hủy và tạo vật phẩm.
     *
     * @param game Trạng thái trò chơi hiện tại.
     */
    private void processExplosion(Game game) {
        List<int[]> explosionTiles = game.getExplosionTiles(this);
        GameMap map = game.getGameMap();
        for (int[] tile : explosionTiles) {
            int tx = tile[0];
            int ty = tile[1];
            char currentTile = map.getTile(tx, ty);
            if (currentTile == 'D') { // Giả sử 'D' là tường phá hủy
// Phá hủy tường
                map.setTile(tx, ty, ' ');
                System.out.println("Tường phá hủy tại (" + tx + ", " + ty + ") đã bị phá hủy.");
// 20% cơ hội tạo vật phẩm sau khi phá hủy tường
                Random rand = new Random();
                if (rand.nextInt(100) < 20) {
                    Item.ItemType type = rand.nextBoolean() ? Item.ItemType.SPEED : Item.ItemType.EXPLOSION_RANGE;
                    game.getGameMap().addItem(new Item(tx, ty, type));
                    System.out.println("Vật phẩm " + type + " xuất hiện tại (" + tx + ", " + ty + ").");
                }
            }
// Các xử lý khác nếu cần thiết, ví dụ: ảnh hưởng đến các thực thể tại tile
// Lưu ý: Các thực thể sẽ nhận sự kiện BombExplodedEvent thông qua Observer Pattern
        }
    }
    /**
     * Clone bom để tạo bản sao sâu. Được sử dụng trong các thuật toán tìm kiếm như Minimax.
     *
     * @return Bản sao của bom.
     */
    @Override
    public Bomb clone() {
        try {
            Bomb cloned = (Bomb) super.clone();
            cloned.observers = new ArrayList<>(); // Không clone các Observer
            cloned.owner = this.owner.clone(); // Clone chủ nhân bom
            return cloned;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * Giảm countdown của bom. Có thể sử dụng trong các tình huống đặc biệt.
     */
    public void decrementCountdown() {
        if (!exploded && countdown > 0) {
            countdown--;
        }
    }
}
