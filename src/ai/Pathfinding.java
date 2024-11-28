package ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;

public class Pathfinding {
    private GameMap map;

    // Tối ưu hoá cache sử dụng LinkedHashMap với access order để làm LRU cache
    private final Map<String, List<int[]>> pathCache;

    // Constructor Pathfinding
    public Pathfinding(GameMap map) {
        this.map = map;
        // Dùng LinkedHashMap để có thể sử dụng cơ chế LRU
        this.pathCache = new LinkedHashMap<String, List<int[]>>(100, 0.75f, true) {
            // Override phương thức removeEldestEntry để áp dụng chiến lược LRU
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, List<int[]>> eldest) {
                // Nếu cache vượt quá 100 đường đi, sẽ xóa đi phần tử cũ nhất (least-recently-used)
                return size() > 100;
            }
        };
    }

    /**
     * Tìm đường đi an toàn từ (startX, startY) đến (goalX, goalY) Tránh các vùng có
     * bom đang sắp nổ hoặc đã đặt bom gần đó và tránh Balloon
     */
    public List<int[]> findSafePath(int startX, int startY, int goalX, int goalY, Game game) {
        // Tạo key để tra cứu trong cache
        String key = "safe:" + startX + "," + startY + "->" + goalX + "," + goalY;

        // Kiểm tra nếu cache đã có sẵn đường đi
        if (pathCache.containsKey(key)) {
            return pathCache.get(key);
        }

        // Tạo PriorityQueue để thực hiện A* search
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> n.fScore));
        Map<String, Node> allNodes = new HashMap<>();

        // Khởi tạo Node bắt đầu
        Node start = new Node(startX, startY, null);
        start.gScore = 0;
        start.fScore = heuristic(startX, startY, goalX, goalY, game);
        openSet.add(start);
        allNodes.put(start.key(), start);

        // A* search để tìm đường đi
        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            if (current.x == goalX && current.y == goalY) {
                List<int[]> path = constructPath(current);
                // Lưu đường đi vào cache và trả về
                pathCache.put(key, path);
                return path;
            }

            // Xử lý các hướng di chuyển của Node hiện tại
            for (int[] dir : getDirections()) {
                int newX = current.x + dir[0];
                int newY = current.y + dir[1];

                // Kiểm tra ô có thể đi được
                if (!map.isWalkable(newX, newY)) continue;

                // Đánh giá mức độ nguy hiểm tại ô mới
                double danger = dangerFactor(newX, newY, game);
                if (danger > 0.7) continue; // Bỏ qua ô quá nguy hiểm

                double tentativeGScore = current.gScore + distance(current.x, current.y, newX, newY) + danger;
                String neighborKey = Node.key(newX, newY);
                Node neighbor = allNodes.getOrDefault(neighborKey, new Node(newX, newY, current));

                // Nếu gScore của Neighbor thấp hơn, cập nhật
                if (tentativeGScore < neighbor.gScore) {
                    neighbor.parent = current;
                    neighbor.gScore = tentativeGScore;
                    neighbor.fScore = neighbor.gScore + heuristic(newX, newY, goalX, goalY, game) + danger;
                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    }
                    allNodes.put(neighborKey, neighbor);
                }
            }
        }

        // Không tìm thấy đường đi an toàn
        return Collections.emptyList();
    }

    // Tính khoảng cách Euclidean hoặc Manhattan (ở đây dùng Manhattan)
    private double distance(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2); // Manhattan Distance
    }

    // Hàm heuristic tính khoảng cách đến mục tiêu và các yếu tố nguy hiểm
    private double heuristic(int x, int y, int goalX, int goalY, Game game) {
        double distance = Math.abs(goalX - x) + Math.abs(goalY - y); // Manhattan Distance
        double danger = dangerFactor(x, y, game);
        double itemPriority = 0.0;

        // Thêm yếu tố vật phẩm gần đó
        for (Item item : game.getGameMap().getItems()) {
            int itemDistance = Math.abs(item.getX() - x) + Math.abs(item.getY() - y);
            if (itemDistance == 0) continue;
            itemPriority += (item.getType() == Item.ItemType.SPEED) ? 1.0 / itemDistance : 1.5 / itemDistance;
        }

        double heuristicValue = distance + danger * 10 - itemPriority * 5;
        return heuristicValue;
    }

    // Đánh giá mức độ nguy hiểm từ các yếu tố như bom, Balloon
    private double dangerFactor(int x, int y, Game game) {
        double danger = 0.0;

        // Nguy hiểm từ bom
        List<Bomb> bombs = game.getBombs();
        for (Bomb bomb : bombs) {
            if (bomb.isExploded()) continue;
            int distance = Math.abs(bomb.getX() - x) + Math.abs(bomb.getY() - y);
            if (distance <= bomb.getExplosionRange()) {
                double timeUntilExplosion = bomb.getCountdown() / 10.0;
                danger += (1.0 - ((double) distance / bomb.getExplosionRange())) / timeUntilExplosion;
            }
        }

        // Nguy hiểm từ Balloon
        List<Balloon> balloons = game.getBalloons();
        for (Balloon balloon : balloons) {
            if (!balloon.isAlive()) continue;
            int distance = Math.abs(balloon.getX() - x) + Math.abs(balloon.getY() - y);
            if (distance == 0) {
                danger += 1.0;
            } else if (distance == 1) {
                danger += 0.8;
            } else if (distance == 2) {
                danger += 0.5;
            }
        }

        return Math.min(danger, 1.0);
    }

    // Xây dựng đường đi từ các Node
    private List<int[]> constructPath(Node node) {
        List<int[]> path = new ArrayList<>();
        while (node.parent != null) {
            path.add(new int[] { node.x, node.y });
            node = node.parent;
        }
        Collections.reverse(path);
        return path;
    }

    // Các hướng di chuyển cơ bản (Lên, Xuống, Trái, Phải)
    private int[][] getDirections() {
        return new int[][] { { 0, -1 }, { 0, 1 }, { -1, 0 }, { 1, 0 } };
    }

    private static class Node {
        int x, y;
        Node parent;
        double gScore = Double.MAX_VALUE;
        double fScore = Double.MAX_VALUE;

        Node(int x, int y, Node parent) {
            this.x = x;
            this.y = y;
            this.parent = parent;
        }

        String key() {
            return key(this.x, this.y);
        }

        static String key(int x, int y) {
            return x + "," + y;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Node) {
                Node other = (Node) obj;
                return this.x == other.x && this.y == other.y;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }
}
