package ai;

public class Move {
    public enum ActionType {
        MOVE_UP, MOVE_DOWN, MOVE_LEFT, MOVE_RIGHT, PLACE_BOMB, STAY
    }

    private ActionType action;

    public Move(ActionType action) {
        this.action = action;
    }

    public ActionType getAction() {
        return action;
    }
} 