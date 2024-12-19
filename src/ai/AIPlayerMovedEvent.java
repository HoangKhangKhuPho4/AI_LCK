
package ai;
/**
 * Sự kiện khi AIPlayer di chuyển.
 */
public class AIPlayerMovedEvent extends Event {
    private int newX;
    private int newY;
    public AIPlayerMovedEvent(int newX, int newY) {
        this.newX = newX;
        this.newY = newY;
    }
    public int getNewX() {
        return newX;
    }
    public int getNewY() {
        return newY;
    }
}
