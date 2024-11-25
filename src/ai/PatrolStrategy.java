// File: ai/PatrolStrategy.java
package ai;

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
        patrolIndex = (patrolIndex + 1) % patrolPath.size();
        int[] nextPosition = patrolPath.get(patrolIndex);
        if (game.getGameMap().isWalkable(nextPosition[0], nextPosition[1])) {
            entity.setX(nextPosition[0]);
            entity.setY(nextPosition[1]);
        } else {
            new RandomMovementStrategy().move(entity, game);
        }
    }
} 