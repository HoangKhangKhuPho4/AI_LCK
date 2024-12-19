package ai;

/**
 * Lớp Action đại diện cho một hành động mà một thực thể có thể thực hiện.
 */
public class Action {
    private ActionType actionType;
    private int targetX; // Vị trí X để đặt bom (áp dụng cho PLACE_BOMB)
    private int targetY; // Vị trí Y để đặt bom (áp dụng cho PLACE_BOMB)

    // Constructor cho các hành động không cần targetX và targetY
    public Action(ActionType actionType) {
        this.actionType = actionType;
    }

    // Constructor cho hành động PLACE_BOMB cần targetX và targetY
    public Action(ActionType actionType, int targetX, int targetY) {
        this.actionType = actionType;
        this.targetX = targetX;
        this.targetY = targetY;
    }

    // Getters và Setters
    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public int getTargetX() {
        return targetX;
    }

    public void setTargetX(int targetX) {
        this.targetX = targetX;
    }

    public int getTargetY() {
        return targetY;
    }

    public void setTargetY(int targetY) {
        this.targetY = targetY;
    }
}
