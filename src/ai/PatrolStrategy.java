package ai;

import ai.Entity;
import ai.Game;
import ai.MovementStrategy;
import ai.RandomMovementStrategy;

import java.util.List;

public class PatrolStrategy implements MovementStrategy {
    private List<int[]> patrolPath;
    private int patrolIndex = 0;

    public PatrolStrategy(List<int[]> patrolPath) {
        this.patrolPath = patrolPath;
    }

    @Override
    public void move(Entity entity, Game game) {
        if (patrolPath == null || patrolPath.isEmpty()) {
            // Nếu không có đường tuần tra, di chuyển ngẫu nhiên
            new RandomMovementStrategy().move(entity, game);
            return;
        }

        int[] currentPosition = new int[]{entity.getX(), entity.getY()};
        int[] targetPosition = patrolPath.get(patrolIndex);

        // Xác định hướng di chuyển cần thiết
        int dx = 0, dy = 0;
        if (targetPosition[0] > currentPosition[0]) {
            dx = 1;
        } else if (targetPosition[0] < currentPosition[0]) {
            dx = -1;
        } else if (targetPosition[1] > currentPosition[1]) {
            dy = 1;
        } else if (targetPosition[1] < currentPosition[1]) {
            dy = -1;
        }

        // Nếu đã đạt đến vị trí mục tiêu, chuyển sang vị trí tiếp theo trong đường tuần tra
        if (currentPosition[0] == targetPosition[0] && currentPosition[1] == targetPosition[1]) {
            patrolIndex = (patrolIndex + 1) % patrolPath.size();
            targetPosition = patrolPath.get(patrolIndex);
            dx = 0;
            dy = 0;
            if (targetPosition[0] > currentPosition[0]) {
                dx = 1;
            } else if (targetPosition[0] < currentPosition[0]) {
                dx = -1;
            } else if (targetPosition[1] > currentPosition[1]) {
                dy = 1;
            } else if (targetPosition[1] < currentPosition[1]) {
                dy = -1;
            }
        }

        // Thực hiện di chuyển một bước theo hướng đã xác định
        if (dx != 0 || dy != 0) {
            // Kiểm tra xem có thể di chuyển tới vị trí mới không
            int newX = currentPosition[0] + dx;
            int newY = currentPosition[1] + dy;
            if (game.getGameMap().isWalkable(newX, newY)) {
                game.moveEntity(entity, dx, dy);
                System.out.println(entity.getClass().getSimpleName() + " đã di chuyển đến (" + newX + ", " + newY + ") theo đường tuần tra.");
            } else {
                // Nếu không thể di chuyển tới hướng đó, chuyển sang hành động ngẫu nhiên hoặc tiếp tục đường tuần tra
                System.out.println(entity.getClass().getSimpleName() + " không thể di chuyển tới (" + newX + ", " + newY + "), cố gắng tiếp tục đường tuần tra.");
                patrolIndex = (patrolIndex + 1) % patrolPath.size(); // Chuyển sang vị trí tiếp theo
            }
        }
    }
}
