package ai;

/**
 * Chiến lược Hybrid kết hợp giữa Q-Learning và Minimax.
 */
public class HybridStrategy implements MovementStrategy {
    private QLearningStrategy qLearningStrategy;
    private MinimaxStrategy minimaxStrategy;
    private double minimaxThreshold; // Ngưỡng quyết định khi nào sử dụng Minimax

    public HybridStrategy(QLearningStrategy qLearningStrategy, MinimaxStrategy minimaxStrategy, double minimaxThreshold) {
        this.qLearningStrategy = qLearningStrategy;
        this.minimaxStrategy = minimaxStrategy;
        this.minimaxThreshold = minimaxThreshold;
    }

    @Override
    public void move(Entity entity, Game game) {
        // Xác định khi nào nên sử dụng Minimax
        // Ví dụ: Khi người chơi ở gần hoặc trong tầm nổ
        AIPlayer aiPlayer = (AIPlayer) entity;
        Player player = game.getPlayer();
        double distance = Math.abs(aiPlayer.getX() - player.getX()) + Math.abs(aiPlayer.getY() - player.getY());

        // Ngưỡng có thể điều chỉnh tùy theo trò chơi
        if (distance <= minimaxThreshold) {
            minimaxStrategy.move(entity, game);
        } else {
            qLearningStrategy.move(entity, game);
        }
    }
}
