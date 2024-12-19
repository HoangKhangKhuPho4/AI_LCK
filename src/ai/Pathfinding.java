

package ai;

import java.util.*;

/**
 * Lớp thực hiện thuật toán tìm đường đi (Pathfinding) sử dụng A*.
 */
public class Pathfinding {
    private GameMap map;

    public Pathfinding(GameMap map) {
        this.map = map;
    }

    /**
     * Tìm đường đi an toàn từ (startX, startY) đến (goalX, goalY)
     * Tránh các vùng có bom đang sắp nổ hoặc đã đặt bom gần đó và tránh Balloon.
     *
     * @param startX Tọa độ X bắt đầu
     * @param startY Tọa độ Y bắt đầu
     * @param goalX  Tọa độ X đích
     * @param goalY  Tọa độ Y đích
     * @param game   Trạng thái hiện tại của trò chơi
     * @return Danh sách các bước đi từ vị trí bắt đầu đến đích
     */
    public List<int[]> findSafePath(int startX, int startY, int goalX, int goalY, Game game) {
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> n.fScore));
        Map<String, Node> allNodes = new HashMap<>();

        Node start = new Node(startX, startY, null);
        start.gScore = 0;
        start.fScore = heuristicForPathfinding(startX, startY, goalX, goalY, game);  // Sử dụng hàm heuristic mới
        openSet.add(start);
        allNodes.put(start.key(), start);

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();

            if (current.x == goalX && current.y == goalY) {
                return constructPath(current);
            }

            for (int[] dir : getDirections()) {
                int newX = current.x + dir[0];
                int newY = current.y + dir[1];

                if (!map.isWalkable(newX, newY)) continue;

                double danger = dangerFactor(newX, newY, game);
                if (danger > 0.7) continue; // Tránh các vị trí có nguy hiểm cao

                double tentativeGScore = current.gScore + distance(current.x, current.y, newX, newY) + danger;
                String neighborKey = Node.key(newX, newY);
                Node neighbor = allNodes.getOrDefault(neighborKey, new Node(newX, newY, current));

                if (tentativeGScore < neighbor.gScore) {
                    neighbor.parent = current;
                    neighbor.gScore = tentativeGScore;
                    neighbor.fScore = neighbor.gScore + heuristicForPathfinding(newX, newY, goalX, goalY, game) + danger;
                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    }
                    allNodes.put(neighborKey, neighbor);
                }
            }
        }

        // Nếu không tìm được đường đi
        return Collections.emptyList();
    }

    // Hàm tính khoảng cách Manhattan giữa hai điểm
    private double distance(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    // Hàm heuristic cho thuật toán A* tính toán khoảng cách và mức độ nguy hiểm
    private double heuristicForPathfinding(int startX, int startY, int goalX, int goalY, Game game) {
        // Tính toán khoảng cách Manhattan giữa điểm bắt đầu và điểm đích
        double distanceToGoal = distance(startX, startY, goalX, goalY);

        // Tính mức độ nguy hiểm từ các yếu tố trong game (bom, Balloon, v.v.)
        double danger = dangerFactor(startX, startY, game);

        // Trả về tổng hợp của khoảng cách và mức độ nguy hiểm
        return distanceToGoal + danger;
    }

    // Hàm đánh giá mức độ nguy hiểm tại vị trí (x, y)
    private double dangerFactor(int x, int y, Game game) {
        double danger = 0.0;

        // Nguy hiểm từ bom
        for (Bomb bomb : game.getBombs()) {
            if (!bomb.isExploded()) {
                int distance = Math.abs(bomb.getX() - x) + Math.abs(bomb.getY() - y);
                if (distance <= bomb.getExplosionRange()) {
                    double timeUntilExplosion = bomb.getCountdown() / 10.0;
                    danger += (1.0 - ((double) distance / bomb.getExplosionRange())) / timeUntilExplosion;
                }
            }
        }

        // Nguy hiểm từ Balloon
        for (Balloon balloon : game.getBalloons()) {
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

    // Hàm xây dựng đường đi từ Node
    private List<int[]> constructPath(Node node) {
        List<int[]> path = new ArrayList<>();
        while (node.parent != null) {
            path.add(new int[]{node.x, node.y});
            node = node.parent;
        }
        Collections.reverse(path);
        return path;
    }

    // Các hướng di chuyển: Lên, Xuống, Trái, Phải
    private int[][] getDirections() {
        return new int[][]{
                {0, -1}, // Lên
                {0, 1},  // Xuống
                {-1, 0}, // Trái
                {1, 0}   // Phải
        };
    }

    // Lớp Node nội bộ cho thuật toán A*
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

    // Hàm đánh giá mức độ nguy hiểm từ bom, Balloon (giữ lại nếu cần thiết cho Minimax)
    private boolean canPlaceBombNearPlayer(Node state) {
        // Kiểm tra xem AI có thể đặt bom gần vị trí của người chơi không
        // Logic kiểm tra vị trí xung quanh người chơi và xem có thể đặt bom hay không.
        return false; // Placeholder: Cần thực hiện logic cụ thể cho việc kiểm tra bom
    }
}

