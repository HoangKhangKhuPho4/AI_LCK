// File: ai/ChasePlayerStrategy.java
package ai;

import java.util.List;

public class ChasePlayerStrategy implements MovementStrategy {
    @Override
    public void move(Entity entity, Game game) {
        Player player = game.getPlayer();
        Pathfinding pathfinding = new Pathfinding(game.getGameMap());
        List<int[]> path = pathfinding.findPath(entity.getX(), entity.getY(), player.getX(), player.getY());
        if (path.size() > 0) {
            int[] nextStep = path.get(0);
            entity.setX(nextStep[0]);
            entity.setY(nextStep[1]);
        } else {
            // Nếu không tìm thấy đường, di chuyển ngẫu nhiên
            new RandomMovementStrategy().move(entity, game);
        }
    }
}