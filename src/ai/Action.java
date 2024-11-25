package ai;

/**
 * Lớp đại diện cho một hành động cụ thể.
 */
public class Action {
    private ActionType actionType;

    public Action(ActionType actionType) {
        this.actionType = actionType;
    }

    public ActionType getActionType() {
        return actionType;
    }
}