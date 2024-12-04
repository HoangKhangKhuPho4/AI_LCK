
package ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lớp triển khai chiến lược di chuyển sử dụng thuật toán Minimax với Alpha-Beta Pruning và Transposition Table.
 */
public class MinimaxStrategy implements MovementStrategy {
    private int maxDepth;
    private boolean isMaximizingPlayer;

    // Transposition Table để lưu trữ kết quả đánh giá các trạng thái
    private Map<String, Double> transpositionTable;

    public MinimaxStrategy(int maxDepth, boolean isMaximizingPlayer) {
        this.maxDepth = maxDepth;
        this.isMaximizingPlayer = isMaximizingPlayer;
        this.transpositionTable = new HashMap<>();
    }

    @Override
    public void move(Entity entity, Game game) {
        // Clone the game state
        Game clonedGame = game.clone();
        // Get the best action using Minimax
        MinimaxResult result = minimax(clonedGame, maxDepth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, isMaximizingPlayer);
        Action bestAction = result.action;

        if (bestAction != null) {
            System.out.println("Minimax chọn hành động: " + bestAction.getActionType());
            // Thực hiện hành động tốt nhất trên thực thể thực tế
            executeAction(entity, game, bestAction);
        } else {
            System.out.println("Minimax không tìm thấy hành động nào.");
        }
    }

    /**
     * Lớp nội bộ để lưu kết quả của Minimax.
     */
    private class MinimaxResult {
        Action action;
        double score;

        MinimaxResult(Action action, double score) {
            this.action = action;
            this.score = score;
        }
    }

    /**
     * Thuật toán Minimax với Alpha-Beta Pruning và Transposition Table.
     */
    private MinimaxResult minimax(Game state, int depth, double alpha, double beta, boolean maximizingPlayer) {
        // Kiểm tra trong Transposition Table trước
        String stateKey = state.getStateHash();
        if (transpositionTable.containsKey(stateKey)) {
            double cachedValue = transpositionTable.get(stateKey);
            return new MinimaxResult(null, cachedValue); // Trả về giá trị đã lưu
        }

        if (depth == 0 || state.isGameOver() || state.isGameWon()) {
            double eval = evaluateState(state);
            // Lưu kết quả vào Transposition Table
            transpositionTable.put(stateKey, eval);
            return new MinimaxResult(null, eval);
        }

        List<Action> possibleActions = generatePossibleActions(state, maximizingPlayer);
        Action bestAction = null;

        if (maximizingPlayer) {
            double maxEval = Double.NEGATIVE_INFINITY;
            for (Action action : possibleActions) {
                Game clonedState = state.clone();
                Entity currentEntity = clonedState.getPlayer();
                executeAction(currentEntity, clonedState, action);
                clonedState.update();
                MinimaxResult result = minimax(clonedState, depth - 1, alpha, beta, false);
                double eval = result.score;
                if (eval > maxEval) {
                    maxEval = eval;
                    bestAction = action;
                }
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) {
                    break; // Beta cut-off
                }
            }
            // Lưu kết quả vào Transposition Table
            transpositionTable.put(stateKey, maxEval);
            return new MinimaxResult(bestAction, maxEval);
        } else {
            double minEval = Double.POSITIVE_INFINITY;
            for (Action action : possibleActions) {
                Game clonedState = state.clone();
                Entity currentEntity = clonedState.getAiPlayer();
                executeAction(currentEntity, clonedState, action);
                clonedState.update();
                MinimaxResult result = minimax(clonedState, depth - 1, alpha, beta, true);
                double eval = result.score;
                if (eval < minEval) {
                    minEval = eval;
                    bestAction = action;
                }
                beta = Math.min(beta, eval);
                if (beta <= alpha) {
                    break; // Alpha cut-off
                }
            }
            // Lưu kết quả vào Transposition Table
            transpositionTable.put(stateKey, minEval);
            return new MinimaxResult(bestAction, minEval);
        }
    }

    /**
     * Tạo danh sách các hành động có thể cho người chơi hoặc AI.
     */
    private List<Action> generatePossibleActions(Game state, boolean isMaximizingPlayer) {
        List<Action> actions = new ArrayList<>();
        // Thêm chỉ các hành động di chuyển
        actions.add(new Action(ActionType.MOVE_UP));
        actions.add(new Action(ActionType.MOVE_DOWN));
        actions.add(new Action(ActionType.MOVE_LEFT));
        actions.add(new Action(ActionType.MOVE_RIGHT));
        // Thêm hành động đặt bom nếu cần
        if (isMaximizingPlayer && state.getAiPlayer().getBombCount() > 0) {
            actions.add(new Action(ActionType.PLACE_BOMB));
        }
        return actions;
    }

    /**
     * Hàm đánh giá trạng thái trò chơi.
     */
    private double evaluateState(Game state) {
        if (state.isGameWon()) {
            return 1000;
        }
        if (state.isGameOver()) {
            return -1000;
        }

        double score = 0.0;

        // Khoảng cách giữa người chơi và AI
        int distance = Math.abs(state.getPlayer().getX() - state.getAiPlayer().getX()) +
                Math.abs(state.getPlayer().getY() - state.getAiPlayer().getY());
        score -= distance * 10; // Người chơi càng gần AI, điểm càng thấp

        // Số bom còn lại
        score += state.getAiPlayer().getBombCount() * 20;
        score -= state.getPlayer().getBombCount() * 20;

        // Phạm vi nổ
        score += state.getPlayer().getExplosionRange() * 15;
        score += state.getAiPlayer().getExplosionRange() * 15;

        // Mức độ nguy hiểm từ bom
        for (Bomb bomb : state.getBombs()) {
            if (!bomb.isExploded()) {
                int bombDistance = Math.abs(bomb.getX() - state.getPlayer().getX()) +
                        Math.abs(bomb.getY() - state.getPlayer().getY());
                if (bombDistance <= bomb.getExplosionRange()) {
                    score -= (bomb.getExplosionRange() - bombDistance + 1) * 50;
                }
            }
        }

        // Đánh giá về vật phẩm
        for (Item item : state.getGameMap().getItems()) {
            int itemDistance = Math.abs(item.getX() - state.getAiPlayer().getX()) +
                    Math.abs(item.getY() - state.getAiPlayer().getY());
            if (item.getType() == Item.ItemType.SPEED) {
                score += 30 / (itemDistance + 1); // Ưu tiên gần hơn
            } else if (item.getType() == Item.ItemType.EXPLOSION_RANGE) {
                score += 40 / (itemDistance + 1); // Ưu tiên gần hơn
            }
        }

        // Đánh giá về Balloon
        for (Balloon balloon : state.getBalloons()) {
            if (balloon.isAlive()) {
                int balloonDistance = Math.abs(balloon.getX() - state.getAiPlayer().getX()) +
                        Math.abs(balloon.getY() - state.getAiPlayer().getY());
                score -= balloonDistance * 5; // Tránh gần Balloon
            }
        }

        return score;
    }

    /**
     * Thực hiện hành động trên một thực thể trong trò chơi.
     */
    private void executeAction(Entity entity, Game game, Action action) {
        switch (action.getActionType()) {
            case MOVE_UP:
                game.moveEntity(entity, 0, -1);
                break;
            case MOVE_DOWN:
                game.moveEntity(entity, 0, 1);
                break;
            case MOVE_LEFT:
                game.moveEntity(entity, -1, 0);
                break;
            case MOVE_RIGHT:
                game.moveEntity(entity, 1, 0);
                break;
            case PLACE_BOMB:
                game.placeBomb(entity);
                break;
            case STAY:
                // Không làm gì
                break;
        }
    }
}
