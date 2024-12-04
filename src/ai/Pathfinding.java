package ai;

import java.util.*;

public class Pathfinding {
    private GameMap map;
    private final Map<String, List<int[]>> pathCache;

    public Pathfinding(GameMap map) {
        this.map = map;
        // Sử dụng LinkedHashMap làm cache cho các đường đi đã tính toán, áp dụng cơ chế LRU
        this.pathCache = new LinkedHashMap<String, List<int[]>>(100, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, List<int[]>> eldest) {
                return size() > 100; // Giới hạn số lượng đường đi trong cache
            }
        };
    }

    /**
     * Tìm đường đi an toàn từ (startX, startY) đến (goalX, goalY)
     * Tránh các vùng có bom đang sắp nổ hoặc đã đặt bom gần đó và tránh Balloon
     */
    public List<int[]> findSafePath(int startX, int startY, int goalX, int goalY, Game game) {
        // Tạo key để tra cứu trong cache
        String key = "safe:" + startX + "," + startY + "->" + goalX + "," + goalY;

        // Kiểm tra nếu cache đã có sẵn đường đi
        if (pathCache.containsKey(key)) {
            return pathCache.get(key);
        }

        // Khởi tạo openSet (hàng đợi ưu tiên) cho thuật toán A*
        // Các node sẽ được sắp xếp theo fScore (tổng chi phí từ start đến mục tiêu qua node đó)
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> n.fScore));
        Map<String, Node> allNodes = new HashMap<>(); // Map lưu trữ tất cả các node đã duyệt

        // Khởi tạo node bắt đầu (start node)
        Node start = new Node(startX, startY, null);
        start.gScore = 0; // Chi phí từ start đến chính nó là 0
        start.fScore = heuristic(startX, startY, goalX, goalY, game); // Heuristic dựa trên khoảng cách và nguy hiểm
        openSet.add(start); // Thêm node bắt đầu vào openSet
        allNodes.put(start.key(), start); // Thêm vào map tất cả các node đã xét

        // Thuật toán A* bắt đầu: Tiến hành tìm đường đi
        while (!openSet.isEmpty()) {
            // Lấy node có fScore thấp nhất từ openSet (A* chọn node tối ưu nhất để kiểm tra tiếp)
            Node current = openSet.poll();

            // Nếu đã đến đích, trả về đường đi từ start đến goal
            if (current.x == goalX && current.y == goalY) {
                List<int[]> path = constructPath(current); // Xây dựng đường đi từ mục tiêu về điểm bắt đầu
                pathCache.put(key, path); // Lưu đường đi vào cache để sử dụng lại
                return path;
            }

            // Duyệt các hướng di chuyển từ node hiện tại (4 hướng cơ bản và 4 hướng chéo)
            for (int[] dir : getDirections()) {
                int newX = current.x + dir[0];
                int newY = current.y + dir[1];

                // Kiểm tra nếu ô mới có thể di chuyển (không có chướng ngại vật)
                if (!map.isWalkable(newX, newY)) continue;

                // Đánh giá mức độ nguy hiểm tại ô mới (do bom hoặc Balloon)
                double danger = dangerFactor(newX, newY, game);
                if (danger > 0.7) continue; // Tránh những ô có nguy hiểm quá cao (tránh bom và Balloon)

                // Tính toán chi phí tạm thời (gScore) từ start đến ô mới
                double tentativeGScore = current.gScore + distance(current.x, current.y, newX, newY) + danger;

                // Tạo key cho node mới
                String neighborKey = Node.key(newX, newY);
                Node neighbor = allNodes.getOrDefault(neighborKey, new Node(newX, newY, current));

                // Nếu gScore của neighbor thấp hơn, cập nhật lại giá trị và thêm vào openSet
                if (tentativeGScore < neighbor.gScore) {
                    neighbor.parent = current;
                    neighbor.gScore = tentativeGScore; // Cập nhật gScore
                    neighbor.fScore = neighbor.gScore + heuristic(newX, newY, goalX, goalY, game) + danger; // Cập nhật fScore
                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor); // Thêm neighbor vào openSet nếu chưa có
                    }
                    allNodes.put(neighborKey, neighbor); // Cập nhật map
                }
            }
        }

        // Nếu không tìm được đường đi an toàn, trả về danh sách rỗng
        return Collections.emptyList();
    }

    // Tính khoảng cách Manhattan giữa hai điểm (x1, y1) và (x2, y2)
    private double distance(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2); // Manhattan Distance
    }

    // Heuristic: tính toán khoảng cách đến mục tiêu và các yếu tố nguy hiểm (bom, Balloon)
    private double heuristic(int x, int y, int goalX, int goalY, Game game) {
        // Khoảng cách Manhattan đến mục tiêu
        double distance = Math.abs(goalX - x) + Math.abs(goalY - y);

        // Tính toán mức độ nguy hiểm tại ô hiện tại
        double danger = dangerFactor(x, y, game);

        // Ưu tiên các vật phẩm gần đó (ví dụ: vật phẩm tăng tốc)
        double itemPriority = 0.0;
        for (Item item : game.getGameMap().getItems()) {
            int itemDistance = Math.abs(item.getX() - x) + Math.abs(item.getY() - y);
            if (itemDistance == 0) continue;
            // Ưu tiên vật phẩm tăng tốc gần đó
            itemPriority += (item.getType() == Item.ItemType.SPEED) ? 1.0 / itemDistance : 1.5 / itemDistance;
        }

        // Tính toán giá trị heuristic cuối cùng, kết hợp khoảng cách, nguy hiểm và ưu tiên vật phẩm
        double heuristicValue = distance + danger * 5 - itemPriority * 3;
        return heuristicValue;
    }

    // Đánh giá mức độ nguy hiểm (bom, Balloon) tại vị trí (x, y)
    private double dangerFactor(int x, int y, Game game) {
        double danger = 0.0;

        // Nguy hiểm từ bom
        List<Bomb> bombs = game.getBombs();
        for (Bomb bomb : bombs) {
            if (bomb.isExploded()) continue;
            int distance = Math.abs(bomb.getX() - x) + Math.abs(bomb.getY() - y);
            if (distance <= bomb.getExplosionRange()) {
                // Tính toán nguy hiểm dựa trên khoảng cách từ bom và thời gian nổ
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

        return Math.min(danger, 1.0); // Đảm bảo giá trị nguy hiểm không vượt quá 1.0
    }

    // Xây dựng đường đi từ các Node (từ mục tiêu về điểm bắt đầu)
    private List<int[]> constructPath(Node node) {
        List<int[]> path = new ArrayList<>();
        while (node.parent != null) {
            path.add(new int[] { node.x, node.y });
            node = node.parent; // Di chuyển lên parent của node
        }
        Collections.reverse(path); // Đảo ngược lại đường đi
        return path;
    }

    // Các hướng di chuyển: Lên, Xuống, Trái, Phải và các hướng chéo
    private int[][] getDirections() {
        return new int[][] {
                { 0, -1 }, { 0, 1 }, { -1, 0 }, { 1, 0 },  // Lên, Xuống, Trái, Phải
                { -1, -1 }, { -1, 1 }, { 1, -1 }, { 1, 1 }  // Các hướng chéo
        };
    }

    private static class Node {
        int x, y;
        Node parent;
        double gScore = Double.MAX_VALUE; // Chi phí từ start đến node này
        double fScore = Double.MAX_VALUE; // Tổng chi phí (gScore + heuristic)

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
