
package ai;

import ai.*;

import java.util.ArrayList;
import java.util.List;

public class EscapeBombsStrategy implements MovementStrategy {
    private Pathfinding pathfinding;

    public EscapeBombsStrategy(GameMap map) {
        this.pathfinding = new Pathfinding(map);
    }

    // Trong EscapeBombsStrategy.java
// Trong EscapeBombsStrategy.java
    @Override
    public void move(Entity entity, Game game) {
        // Xác định mục tiêu là ô an toàn nhất gần nhất
        List<int[]> safePositions = findSafePositions(entity, game);
        if (safePositions.isEmpty()) {
            // Nếu không tìm thấy ô an toàn, di chuyển ngẫu nhiên
            new RandomMovementStrategy().move(entity, game);
            System.out.println(entity.getClass().getSimpleName() + " không tìm thấy ô an toàn, di chuyển ngẫu nhiên.");
            return;
        }

        // Tìm đường đi tới ô an toàn gần nhất
        // **Ưu tiên các ô có nhiều hướng đi để tránh ngõ cụt**
        safePositions.sort((pos1, pos2) -> {
            int directions1 = countAvailableDirections(pos1[0], pos1[1], game.getGameMap());
            int directions2 = countAvailableDirections(pos2[0], pos2[1], game.getGameMap());
            return Integer.compare(directions2, directions1); // Sắp xếp giảm dần
        });

        int[] target = safePositions.get(0); // Lấy ô an toàn với nhiều hướng đi nhất
        List<int[]> path = new Pathfinding(game.getGameMap()).findSafePath(entity.getX(), entity.getY(), target[0], target[1], game);
        if (!path.isEmpty()) {
            int[] nextStep = path.get(0);
            entity.setX(nextStep[0]);
            entity.setY(nextStep[1]);
            System.out.println(entity.getClass().getSimpleName() + " di chuyển tới (" + nextStep[0] + ", " + nextStep[1] + ") để tránh bom.");
        } else {
            // Nếu không tìm được đường đi, di chuyển ngẫu nhiên
            new RandomMovementStrategy().move(entity, game);
            System.out.println(entity.getClass().getSimpleName() + " không tìm được đường đi tới ô an toàn, di chuyển ngẫu nhiên.");
        }
    }

    // Hàm đếm số hướng đi khả dụng từ vị trí (x, y)
    private int countAvailableDirections(int x, int y, GameMap map) {
        int count = 0;
        int[][] directions = {
                {0, -1}, // Lên
                {0, 1},  // Xuống
                {-1, 0}, // Trái
                {1, 0}   // Phải
        };
        for (int[] dir : directions) {
            int newX = x + dir[0];
            int newY = y + dir[1];
            if (map.isWalkable(newX, newY)) {
                count++;
            }
        }
        return count;
    }



    List<int[]> findSafePositions(Entity entity, Game game) {
        // Tìm các ô trên bản đồ mà không bị ảnh hưởng bởi bom
        List<int[]> safePositions = new ArrayList<>();
        GameMap map = game.getGameMap();
        for (int x = 0; x < map.getWidth(); x++) {
            for (int y = 0; y < map.getHeight(); y++) {
                if (map.isWalkable(x, y)) {
                    boolean isSafe = true;
                    for (Bomb bomb : game.getBombs()) {
                        if (!bomb.isExploded()) {
                            int distance = Math.abs(bomb.getX() - x) + Math.abs(bomb.getY() - y);
                            if (distance <= bomb.getExplosionRange()) {
                                isSafe = false;
                                break;
                            }
                        }
                    }
                    if (isSafe) {
                        safePositions.add(new int[]{x, y});
                    }
                }
            }
        }
        return safePositions;
    }
}
