// File: ai/Pathfinding.java
package ai;

import java.util.*;

public class Pathfinding {
    private GameMap map;
    private Game game;

    public Pathfinding(GameMap map) {
        this.map = map;
        this.game = game;

    }

    /**
     * Tìm đường từ (startX, startY) đến (goalX, goalY) sử dụng thuật toán BFS 
     */
    public List<int[]> findPath(int startX, int startY, int goalX, int goalY) {
        Queue<Node> queue = new LinkedList<>();
        Map<String, Node> allNodes = new HashMap<>();
        Node start = new Node(startX, startY, null);
        queue.add(start);
        allNodes.put(start.key(), start);
        while (!queue.isEmpty()) {
            Node current = queue.poll();
            if (current.x == goalX && current.y == goalY) {
                return constructPath(current);
            }
            for (int[] dir : getDirections()) {
                int newX = current.x + dir[0];
                int newY = current.y + dir[1];
                if (!map.isWalkable(newX, newY)) continue;
                if (allNodes.containsKey(Node.key(newX, newY))) continue;
                Node neighbor = new Node(newX, newY, current);
                allNodes.put(neighbor.key(), neighbor);
                queue.add(neighbor);
            }
        }
        return Collections.emptyList(); // Không tìm thấy đường
    }

    private List<int[]> constructPath(Node node) {
        List<int[]> path = new ArrayList<>();
        while (node.parent != null) {
            path.add(new int[]{node.x, node.y});
            node = node.parent;
        }
        Collections.reverse(path);
        return path;
    }

    private int[][] getDirections() {
        return new int[][]{
                {0, -1}, // Lên
                {0, 1},  // Xuống
                {-1, 0}, // Trái
                {1, 0}   // Phải
        };
    }

    private static class Node {
        int x, y;
        Node parent;

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
    }
}