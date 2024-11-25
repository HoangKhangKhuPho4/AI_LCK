package ai;

// File: ai/Player.java

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
        try {
            Player clonedPlayer = (Player) super.clone();
            // Sao chép các thuộc tính cần thiết nếu có đối tượng phức tạp
            return clonedPlayer;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
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
}