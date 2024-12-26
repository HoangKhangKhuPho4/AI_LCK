

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
    public void setMap(int[][] gameMapData) {
// Kiểm tra nếu dữ liệu bản đồ hợp lệ
        if (gameMapData == null || gameMapData.length != width || gameMapData[0].length != height) {
            System.out.println("Dữ liệu bản đồ không hợp lệ.");
            return;
        }
// Cập nhật bản đồ với dữ liệu từ gameMapData
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
// Chuyển đổi giá trị từ gameMapData sang ký tự tương ứng
                switch (gameMapData[i][j]) {
                    case 0: // Ô trống
                        map[i][j] = ' ';
                        break;
                    case 1: // Tường không thể phá hủy
                        map[i][j] = '#';
                        break;
                    case 2: // Tường có thể phá hủy
                        map[i][j] = 'D';
                        break;
                    default:
                        map[i][j] = ' '; // Mặc định là ô trống
                }
            }
        }
// Sau khi cập nhật bản đồ, xóa các vật phẩm cũ và thêm lại các vật phẩm mới từ dữ liệu bản đồ
        items.clear(); // Xóa các vật phẩm cũ
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (map[i][j] == ' ' && !isItemAt(i, j) && !isWithinStartArea(i, j)) {
// Đặt vật phẩm ngẫu nhiên (chỉ ở ô trống)
                    Random rand = new Random();
                    Item.ItemType type = rand.nextBoolean() ? Item.ItemType.SPEED : Item.ItemType.EXPLOSION_RANGE;
                    items.add(new Item(i, j, type));
                }
            }
        }
    }
    /**
     * Chuyển đổi bản đồ từ char[][] sang int[][] với các mã số tương ứng.
     */
    public int[][] getMap() {
        int[][] intMap = new int[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                switch (map[i][j]) {
                    case ' ':
                        intMap[i][j] = 0; // Ô trống
                        break;
                    case '#':
                        intMap[i][j] = 1; // Tường không phá hủy
                        break;
                    case 'D':
                        intMap[i][j] = 2; // Tường phá hủy
                        break;
                    default:
                        intMap[i][j] = 0; // Mặc định là ô trống nếu không xác định
                        break;
                }
            }
        }
        return intMap;
    }

    public List<int[]> findSafePositions(AIPlayer aiPlayer, Game game) {
        List<int[]> safePositions = new ArrayList<>();
        for (int x = 0; x < map.length; x++) {
            for (int y = 0; y < map[0].length; y++) {
                if (isWalkable(x, y)) {
                    boolean isSafe = true;
                    // Kiểm tra xem vị trí này có nằm trong phạm vi nổ của bất kỳ bom nào không
                    for (Bomb bomb : game.getBombs()) {
                        if (!bomb.isExploded()) {
                            int distance = Math.abs(bomb.getX() - x) + Math.abs(bomb.getY() - y);
                            if (distance <= bomb.getExplosionRange()) {
                                isSafe = false;
                                break;
                            }
                        }
                    }
                    // Kiểm tra xem có Balloon nào gần không (tùy thuộc vào logic của bạn)
                    if (isSafe) {
                        safePositions.add(new int[]{x, y});
                    }
                }
            }
        }
        return safePositions;
    }
}
