
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




    /**
     * Lớp nội bộ để lưu kết quả của Minimax.
     */
//    private class MinimaxResult {
//        Action action;
//        double score;
//
//        MinimaxResult(Action action, double score) {
//            this.action = action;
//            this.score = score;
//        }
//    }

    /**
     * Thuật toán Minimax với Alpha-Beta Pruning và Transposition Table.
     */
    public int minimax(boolean maxmin, Node state, int depth, int alpha, int beta) {
        // Điều kiện dừng đệ quy
        if (depth == 0 || isOver(state)) {
            return heuristic(state);
        }

        if (maxmin) { // Người chơi tối đa
            int temp = Integer.MIN_VALUE; // Sử dụng Integer.MIN_VALUE thay vì -999999999
            for (Node child : generateChildren(state)) { // Tạo danh sách node con
                int value = minimax(false, child, depth - 1, alpha, beta);
                temp = Math.max(temp, value);
                alpha = Math.max(alpha, temp);
                if (alpha >= beta) {
                    // Cắt tỉa nhánh không cần thiết
                    break;
                }
            }
            return temp;
        } else { // Người chơi tối thiểu
            int temp = Integer.MAX_VALUE; // Sử dụng Integer.MAX_VALUE thay vì 999999999
            for (Node child : generateChildren(state)) { // Tạo danh sách node con
                int value = minimax(true, child, depth - 1, alpha, beta);
                temp = Math.min(temp, value);
                beta = Math.min(beta, temp);
                if (alpha >= beta) {
                    // Cắt tỉa nhánh không cần thiết
                    break;
                }
            }
            return temp;
        }
    }

    private boolean isOver(Node state) {
        // Kiểm tra điều kiện kết thúc trò chơi, ví dụ:
        // - Kiểm tra nếu một trong hai người chơi đã thắng hoặc thua.
        // - Kiểm tra nếu không còn nước đi hợp lệ nào.
        // Bạn có thể điều chỉnh tùy theo luật của trò chơi.

        Game game = state.getGame();  // Giả sử bạn có thể lấy Game từ Node

        // Kiểm tra nếu một trong hai người chơi đã thua (ví dụ: AIPlayer hoặc Player)
        if (game.isGameWon()) {
            return true;  // Trò chơi đã thắng
        }

        if (game.isGameOver()) {
            return true;  // Trò chơi kết thúc do các lý do khác (ví dụ: không còn đường đi hợp lệ hoặc bom đã nổ)
        }

        // Kiểm tra các điều kiện khác nếu cần, ví dụ như xem AI hay Player có bị kẹt trong một góc hay không
        return false;
    }



    public List<Node> generateChildren(Node state) {
        List<Node> children = new ArrayList<>();
        int aiX = state.getAIPlayerX();
        int aiY = state.getAIPlayerY();

        // Các hướng di chuyển có thể: lên, xuống, trái, phải
        int[][] directions = {
                {0, -1}, // Lên
                {0, 1},  // Xuống
                {-1, 0}, // Trái
                {1, 0}   // Phải
        };

        for (int[] dir : directions) {
            int newX = aiX + dir[0];
            int newY = aiY + dir[1];

            // Kiểm tra xem ô mới có hợp lệ không
            if (state.isValidMove(newX, newY)) {
                // Tạo trạng thái mới và thêm vào danh sách con
                Node childState = new Node(newX, newY, state.getPlayerX(), state.getPlayerY(), state.getBombCount(), state.getGameMap());
                children.add(childState);
            }
        }

        return children;
    }


    private boolean isValidMove(int x, int y, Node state) {
        // Kiểm tra xem ô (x, y) có hợp lệ không (không phải tường hoặc ngoài bản đồ)
        // Giả sử giá trị 0 là ô trống và các giá trị khác là tường/chướng ngại vật
        return x >= 0 && x < state.getGameMap().length &&
                y >= 0 && y < state.getGameMap()[0].length &&
                state.getGameMap()[x][y] == 0; // 0 là ô trống
    }




    private int heuristic(Node state) {
        int aiX = state.getAIPlayerX();
        int aiY = state.getAIPlayerY();
        int playerX = state.getPlayerX();
        int playerY = state.getPlayerY();

        // Tính toán khoảng cách Manhattan giữa AI và người chơi
        int distanceToPlayer = Math.abs(aiX - playerX) + Math.abs(aiY - playerY);

        // Nếu AI ở gần người chơi, trả về giá trị âm (AI có nguy cơ)
        if (distanceToPlayer < 3) {
            return -100 + distanceToPlayer; // Giá trị âm lớn hơn nếu gần người chơi
        }

        // Nếu AI có thể đặt bom và gây nổ gần người chơi
        if (canPlaceBombNearPlayer(state)) {
            return 100; // Giá trị dương nếu có cơ hội tấn công
        }

        // Trả về giá trị dựa trên khoảng cách an toàn từ bom hoặc các mối nguy hiểm khác
        return distanceToPlayer; // Giá trị trung bình dựa trên khoảng cách an toàn
    }

    private boolean canPlaceBombNearPlayer(Node state) {
        // Lấy vị trí người chơi
        int playerX = state.getPlayerX();
        int playerY = state.getPlayerY();

        // Kiểm tra các ô xung quanh người chơi
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if ((dx != 0 || dy != 0) && isValidMove(playerX + dx, playerY + dy, state)) {
                    return true; // Có thể đặt bom ở vị trí này
                }
            }
        }

        return false; // Không thể đặt bom gần người chơi
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


    @Override
    public void move(Entity entity, Game game) {

    }
}
