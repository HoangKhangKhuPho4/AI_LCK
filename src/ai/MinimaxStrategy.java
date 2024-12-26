
package ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Lớp triển khai chiến lược di chuyển sử dụng thuật toán Minimax với Alpha-Beta Pruning.
 */
public class MinimaxStrategy implements MovementStrategy {
    private final boolean ismaximizingPlayer;
    private int maxDepth;

    /**
     * Constructor để khởi tạo MinimaxStrategy với độ sâu tối đa.
     *
     * @param maxDepth Độ sâu tối đa của thuật toán Minimax.
     */
    public MinimaxStrategy(int maxDepth , boolean isMaximizingPlayer) {
        this.maxDepth = maxDepth;
        this.ismaximizingPlayer = isMaximizingPlayer;
    }



    /**
     * Hàm chính của thuật toán Minimax với Alpha-Beta Pruning.
     *
     * @param state Trạng thái hiện tại của Node.
     * @param depth Độ sâu còn lại.
     * @param alpha Giá trị alpha trong Alpha-Beta Pruning.
     * @param beta  Giá trị beta trong Alpha-Beta Pruning.
     * @return Giá trị heuristic của trạng thái.
     */
    private int minimax(Node state, int depth, int alpha, int beta) {
        // Điều kiện dừng đệ quy
        if (depth == 0 || isOver(state)) {
            return (int) heuristic(state);
        }

        if (state.isAiTurn()) { // Lượt AIPlayer (Maximizing Player)
            int maxEval = Integer.MIN_VALUE;
            List<Node> children = generateChildren(state);
            for (Node child : children) {
                int eval = minimax(child, depth - 1, alpha, beta);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (alpha >= beta) {
                    break; // Beta cut-off
                }
            }
            return maxEval;
        } else { // Lượt Player (Minimizing Player)
            int minEval = Integer.MAX_VALUE;
            List<Node> children = generateChildren(state);
            for (Node child : children) {
                int eval = minimax(child, depth - 1, alpha, beta);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (alpha >= beta) {
                    break; // Alpha cut-off
                }
            }
            return minEval;
        }
    }



    /**
     * Kiểm tra xem trạng thái trò chơi có kết thúc hay không.
     *
     * @param state Trạng thái của Node.
     * @return true nếu trò chơi kết thúc, ngược lại false.
     */
    private boolean isOver(Node state) {
        // Kiểm tra điều kiện kết thúc trò chơi dựa trên trạng thái của Node
        // Bạn cần điều chỉnh điều kiện này dựa trên logic trò chơi của bạn.
        // Ví dụ:
        // - Nếu một trong hai người chơi đã thắng hoặc thua.
        // - Nếu không còn nước đi hợp lệ.

        // Placeholder: Cần triển khai logic cụ thể
        // Ví dụ:
        // return state.isGameWon() || state.isGameOver();
        return false; // Placeholder: Chưa triển khai
    }

    /**
     * Hàm đánh giá trạng thái trò chơi.
     *
     * @param state Trạng thái của Node cần đánh giá.
     * @return Giá trị heuristic của trạng thái.
     */
    private double heuristic(Node state) {
        double score = 0.0;

        // Khoảng cách giữa AI và Player
        int distance = Math.abs(state.getAiPlayerX() - state.getPlayerX()) +
                Math.abs(state.getAiPlayerY() - state.getPlayerY());
        score -= distance * 10; // Người chơi càng gần AI, điểm càng thấp

        // An toàn của vị trí hiện tại
        if (!isSafe(state)) {
            score -= 50; // Phạt nếu AI không an toàn
        }

        // Số bom còn lại và phạm vi nổ
        score += state.getBombCount() * 20; // AI có bom hơn

        // Nguy hiểm từ bom
        for (Bomb bomb : state.getBombs()) {
            if (!bomb.isExploded()) {
                int bombDistance = Math.abs(bomb.getX() - state.getPlayerX()) +
                        Math.abs(bomb.getY() - state.getPlayerY());
                if (bombDistance <= bomb.getExplosionRange()) {
                    score -= (bomb.getExplosionRange() - bombDistance + 1) * 50;
                }
            }
        }

        // Thêm điểm thưởng khi đặt bom gần người chơi hoặc Balloon
        if (distance <= 3 && state.getBombCount() > 0) {
            score += 100;
        }

        // Phạt khi AI bị dồn vào ngõ cụt
        if (isCornered(state)) {
            score -= 150; // Giảm điểm thay vì tăng
        }

        // Thêm điểm thưởng cho các vị trí có nhiều hướng đi hơn
        int availableDirections = countAvailableDirections(state.getAiPlayerX(), state.getAiPlayerY(), state.getGameMap());
        score += availableDirections * 10; // Ưu tiên các vị trí có nhiều hướng đi hơn

        // **Thêm điểm thưởng cho hành động Đặt Bom**
        if (state.hasJustPlacedBomb()) {
            score += 150; // Tăng điểm khi AI đã đặt bom
        }

        return score;
    }



    /**
     * Constructor để khởi tạo MinimaxStrategy với độ sâu tối đa và kiểu người chơi (Maximizing hoặc Minimizing).
     *
     * @param maxDepth Độ sâu tối đa của thuật toán Minimax.
     * @param isMaximizingPlayer true nếu là Maximizing Player, false nếu là Minimizing Player.
     */

    /**
     * Kiểm tra xem vị trí của AIPlayer có an toàn không.
     *
     * @param state Trạng thái của Node.
     * @return true nếu an toàn, ngược lại false.
     */
    private boolean isSafe(Node state) {
        // Kiểm tra xem AIPlayer có bị bom hoặc Balloon đang tấn công không
        // Bạn cần thêm logic phù hợp dựa trên trạng thái của Node
        // Placeholder: Giả sử AIPlayer luôn an toàn
        return true;
    }

    /**
     * Kiểm tra xem AIPlayer có bị dồn vào ngõ cụt không.
     *
     * @param state Trạng thái của Node.
     * @return true nếu bị dồn vào ngõ cụt, ngược lại false.
     */
    private boolean isCornered(Node state) {
        int x = state.getAiPlayerX();
        int y = state.getAiPlayerY();
        int walkable = 0;
        int[][] directions = {
                {0, -1}, // Lên
                {0, 1},  // Xuống
                {-1, 0}, // Trái
                {1, 0}   // Phải
        };
        for (int[] dir : directions) {
            int newX = x + dir[0];
            int newY = y + dir[1];
            if (isWalkable(newX, newY, state.getGameMap())) {
                walkable++;
            }
        }
        return walkable <= 1; // Nếu có ít hơn hoặc bằng 1 hướng đi, coi như bị dồn vào ngõ cụt
    }

    /**
     * Kiểm tra xem ô (x, y) có walkable không.
     *
     * @param x       Tọa độ X.
     * @param y       Tọa độ Y.
     * @param gameMap Bản đồ trò chơi.
     * @return true nếu ô walkable, ngược lại false.
     */
    private boolean isWalkable(int x, int y, int[][] gameMap) {
        return x >= 0 && x < gameMap.length &&
                y >= 0 && y < gameMap[0].length &&
                gameMap[x][y] == 0; // 0 là ô trống
    }

    /**
     * Đếm số hướng đi khả thi từ vị trí (x, y).
     *
     * @param x       Tọa độ X của AIPlayer.
     * @param y       Tọa độ Y của AIPlayer.
     * @param gameMap Bản đồ trò chơi.
     * @return Số hướng đi khả thi.
     */
    private int countAvailableDirections(int x, int y, int[][] gameMap) {
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
            if (isWalkable(newX, newY, gameMap)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Tạo danh sách các trạng thái con dựa trên lượt hiện tại trong Node.
     *
     * @param state Trạng thái hiện tại của Node.
     * @return Danh sách các trạng thái con.
     */
    /**
     * Tạo danh sách các trạng thái con dựa trên lượt hiện tại trong Node.
     *
     * @param state Trạng thái hiện tại của Node.
     * @return Danh sách các trạng thái con.
     */
    private List<Node> generateChildren(Node state) {
        List<Node> children = new ArrayList<>();
        ActionType[] actionTypes = {
                ActionType.MOVE_UP,
                ActionType.MOVE_DOWN,
                ActionType.MOVE_LEFT,
                ActionType.MOVE_RIGHT,
                ActionType.PLACE_BOMB,
                ActionType.STAY
        };

        for (ActionType actionType : actionTypes) {
            // Tạo hành động
            Action action = new Action(actionType);
            if (actionType == ActionType.PLACE_BOMB) {
                action.setTargetX(state.isAiTurn() ? state.getAiPlayerX() : state.getPlayerX());
                action.setTargetY(state.isAiTurn() ? state.getAiPlayerY() : state.getPlayerY());
            }

            // Mô phỏng hành động và tạo trạng thái mới
            Node child = simulateAction(state, action, state.isAiTurn());
            if (child != null) {
                children.add(child);
            }
        }

        return children;
    }

    /**
     * Simulate một hành động trên một Node và trả về Node mới sau khi thực hiện hành động đó.
     *
     * @param currentState Trạng thái hiện tại của Node.
     * @param action        Hành động cần thực hiện.
     * @param isAiAction    true nếu hành động được thực hiện bởi AIPlayer, false nếu bởi Player.
     * @return Trạng thái mới sau khi thực hiện hành động, hoặc null nếu hành động không hợp lệ.
     */
    private Node simulateAction(Node currentState, Action action, boolean isAiAction) {
        // Clone trạng thái hiện tại để không ảnh hưởng đến trạng thái gốc
        Node clonedState = currentState.clone();
        if (clonedState == null) return null;

        // Thực hiện hành động trên AIPlayer hoặc Player trong clonedState
        if (isAiAction) {
            // Thực hiện hành động trên AIPlayer
            switch (action.getActionType()) {
                case MOVE_UP:
                    if (clonedState.getAiPlayerY() > 0 && isWalkable(clonedState.getAiPlayerX(), clonedState.getAiPlayerY() - 1, clonedState.getGameMap())) {
                        clonedState.setAiPlayerY(clonedState.getAiPlayerY() - 1);
                    } else {
                        return null; // Hành động không hợp lệ
                    }
                    break;
                case MOVE_DOWN:
                    if (clonedState.getAiPlayerY() < clonedState.getGameMap()[0].length - 1 &&
                            isWalkable(clonedState.getAiPlayerX(), clonedState.getAiPlayerY() + 1, clonedState.getGameMap())) {
                        clonedState.setAiPlayerY(clonedState.getAiPlayerY() + 1);
                    } else {
                        return null; // Hành động không hợp lệ
                    }
                    break;
                case MOVE_LEFT:
                    if (clonedState.getAiPlayerX() > 0 && isWalkable(clonedState.getAiPlayerX() - 1, clonedState.getAiPlayerY(), clonedState.getGameMap())) {
                        clonedState.setAiPlayerX(clonedState.getAiPlayerX() - 1);
                    } else {
                        return null; // Hành động không hợp lệ
                    }
                    break;
                case MOVE_RIGHT:
                    if (clonedState.getAiPlayerX() < clonedState.getGameMap().length - 1 &&
                            isWalkable(clonedState.getAiPlayerX() + 1, clonedState.getAiPlayerY(), clonedState.getGameMap())) {
                        clonedState.setAiPlayerX(clonedState.getAiPlayerX() + 1);
                    } else {
                        return null; // Hành động không hợp lệ
                    }
                    break;
                case PLACE_BOMB:
                    // Đặt bom tại vị trí hiện tại của AIPlayer
                    if (clonedState.getBombCount() > 0 && isWalkable(clonedState.getAiPlayerX(), clonedState.getAiPlayerY(), clonedState.getGameMap())) {
                        Bomb newBomb = new Bomb(clonedState.getAiPlayerX(), clonedState.getAiPlayerY(), 30, "AIPlayer", clonedState.getExplosionRange());
                        clonedState.getBombs().add(newBomb);
                        clonedState.setBombCount(clonedState.getBombCount() - 1);
                        markBombPlaced(clonedState); // Đánh dấu rằng bom đã được đặt
                    } else {
                        return null; // Không thể đặt bom
                    }
                    break;
                case STAY:
                    // Không thực hiện gì
                    break;
            }
        } else {
            // Thực hiện hành động trên Player (nếu cần thiết)
            // Bạn có thể triển khai tương tự như trên
        }

        // Chuyển lượt chơi
        clonedState.setAiTurn(!currentState.isAiTurn());

        return clonedState;
    }

    private void markBombPlaced(Node state) {
        state.setJustPlacedBomb(true);
    }


    /**
     * Hàm thực hiện hành động tốt nhất tìm được bằng thuật toán Minimax.
     *
     * @param entity Thực thể thực hiện hành động (AIPlayer).
     * @param game   Trạng thái trò chơi hiện tại.
     */
    @Override
    public void move(Entity entity, Game game) {
        AIPlayer aiPlayer = (AIPlayer) entity;
        // Tạo Node hiện tại dựa trên trạng thái của game
        Node currentState = new Node(
                aiPlayer.getX(),
                aiPlayer.getY(),
                game.getPlayer().getX(),
                game.getPlayer().getY(),
                aiPlayer.getBombCount(),
                game.getGameMap().clone().getMap(),
                clonedBombs(game.getBombs()),
                aiPlayer.getExplosionRange(),
                true // AIPlayer đang làm lượt
        );

        // Thực hiện thuật toán Minimax để tìm điểm số tốt nhất
        int bestScore = Integer.MIN_VALUE;
        Action bestAction = null;

        List<Action> possibleActions = generatePossibleActions(currentState);
        System.out.println("Các hành động có thể: ");
        for (Action action : possibleActions) {
            System.out.println("- " + action.getActionType());
        }

        for (Action action : possibleActions) {
            // Mô phỏng hành động và tạo trạng thái mới
            Node childState = simulateAction(currentState, action, true);
            if (childState == null) {
                System.out.println("Không thể mô phỏng hành động: " + action.getActionType());
                continue; // Hành động không hợp lệ
            }

            int score = minimax(childState, maxDepth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE);
            System.out.println("Hành động: " + action.getActionType() + ", Điểm số: " + score);
            if (score > bestScore) {
                bestScore = score;
                bestAction = action;
            }
        }

        // Thực hiện hành động tốt nhất
        if (bestAction != null) {
            System.out.println("AIPlayer chọn hành động: " + bestAction.getActionType());
            executeAction(aiPlayer, game, bestAction);
        } else {
            // Nếu không có hành động nào, AI có thể chọn ở lại hoặc di chuyển ngẫu nhiên
            System.out.println("AIPlayer không tìm thấy hành động tốt nhất, sử dụng RandomMovementStrategy.");
            new RandomMovementStrategy().move(entity, game);
        }
    }



    /**
     * Clone danh sách bom để tránh thay đổi trạng thái gốc.
     *
     * @param originalBombs Danh sách bom gốc.
     * @return Danh sách bom clone.
     */
    private List<Bomb> clonedBombs(List<Bomb> originalBombs) {
        List<Bomb> clonedBombs = new ArrayList<>();
        for (Bomb bomb : originalBombs) {
            clonedBombs.add(bomb.clone()); // Giả sử Bomb có phương thức clone()
        }
        return clonedBombs;
    }

    /**
     * Tạo danh sách các hành động có thể cho AIPlayer trong trạng thái hiện tại.
     *
     * @param state Trạng thái hiện tại của Node.
     * @return Danh sách các hành động có thể.
     */
    private List<Action> generatePossibleActions(Node state) {
        List<Action> actions = new ArrayList<>();
        ActionType[] actionTypes = {ActionType.MOVE_UP, ActionType.MOVE_DOWN, ActionType.MOVE_LEFT, ActionType.MOVE_RIGHT, ActionType.PLACE_BOMB, ActionType.STAY};
        for (ActionType actionType : actionTypes) {
            Action action = new Action(actionType);
            if (actionType == ActionType.PLACE_BOMB) {
                action.setTargetX(state.getAiPlayerX());
                action.setTargetY(state.getAiPlayerY());
            }
            actions.add(action);
        }
        // Xáo trộn danh sách hành động để tránh ưu tiên cố định
        Collections.shuffle(actions, new Random());
        return actions;
    }

    /**
     * Thực hiện hành động trên một thực thể trong trò chơi.
     *
     * @param entity Thực thể thực hiện hành động.
     * @param game   Trạng thái trò chơi hiện tại.
     * @param action Hành động cần thực hiện.
     */
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

                    // Sau khi đặt bom, tìm đường thoát an toàn
                    Pathfinding pathfinding = new Pathfinding(game.getGameMap());

                    // Tìm vị trí an toàn gần nhất
                    int[] safePosition = findNearestSafePosition(bombX, bombY, game);
                    if (safePosition != null) {
                        List<int[]> escapePath = pathfinding.findSafePath(
                                bombX,
                                bombY,
                                safePosition[0],
                                safePosition[1],
                                game
                        );

                        // Nếu tìm được đường thoát, di chuyển tới bước tiếp theo
                        if (!escapePath.isEmpty()) {
                            int[] nextStep = escapePath.get(0);
                            int dx = nextStep[0] - bombX;
                            int dy = nextStep[1] - bombY;
                            game.moveEntity(entity, dx, dy);
                            System.out.println(entity.getClass().getSimpleName() + " di chuyển tới (" + nextStep[0] + ", " + nextStep[1] + ") để tránh bom.");
                        } else {
                            System.out.println(entity.getClass().getSimpleName() + " không thể tìm đường thoát sau khi đặt bom.");
                        }
                    } else {
                        System.out.println("Không tìm thấy vị trí an toàn để thoát.");
                    }
                } else {
                    System.out.println("Không thể đặt bom tại (" + bombX + ", " + bombY + ") vì không có đường thoát an toàn.");
                }
                break;
            case STAY:
                System.out.println(entity.getClass().getSimpleName() + " ở lại.");
                break;
        }
    }

    /**
     * Tìm vị trí an toàn gần nhất từ (bombX, bombY).
     *
     * @param bombX Tọa độ X của bom.
     * @param bombY Tọa độ Y của bom.
     * @param game  Trạng thái trò chơi hiện tại.
     * @return Vị trí an toàn dưới dạng mảng int[]{x, y}, hoặc null nếu không tìm được.
     */
    private int[] findNearestSafePosition(int bombX, int bombY, Game game) {
        List<int[]> safePositions = game.getGameMap().findSafePositions(game.getAiPlayer(), game);
        if (safePositions.isEmpty()) return null;

        // Tìm vị trí an toàn gần nhất
        int minDistance = Integer.MAX_VALUE;
        int[] nearest = null;
        for (int[] pos : safePositions) {
            int distance = Math.abs(pos[0] - bombX) + Math.abs(pos[1] - bombY);
            if (distance < minDistance) {
                minDistance = distance;
                nearest = pos;
            }
        }
        return nearest;
    }

    /**
     * Kiểm tra xem có thể đặt bom một cách an toàn tại vị trí (bombX, bombY) không.
     *
     * @param game   Trạng thái trò chơi hiện tại.
     * @param entity Thực thể đặt bom.
     * @param bombX  Tọa độ X của bom.
     * @param bombY  Tọa độ Y của bom.
     * @return true nếu có thể đặt bom an toàn, ngược lại false.
     */
    private boolean canPlaceBombSafely(Game game, Entity entity, int bombX, int bombY) {
        // Đảm bảo AIPlayer có bombCount > 0
        if (entity.getBombCount() <= 0) {
            System.out.println("AIPlayer không còn bom để đặt.");
            return false;
        }

        // Clone game để kiểm tra
        Game clonedGame = game.clone();
        if (clonedGame == null) {
            System.out.println("Không thể clone game.");
            return false;
        }

        // Kiểm tra xem AIPlayer có thể thoát an toàn sau khi đặt bom
        if (!clonedGame.canEscape(entity, bombX, bombY)) {
            System.out.println("AIPlayer không thể thoát sau khi đặt bom.");
            return false;
        }

        System.out.println("AIPlayer có thể đặt bom an toàn.");
        return true;
    }
}
