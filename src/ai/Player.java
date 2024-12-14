

package ai;

/**
 * Lớp đại diện cho người chơi trong trò chơi.
 */
public class Player extends Entity implements Cloneable {
    private int speed;
    private int explosionRange;

    public Player(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        this.speed = 1;
        this.explosionRange = 1;
        this.alive = true;
    }

    @Override
    public Player clone() {
        Player clonedPlayer = (Player) super.clone();
        // Nếu có các đối tượng phức tạp cần clone sâu, thực hiện ở đây
        return clonedPlayer;
    }

    @Override
    public void update(Game game) {
        // Xử lý cập nhật nếu cần
    }

    // Các phương thức liên quan đến tốc độ và phạm vi nổ
    public void increaseSpeed() {
        if (speed < 5) {
            speed++;
        }
    }

    public int getSpeed() {
        return speed;
    }

    public void increaseExplosionRange() {
        explosionRange++;
    }

    public int getExplosionRange() {
        return explosionRange;
    }

    // Phương thức để thiết lập số lượng bom
    public void setBombCount(int count) {
        if (count >= 0) {  // Kiểm tra nếu count không âm
            this.bombCount = count;
        } else {
            System.out.println("Số lượng bom không hợp lệ. Giá trị phải lớn hơn hoặc bằng 0.");
        }
    }

    // Phương thức để lấy số lượng bom hiện tại
    public int getBombCount() {
        return bombCount;
    }
}
