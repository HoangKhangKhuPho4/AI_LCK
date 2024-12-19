
package ai;
/**
 * Sự kiện khi người chơi di chuyển.
 */
public class PlayerMovedEvent extends Event {
    private int newX;
    private int newY;
    public PlayerMovedEvent(int newX, int newY) {
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
