package ai;

import java.util.List;

/**
 * Lớp trừu tượng đại diện cho một thực thể trong trò chơi.
 */
public abstract class Entity implements Cloneable, Observer {
    protected int x, y;
    protected boolean alive = true;
    protected int bombCount = 1;

    @Override
    public Entity clone() {
        try {
            return (Entity) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public boolean isAlive() { return alive; }
    public void setAlive(boolean alive) { this.alive = alive; }

    // Hàm cập nhật trạng thái của thực thể
    public abstract void update(Game game);

    @Override
    public void update(Event event) {
        if (event instanceof BombExplodedEvent) {
            BombExplodedEvent bombEvent = (BombExplodedEvent) event;
            List<int[]> explosionTiles = bombEvent.getExplosionTiles();
            // Kiểm tra nếu thực thể đang ở trong phạm vi nổ của bom
            for (int[] tile : explosionTiles) {
                if (this.x == tile[0] && this.y == tile[1]) {
                    this.alive = false;
                    System.out.println(this.getClass().getSimpleName() + " tại (" + x + ", " + y + ") bị nổ.");
                    break;
                }
            }
        }
    }

    /**
     * Quản lý đặt bom.
     * @return true nếu đặt bom thành công, false nếu không còn bom để đặt.
     */
    public boolean placeBomb() {
        if (bombCount > 0) {
            bombCount--;
            System.out.println(this.getClass().getSimpleName() + " đã đặt một quả bom tại: (" + x + ", " + y + ")");
            return true;
        }
        return false;
    }

    /**
     * Tăng số lượng bom có thể đặt.
     */
    public void increaseBombCount() {
        bombCount++;
    }

    public int getBombCount() {
        return bombCount;
    }

    /**
     * Lấy phạm vi nổ của thực thể.
     * @return phạm vi nổ.
     */
    protected abstract int getExplosionRange();

    /**
     * Kiểm tra xem vị trí của thực thể có an toàn không (không nằm trong phạm vi nổ của bom).
     * @param game Trạng thái trò chơi hiện tại.
     * @return true nếu vị trí an toàn, false nếu có bom nổ gần.
     */
    public boolean isSafe(Game game) {
        for (Bomb bomb : game.getBombs()) {
            if (!bomb.isExploded()) {
                int bombDistance = Math.abs(bomb.getX() - this.x) + Math.abs(bomb.getY() - this.y);
                // Kiểm tra nếu thực thể đang đứng trong phạm vi bom
                if (bombDistance <= bomb.getExplosionRange()) {
                    return false; // Không an toàn
                }
            }
        }
        return true; // An toàn
    }

    /**
     * Kiểm tra xem AI có nên đặt bom hay không dựa trên các yếu tố an toàn.
     * @param game Trạng thái trò chơi hiện tại.
     * @return true nếu AI có thể an toàn khi đặt bom, false nếu không.
     */
    public boolean canPlaceBombSafely(Game game) {
        // Kiểm tra nếu vị trí của AI không an toàn
        if (!isSafe(game)) {
            System.out.println("AI không thể đặt bom vì vị trí không an toàn.");
            return false;
        }
        // Nếu vị trí an toàn, thực hiện đặt bom
        return placeBomb();
    }

    /**
     * Di chuyển thực thể lên trên.
     */
    public void moveUp() {
        this.y -= 1;
    }

    /**
     * Di chuyển thực thể xuống dưới.
     */
    public void moveDown() {
        this.y += 1;
    }

    /**
     * Di chuyển thực thể sang trái.
     */
    public void moveLeft() {
        this.x -= 1;
    }

    /**
     * Di chuyển thực thể sang phải.
     */
    public void moveRight() {
        this.x += 1;
    }

    /**
     * Phương thức được gọi khi thực thể bị trúng bom.
     * @param bomb Quả bom đã phát nổ.
     */
    public void onHitByBomb(Bomb bomb) {
        // Kiểm tra xem thực thể có trúng bom không
        if (this.x == bomb.getX() && this.y == bomb.getY()) {
            this.alive = false;
            System.out.println(this.getClass().getSimpleName() + " tại (" + x + ", " + y + ") bị trúng bom.");
        }
    }
}
