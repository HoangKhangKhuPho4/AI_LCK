package ai;
/**
 * Lớp đại diện cho một hành động.
 */
public class Action {
    private ActionType actionType;
    private int targetX;
    private int targetY;
    // Constructor cho hành động di chuyển
    public Action(ActionType actionType) {
        this.actionType = actionType;
    }
    // Constructor cho hành động đặt bom
    public Action(ActionType actionType, int targetX, int targetY) {
        this.actionType = actionType;
        this.targetX = targetX;
        this.targetY = targetY;
    }
    public ActionType getActionType() {
        return actionType;
    }
    public int getTargetX() {
        return targetX;
    }
    public int getTargetY() {
        return targetY;
    }
}

