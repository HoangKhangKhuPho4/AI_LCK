package ai;

public class AIState {
    private int x;
    private int y;
    private int bombCount;
    private int balloonCount;

    public AIState(int x, int y, int bombCount, int balloonCount) {
        this.x = x;
        this.y = y;
        this.bombCount = bombCount;
        this.balloonCount = balloonCount;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getBombCount() {
        return bombCount;
    }

    public int getBalloonCount() {
        return balloonCount;
    }
}
