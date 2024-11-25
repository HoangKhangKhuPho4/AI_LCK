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

    public abstract void update(Game game);

    @Override
    public void update(Event event) {
        if (event instanceof BombExplodedEvent) {
            BombExplodedEvent bombEvent = (BombExplodedEvent) event;
            List<int[]> explosionTiles = bombEvent.getExplosionTiles();
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
}