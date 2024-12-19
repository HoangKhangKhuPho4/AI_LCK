

package ai;

import java.util.List;

/**
 * Chiến lược đặt bom cho AIPlayer.
 */
public class BombPlacementStrategy implements MovementStrategy {
    private double bombPlacementThreshold = 0.7; // Ngưỡng đánh giá để đặt bom

    @Override
    public void move(Entity entity, Game game) {
        // Không thực hiện di chuyển trong chiến lược đặt bom
        // Chiến lược này chỉ tập trung vào việc đặt bom
    }



    /**
     * Quyết định đặt bom dựa trên vị trí hiện tại và tình trạng trò chơi.
     * @param entity AIPlayer thực hiện đặt bom.
     * @param game Trạng thái hiện tại của trò chơi.
     */
    public void placeBomb(Entity entity, Game game) {
        // Đánh giá vị trí hiện tại để quyết định đặt bom
        if (shouldPlaceBomb(entity, game)) {
            game.placeBomb(entity);
            System.out.println(entity.getClass().getSimpleName() + " đã đặt bom tại (" + entity.getX() + ", " + entity.getY() + ").");
        }
    }

    /**
     * Đánh giá xem có nên đặt bom tại vị trí hiện tại không.
     * @param entity AIPlayer.
     * @param game Trạng thái trò chơi.
     * @return true nếu nên đặt bom, false nếu không.
     */
    private boolean shouldPlaceBomb(Entity entity, Game game) {
        // Kiểm tra số lượng bom còn lại
        if (entity.getBombCount() <= 0) {
            return false;
        }

        // Đánh giá mức độ nguy hiểm xung quanh
        double danger = calculateDanger(entity, game);
        if (danger < bombPlacementThreshold) {
            return false;
        }

        // Kiểm tra xem sau khi đặt bom, AIPlayer có thể thoát khỏi vùng nổ không
        if (!canEscapeAfterPlacingBomb(entity, game)) {
            return false;
        }

        return true;
    }

    /**
     * Tính toán mức độ nguy hiểm tại vị trí hiện tại.
     * @param entity AIPlayer.
     * @param game Trạng thái trò chơi.
     * @return Mức độ nguy hiểm từ 0.0 đến 1.0.
     */
    private double calculateDanger(Entity entity, Game game) {
        double danger = 0.0;

        // Nguy hiểm từ bom đang có
        for (Bomb bomb : game.getBombs()) {
            if (!bomb.isExploded()) {
                int distance = Math.abs(bomb.getX() - entity.getX()) + Math.abs(bomb.getY() - entity.getY());
                if (distance <= bomb.getExplosionRange()) {
                    danger += 1.0 - ((double) distance / bomb.getExplosionRange());
                }
            }
        }

        // Nguy hiểm từ Balloon
        for (Balloon balloon : game.getBalloons()) {
            if (!balloon.isAlive()) continue;
            int distance = Math.abs(balloon.getX() - entity.getX()) + Math.abs(balloon.getY() - entity.getY());
            if (distance <= 2) { // Nguy hiểm gần
                danger += 0.5;
            }
        }

        // Đảm bảo mức độ nguy hiểm không vượt quá 1.0
        return Math.min(danger, 1.0);
    }

    /**
     * Kiểm tra xem AIPlayer có thể thoát khỏi vùng nổ sau khi đặt bom không.
     * @param entity AIPlayer.
     * @param game Trạng thái trò chơi.
     * @return true nếu có thể thoát, false nếu không.
     */
    private boolean canEscapeAfterPlacingBomb(Entity entity, Game game) {
        // Tạm thời đặt bom và kiểm tra khả năng di chuyển
        Game clonedGame = game.clone();
        clonedGame.placeBomb(entity);

        // Sử dụng chiến lược tìm đường đi an toàn để xem AIPlayer có thể di chuyển ra khỏi vùng nổ không
        MovementStrategy escapeStrategy = new EscapeBombsStrategy(clonedGame.getGameMap());
        List<int[]> safePositions = ((EscapeBombsStrategy) escapeStrategy).findSafePositions(entity, clonedGame);

        return !safePositions.isEmpty();
    }
}
