// File: ai/EscapeBombsStrategy.java
package ai;

import java.util.List;

public class EscapeBombsStrategy implements MovementStrategy {
    private Pathfinding pathfinding;

    public EscapeBombsStrategy(GameMap map) {
        this.pathfinding = new Pathfinding(map);
    }

    @Override
    public void move(Entity entity, Game game) {
        // Tìm đường đi an toàn thoát khỏi các vùng bom
        // Đặt mục tiêu là vị trí không có bom trong phạm vi
        // Ở đây, chúng ta có thể đặt mục tiêu là vị trí hiện tại để tìm lối thoát
        List<int[]> safePath = pathfinding.findSafePath(entity.getX(), entity.getY(), entity.getX(), entity.getY(), game);
        if (safePath.size() > 0) {
            int[] nextStep = safePath.get(0);
            entity.setX(nextStep[0]);
            entity.setY(nextStep[1]);
            System.out.println(entity.getClass().getSimpleName() + " đã di chuyển đến (" + nextStep[0] + ", " + nextStep[1] + ") để tránh bom.");
        } else {
            // Nếu không tìm được đường an toàn, di chuyển ngẫu nhiên
            new RandomMovementStrategy().move(entity, game);
            System.out.println(entity.getClass().getSimpleName() + " không tìm được đường an toàn, di chuyển ngẫu nhiên.");
        }
    }
}