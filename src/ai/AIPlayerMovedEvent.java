

package ai;

public class AIPlayerMovedEvent extends Event {
    private int x, y;

    public AIPlayerMovedEvent(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() { return x; }
    public int getY() { return y; }
}
