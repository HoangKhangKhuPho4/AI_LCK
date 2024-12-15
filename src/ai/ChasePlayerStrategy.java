

// File: ai/ChasePlayerStrategy.java
package ai;

import java.util.List;

public class ChasePlayerStrategy implements MovementStrategy {
    private Pathfinding pathfinding;

    public ChasePlayerStrategy(GameMap map) {
        this.pathfinding = new Pathfinding(map);
    }

    @Override
    public void move(Entity entity, Game game) {
        Player player = game.getPlayer();
        // Tìm đường đi an toàn đến người chơi
        List<int[]> path = pathfinding.findSafePath(entity.getX(), entity.getY(), player.getX(), player.getY(), game);
        if (path.size() > 0) {
            int[] nextStep = path.get(0);
            entity.setX(nextStep[0]);
            entity.setY(nextStep[1]);
            System.out.println(entity.getClass().getSimpleName() + " đã di chuyển đến (" + nextStep[0] + ", " + nextStep[1] + ") theo đường an toàn.");
        } else {
            // Nếu không tìm được đường an toàn, di chuyển ngẫu nhiên
            new RandomMovementStrategy().move(entity, game);
            System.out.println(entity.getClass().getSimpleName() + " không tìm được đường an toàn, di chuyển ngẫu nhiên.");
        }
    }
}
