
package ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Lớp đại diện cho bản đồ trò chơi.
 */
public class GameMap implements Cloneable {
    private char[][] map;
    private int width;
    private int height;
    private List<Item> items;

    public GameMap(int width, int height) {
        this.width = width;
        this.height = height;
        map = new char[width][height];
        items = new ArrayList<>();
        initializeMap();
        placeRandomItems(5); // Đặt 5 vật phẩm ngẫu nhiên
    }

    /**
     * Khởi tạo bản đồ với tường không phá hủy, tường phá hủy và ô trống 
     */
    private void initializeMap() {
        // Khởi tạo toàn bộ bản đồ là ô trống
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                map[i][j] = ' ';
            }
        }
        // Đặt tường không phá hủy ở viền bản đồ
        for (int i = 0; i < width; i++) {
            map[i][0] = '#';
            map[i][height - 1] = '#';
        }
        for (int j = 0; j < height; j++) {
            map[0][j] = '#';
            map[width - 1][j] = '#';
        }
        // Đặt các tường không phá hủy nội bộ theo mẫu, bỏ qua khu vực khởi đầu
        for (int i = 2; i < width - 2; i += 2) {
            for (int j = 2; j < height - 2; j += 2) {
                if (!isWithinStartArea(i, j)) {
                    map[i][j] = '#';
                }
            }
        }
        // Đặt tường phá hủy ngẫu nhiên, tránh khu vực khởi đầu
        placeRandomDestructibleWalls(30);
    }

    /**
     * Kiểm tra xem một vị trí có nằm trong khu vực khởi đầu không 
     */
    private boolean isWithinStartArea(int x, int y) {
        return (x >= 8 && x <= 12) && (y >= 8 && y <= 12);
    }

    /**
     * Đặt các tường phá hủy ngẫu nhiên trên bản đồ 
     */
    private void placeRandomDestructibleWalls(int count) {
        Random rand = new Random();
        int placed = 0;
        while (placed < count) {
            int x = rand.nextInt(width);
            int y = rand.nextInt(height);
            if (map[x][y] == ' ' && !isWithinStartArea(x, y)) {
                map[x][y] = 'D';
                placed++;
            }
        }
    }

    /**
     * Đặt các vật phẩm ngẫu nhiên trên bản đồ 
     */
    private void placeRandomItems(int count) {
        Random rand = new Random();
        int placed = 0;
        while (placed < count) {
            int x = rand.nextInt(width);
            int y = rand.nextInt(height);
            if (map[x][y] == ' ' && !isWithinStartArea(x, y) && !isItemAt(x, y)) {
                Item.ItemType type = rand.nextBoolean() ? Item.ItemType.SPEED : Item.ItemType.EXPLOSION_RANGE;
                items.add(new Item(x, y, type));
                placed++;
            }
        }
    }

    /**
     * Kiểm tra xem có vật phẩm tại vị trí (x, y) không 
     */
    public boolean isItemAt(int x, int y) {
        for (Item item : items) {
            if (item.getX() == x && item.getY() == y) {
                return true;
            }
        }
        return false;
    }

    /**
     * Lấy danh sách vật phẩm 
     */
    public List<Item> getItems() {
        return items;
    }

    /**
     * Xóa vật phẩm khỏi bản đồ 
     */
    public void removeItem(Item item) {
        items.remove(item);
    }

    /**
     * Trả về ký tự tại vị trí (x, y) 
     */
    public char getTile(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return '#';
        }
        return map[x][y];
    }

    /**
     * Đặt ký tự tại vị trí (x, y) 
     */
    public void setTile(int x, int y, char tile) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            map[x][y] = tile;
        }
    }

    /**
     * Kiểm tra xem một ô có thể đi qua được không 
     */
    public boolean isWalkable(int x, int y) {
        char tile = getTile(x, y);
        return tile == ' ';
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void addItem(Item item) {
        items.add(item);
    }

    @Override
    public GameMap clone() {
        try {
            GameMap clonedMap = (GameMap) super.clone();
            clonedMap.map = new char[this.width][this.height];
            for (int i = 0; i < this.width; i++) {
                System.arraycopy(this.map[i], 0, clonedMap.map[i], 0, this.height);
            }
            clonedMap.items = new ArrayList<>();
            for (Item item : this.items) {
                clonedMap.items.add(item.clone());
            }
            return clonedMap;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isValidCoordinate(int x, int y) {
        // Kiểm tra nếu tọa độ nằm trong phạm vi hợp lệ của bản đồ và là ô trống
        return x >= 0 && x < width && y >= 0 && y < height && isWalkable(x, y);
    }

}
