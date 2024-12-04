package ai;

import java.util.*;

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
            if (bestAction.getActionType() == ActionType.PLACE_BOMB) {
                System.out.println("AI sẽ đặt bom tại (" + bestAction.getTargetX() + ", " + bestAction.getTargetY() + ").");
            }
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
                Entity currentEntity = clonedState.getAiPlayer(); // Đúng: AIPlayer là người tối đa hóa
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
                Entity currentEntity = clonedState.getPlayer(); // Người chơi là người tối thiểu hóa
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
    // Trong MinimaxStrategy.java, trong hàm generatePossibleActions
    // Trong MinimaxStrategy.java, trong hàm generatePossibleActions
    private List<Action> generatePossibleActions(Game state, boolean isMaximizingPlayer) {
        List<Action> actions = new ArrayList<>();
        // Thêm các hành động di chuyển
        actions.add(new Action(ActionType.MOVE_UP));
        actions.add(new Action(ActionType.MOVE_DOWN));
        actions.add(new Action(ActionType.MOVE_LEFT));
        actions.add(new Action(ActionType.MOVE_RIGHT));

        // Thêm hành động đặt bom tại các vị trí tiềm năng
        if (isMaximizingPlayer && state.getAiPlayer().getBombCount() > 0) {
            List<int[]> potentialBombPositions = getPotentialBombPositions(state);
            for (int[] pos : potentialBombPositions) {
                actions.add(new Action(ActionType.PLACE_BOMB, pos[0], pos[1]));
            }
        }

        // **Xáo trộn danh sách hành động để tránh ưu tiên cố định**
        Collections.shuffle(actions, new Random());

        return actions;
    }


    private List<int[]> getPotentialBombPositions(Game state) {
        List<int[]> positions = new ArrayList<>();
        AIPlayer aiPlayer = state.getAiPlayer();
        int x = aiPlayer.getX();
        int y = aiPlayer.getY();

        // Thêm vị trí hiện tại để đặt bom
        positions.add(new int[]{x, y});

        // Thêm các vị trí lân cận
        int[][] directions = {{0, -1}, // Lên
                {0, 1},  // Xuống
                {-1, 0}, // Trái
                {1, 0}   // Phải
        };
        for (int[] dir : directions) {
            int newX = x + dir[0];
            int newY = y + dir[1];
            if (state.getGameMap().isWalkable(newX, newY)) {
                positions.add(new int[]{newX, newY});
            }
        }
        return positions;
    }

    /**
     * Hàm đánh giá trạng thái trò chơi.
     */
    // Trong MinimaxStrategy.java
// Trong MinimaxStrategy.java, trong hàm evaluateState
    private double evaluateState(Game state) {
        if (state.isGameWon()) {
            return 1000;
        }
        if (state.isGameOver()) {
            return -1000;
        }
        double score = 0.0;

        // Khoảng cách giữa người chơi và AI
        int distance = Math.abs(state.getPlayer().getX() - state.getAiPlayer().getX()) + Math.abs(state.getPlayer().getY() - state.getAiPlayer().getY());
        score -= distance * 10; // Người chơi càng gần AI, điểm càng thấp

        // An toàn của vị trí hiện tại
        if (!state.getAiPlayer().isSafe(state)) {
            score -= 50; // Phạt nếu AI không an toàn
        }

        // Số bom còn lại và phạm vi nổ
        score += state.getAiPlayer().getBombCount() * 20;
        score -= state.getPlayer().getBombCount() * 20;
        score += state.getPlayer().getExplosionRange() * 15;
        score += state.getAiPlayer().getExplosionRange() * 15;

        // Nguy hiểm từ bom
        for (Bomb bomb : state.getBombs()) {
            if (!bomb.isExploded()) {
                int bombDistance = Math.abs(bomb.getX() - state.getPlayer().getX()) + Math.abs(bomb.getY() - state.getPlayer().getY());
                if (bombDistance <= bomb.getExplosionRange()) {
                    score -= (bomb.getExplosionRange() - bombDistance + 1) * 50;
                }
            }
        }

        // Ưu tiên thu thập vật phẩm
        for (Item item : state.getGameMap().getItems()) {
            int itemDistance = Math.abs(item.getX() - state.getAiPlayer().getX()) + Math.abs(item.getY() - state.getAiPlayer().getY());
            if (item.getType() == Item.ItemType.SPEED) {
                score += 30 / (itemDistance + 1);
            } else if (item.getType() == Item.ItemType.EXPLOSION_RANGE) {
                score += 40 / (itemDistance + 1);
            }
        }

        // Đánh giá về Balloon
        for (Balloon balloon : state.getBalloons()) {
            if (balloon.isAlive()) {
                int balloonDistance = Math.abs(balloon.getX() - state.getAiPlayer().getX()) + Math.abs(balloon.getY() - state.getAiPlayer().getY());
                score -= balloonDistance * 5; // Tránh gần Balloon
            }
        }

        // Thêm điểm thưởng khi đặt bom gần người chơi hoặc Balloon
        if (distance <= 3 && state.getAiPlayer().getBombCount() > 0) {
            score += 100;
        }
        for (Balloon balloon : state.getBalloons()) {
            if (balloon.isAlive()) {
                int bombProximity = Math.abs(balloon.getX() - state.getAiPlayer().getX()) + Math.abs(balloon.getY() - state.getAiPlayer().getY());
                if (bombProximity <= state.getAiPlayer().getExplosionRange()) {
                    score += 50;
                }
            }
        }

        // **Phạt khi AI bị dồn vào ngõ cụt**
        if (isCornered(state.getAiPlayer(), state.getGameMap())) {
            score -= 150; // Giảm điểm thay vì tăng
        }

        // **Thêm điểm thưởng cho các vị trí có nhiều hướng đi hơn**
        int availableDirections = 0;
        int x = state.getAiPlayer().getX();
        int y = state.getAiPlayer().getY();
        int[][] directions = {{0, -1}, // Lên
                {0, 1},  // Xuống
                {-1, 0}, // Trái
                {1, 0}   // Phải
        };
        for (int[] dir : directions) {
            int newX = x + dir[0];
            int newY = y + dir[1];
            if (state.getGameMap().isWalkable(newX, newY)) {
                availableDirections++;
            }
        }
        score += availableDirections * 10; // Ưu tiên các vị trí có nhiều hướng đi hơn

        return score;
    }


    // Hàm kiểm tra xem AIPlayer có bị dồn vào ngõ cụt không
    private boolean isCornered(AIPlayer aiPlayer, GameMap map) {
        int x = aiPlayer.getX();
        int y = aiPlayer.getY();
        int walkable = 0;
        int[][] directions = {{0, -1}, // Lên
                {0, 1},  // Xuống
                {-1, 0}, // Trái
                {1, 0}   // Phải
        };
        for (int[] dir : directions) {
            int newX = x + dir[0];
            int newY = y + dir[1];
            if (map.isWalkable(newX, newY)) {
                walkable++;
            }
        }
        return walkable <= 1; // Nếu có ít hơn hoặc bằng 1 hướng đi, coi như bị dồn vào ngõ cụt
    }


    /**
     * Thực hiện hành động trên một thực thể trong trò chơi.
     */
    // Trong MinimaxStrategy.java, trong hàm executeAction
    private void executeAction(Entity entity, Game game, Action action) {
        switch (action.getActionType()) {
            case MOVE_UP:
                game.moveEntity(entity, 0, -1);
                System.out.println(entity.getClass().getSimpleName() + " di chuyển lên.");
                break;
            case MOVE_DOWN:
                game.moveEntity(entity, 0, 1);
                System.out.println(entity.getClass().getSimpleName() + " di chuyển xuống.");
                break;
            case MOVE_LEFT:
                game.moveEntity(entity, -1, 0);
                System.out.println(entity.getClass().getSimpleName() + " di chuyển trái.");
                break;
            case MOVE_RIGHT:
                game.moveEntity(entity, 1, 0);
                System.out.println(entity.getClass().getSimpleName() + " di chuyển phải.");
                break;
            case PLACE_BOMB:
                int bombX = action.getTargetX();
                int bombY = action.getTargetY();
                if (canPlaceBombSafely(game, entity, bombX, bombY)) {
                    Bomb newBomb = new Bomb(bombX, bombY, 30, entity, entity.getExplosionRange());
                    game.addBomb(newBomb);
                    entity.placeBomb();
                    System.out.println(entity.getClass().getSimpleName() + " đặt bom tại (" + bombX + ", " + bombY + ").");
                    // **Thực hiện di chuyển thoát ngay sau khi đặt bom**
                    MovementStrategy escapeStrategy = new EscapeBombsStrategy(game.getGameMap());
                    escapeStrategy.move(entity, game);
                    System.out.println(entity.getClass().getSimpleName() + " di chuyển ra khỏi khu vực nổ.");
                } else {
                    System.out.println("Không thể đặt bom tại (" + bombX + ", " + bombY + ") vì không có đường thoát an toàn.");
                }
                break;

            case STAY:
                System.out.println(entity.getClass().getSimpleName() + " ở lại.");
                break;
        }
    }


    // Trong MinimaxStrategy.java
    private boolean canPlaceBombSafely(Game game, Entity entity, int bombX, int bombY) {
        // Tạm thời đặt bom và kiểm tra xem AI còn đường thoát hay không
        Game clonedGame = game.clone();
        clonedGame.placeBomb(entity); // Đặt bom trên bản sao
        clonedGame.update(); // Cập nhật trạng thái sau khi đặt bom

        // Kiểm tra xem có lối thoát an toàn nào cho AI không
        List<int[]> safePositions = new EscapeBombsStrategy(clonedGame.getGameMap()).findSafePositions(entity, clonedGame);
        return !safePositions.isEmpty();
    }


}
