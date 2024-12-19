
package ai;
/**
 * Lớp đại diện cho các vật phẩm trên bản đồ.
 */
public class Item implements Cloneable {
    private int x, y;
    private ItemType type;
    public enum ItemType {
        SPEED, // Vật phẩm tăng tốc độ di chuyển
        EXPLOSION_RANGE // Vật phẩm tăng phạm vi nổ
    }
    public Item(int x, int y, ItemType type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }
    public int getX() { return x; }
    public int getY() { return y; }
    public ItemType getType() { return type; }
    @Override
    public Item clone() {
        try {
            return (Item) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }
}
