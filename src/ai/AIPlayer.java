package ai;

// File: ai/AIPlayer.java

public class AIPlayer extends Entity {
    private MovementStrategy movementStrategy;
    private int ticksUntilMove;
    private int moveDelay;
    private int explosionRange = 1; // Thêm thuộc tính phạm vi nổ

    public AIPlayer(int startX, int startY, MovementStrategy strategy) {
        this.x = startX;
        this.y = startY;
        this.movementStrategy = strategy;
        this.moveDelay = 3;
        this.ticksUntilMove = moveDelay;
    }

    @Override
    public void update(Game game) {
        if (!alive) {
            System.out.println(this.getClass().getSimpleName() + " không còn sống.");
            return;
        }
        ticksUntilMove--;
        if (ticksUntilMove <= 0) {
            // Kiểm tra xem AI có nên đặt bom không
            if (shouldPlaceBomb(game)) {
                placeBomb(game);
            }
            movementStrategy.move(this, game);
            ticksUntilMove = moveDelay;
            System.out.println(this.getClass().getSimpleName() + " đã di chuyển đến (" + x + ", " + y + ").");
        }
    }




    private boolean shouldPlaceBomb(Game game) {
        Player player = game.getPlayer();
        int distance = Math.abs(player.getX() - x) + Math.abs(player.getY() - y);
        // Đặt bom nếu người chơi ở ngay cạnh
        return distance == 1 && bombCount > 0;
    }


    private void placeBomb(Game game) {
        if (placeBomb()) { // Gọi phương thức placeBomb() từ lớp Entity
            int countdown = 30; // Đếm ngược 3 giây
            Bomb bomb = new Bomb(x, y, countdown, this, explosionRange);
            game.addBomb(bomb);
        }
    }


    public int getExplosionRange() {
        return explosionRange;
    }

    public void increaseExplosionRange() {
        explosionRange++;
    }
}