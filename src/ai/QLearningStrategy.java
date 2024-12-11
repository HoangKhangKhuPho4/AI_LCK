
package ai;

import ai.Entity;
import ai.Game;
import ai.MovementStrategy;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class QLearningStrategy implements MovementStrategy {
    private Map<String, Map<ActionType, Double>> qTable;
    private double learningRate = 0.1;
    private double discountFactor = 0.9;
    private double explorationRate = 0.2;
    private Random rand = new Random();

    public QLearningStrategy() {
        qTable = new HashMap<>();
    }

//    @Override
//    public void move(Entity entity, Game game) {
//        String state = getStateKey(entity, game);
//        ActionType action = chooseAction(state);
//        executeAction(entity, game, action);
//        // Sau khi thực hiện hành động, cập nhật Q-table
//        String newState = getStateKey(entity, game);
//        double reward = evaluateReward(entity, game);
//        updateQTable(state, action, reward, newState);
//    }


    @Override
    public void move(Entity entity, Game game) {
        String state = getStateKey(entity, game);
        ActionType action = chooseAction(state);
        executeAction(entity, game, action);
        String newState = getStateKey(entity, game);
        double reward = evaluateReward(entity, game);
        updateQTable(state, action, reward, newState);
    }

    private String getStateKey(Entity entity, Game game) {
        // Tạo khóa trạng thái dựa trên vị trí và các yếu tố khác
        return entity.getX() + "," + entity.getY() + "," + game.getBombs().size();
    }

    private ActionType chooseAction(String state) {
        if (!qTable.containsKey(state)) {
            qTable.put(state, new HashMap<>());
        }
        Map<ActionType, Double> actions = qTable.get(state);
        // Khám phá hành động ngẫu nhiên với xác suất explorationRate
        if (rand.nextDouble() < explorationRate) {
            return ActionType.values()[rand.nextInt(ActionType.values().length)];
        }
        // Chọn hành động có giá trị Q cao nhất
        return actions.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(ActionType.STAY);
    }

    private void executeAction(Entity entity, Game game, ActionType action) {
        // Thực hiện hành động tương ứng
        switch (action) {
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

    private double evaluateReward(Entity entity, Game game) {
        if (!entity.isAlive()) {
            return -100; // Phạt khi chết
        }
        if (game.isGameWon()) {
            return 100; // Thưởng khi thắng
        }
        // Các tiêu chí thưởng khác
        return 10; // Thưởng cơ bản cho mỗi bước đi an toàn
    }

    private void updateQTable(String state, ActionType action, double reward, String newState) {
        if (!qTable.containsKey(newState)) {
            qTable.put(newState, new HashMap<>());
        }
        double oldQ = qTable.get(state).getOrDefault(action, 0.0);
        double maxFutureQ = qTable.get(newState).values().stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        double newQ = oldQ + learningRate * (reward + discountFactor * maxFutureQ - oldQ);
        qTable.get(state).put(action, newQ);
    }
}
