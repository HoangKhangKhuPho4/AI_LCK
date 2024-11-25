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
    private Map<String, List<int[]>> pathCache = new LinkedHashMap<String, List<int[]>>(100, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, List<int[]>> eldest) {
            return this.size() > 100; // Giới hạn cache tối đa 100 đường đi
        }
    };

    public Pathfinding(GameMap map) {
        this.map = map;
        this.pathCache = new HashMap<>();
    }

    /**
     * Tìm đường đi an toàn từ (startX, startY) đến (goalX, goalY) Tránh các vùng có
     * bom đang sắp nổ hoặc đã đặt bom gần đó và tránh Balloon
     */
    public List<int[]> findSafePath(int startX, int startY, int goalX, int goalY, Game game) {
        String key = "safe:" + startX + "," + startY + "->" + goalX + "," + goalY;
        if (pathCache.containsKey(key)) {
            return pathCache.get(key);
        }

        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> n.fScore));
        Map<String, Node> allNodes = new HashMap<>();
        Node start = new Node(startX, startY, null);
        start.gScore = 0;
        start.fScore = heuristic(startX, startY, goalX, goalY, game);
        openSet.add(start);
        allNodes.put(start.key(), start);

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            if (current.x == goalX && current.y == goalY) {
                List<int[]> path = constructPath(current);
                pathCache.put(key, path);
                return path;
            }

            for (int[] dir : getDirections()) {
                int newX = current.x + dir[0];
                int newY = current.y + dir[1];

                if (!map.isWalkable(newX, newY))
                    continue;

                // Đánh giá mức độ nguy hiểm tại ô mới
                double danger = dangerFactor(newX, newY, game);
                if (danger > 0.7)
                    continue; // Bỏ qua ô quá nguy hiểm

                double tentativeGScore = current.gScore + distance(current.x, current.y, newX, newY) + danger;
                String neighborKey = Node.key(newX, newY);
                Node neighbor = allNodes.getOrDefault(neighborKey, new Node(newX, newY, current));

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

        return Collections.emptyList(); // Không tìm thấy đường an toàn
    }

    private double distance(int x1, int y1, int x2, int y2) {
        // Khoảng cách giữa hai ô, ở đây là 1 đơn vị di chuyển
        return 1.0;
    }

    private double heuristic(int x, int y, int goalX, int goalY, Game game) {
        // Khoảng cách Manhattan
        double distance = Math.abs(goalX - x) + Math.abs(goalY - y);

        // Thêm yếu tố nguy hiểm từ bom
        double danger = dangerFactor(x, y, game);

        // Thêm ưu tiên cho vật phẩm gần đường đi
        double itemPriority = 0.0;
        for (Item item : game.getGameMap().getItems()) {
            int itemDistance = Math.abs(item.getX() - x) + Math.abs(item.getY() - y);
            if (itemDistance == 0)
                continue; // Tránh chia cho 0
            itemPriority += (item.getType() == Item.ItemType.SPEED) ? 1.0 / itemDistance : 1.5 / itemDistance;
        }

        // Trọng số các yếu tố
        double heuristicValue = distance + danger * 10 - itemPriority * 5;
        return heuristicValue;
    }

    private double dangerFactor(int x, int y, Game game) {
        double danger = 0.0;

        // Đánh giá nguy hiểm từ bom
        List<Bomb> bombs = game.getBombs();
        for (Bomb bomb : bombs) {
            if (bomb.isExploded())
                continue; // Bỏ qua bom đã nổ
            int distance = Math.abs(bomb.getX() - x) + Math.abs(bomb.getY() - y);
            if (distance <= bomb.getExplosionRange()) {
                // Mức độ nguy hiểm tăng khi gần bom hơn
                double timeUntilExplosion = bomb.getCountdown() / 10.0; // Giả sử 10 ticks = 1 giây
                danger += (1.0 - ((double) distance / bomb.getExplosionRange())) / timeUntilExplosion;
            }
        }

        // Đánh giá nguy hiểm từ Balloon
        List<Balloon> balloons = game.getBalloons();
        for (Balloon balloon : balloons) {
            if (!balloon.isAlive())
                continue;
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

    private List<int[]> constructPath(Node node) {
        List<int[]> path = new ArrayList<>();
        while (node.parent != null) {
            path.add(new int[] { node.x, node.y });
            node = node.parent;
        }
        Collections.reverse(path);
        return path;
    }

    private int[][] getDirections() {
        return new int[][] { { 0, -1 }, // Lên
                { 0, 1 }, // Xuống
                { -1, 0 }, // Trái
                { 1, 0 } // Phải
        };
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