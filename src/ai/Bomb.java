// File: ai/Bomb.java
package ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Bomb implements Subject {
    private int x, y;
    private int countdown;
    private boolean exploded;
    private int explosionDuration;
    private boolean explosionProcessed;
    private Entity owner;
    private int explosionRange;
    private List<Observer> observers = new ArrayList<>();

    public Bomb(int x, int y, int countdown, Entity owner, int explosionRange) {
        this.x = x;
        this.y = y;
        this.countdown = countdown;
        this.exploded = false;
        this.explosionDuration = 1;
        this.explosionProcessed = false;
        this.owner = owner;
        this.explosionRange = explosionRange;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getCountdown() {
        return countdown;
    }

    public Entity getOwner() {
        return owner;
    }

    public int getExplosionRange() {
        return explosionRange;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setCountdown(int countdown) {
        this.countdown = countdown;
    }

    @Override
    public void attach(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void detach(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(Event event) {
        for (Observer observer : observers) {
            observer.update(event);
        }
    }

    /**
     * Cập nhật trạng thái của bom
     */
    public void tick(Game game) {
        if (!exploded) {
            if (countdown > 0) {
                countdown--;
            }
            if (countdown == 0) {
                exploded = true;
                // Khi bom nổ, xử lý phá hủy tường phá hủy
                processExplosion(game);
                // Khi bom nổ, thông báo cho các Observer
                List<int[]> explosionTiles = game.getExplosionTiles(this);
                notifyObservers(new BombExplodedEvent(explosionTiles));
            }
        } else {
            if (explosionDuration > 0) {
                explosionDuration--;
            }
        }
    }


    private void processExplosion(Game game) {
        List<int[]> explosionTiles = game.getExplosionTiles(this);
        GameMap map = game.getGameMap();
        for (int[] tile : explosionTiles) {
            int x = tile[0];
            int y = tile[1];
            char currentTile = map.getTile(x, y);
            if (currentTile == 'D') {
                // Phá hủy tường phá hủy
                map.setTile(x, y, ' ');
                System.out.println("Tường phá hủy tại (" + x + ", " + y + ") đã bị phá hủy.");
                // Có thể tạo cơ hội xuất hiện vật phẩm sau khi tường bị phá hủy
                // Ví dụ: 20% cơ hội xuất hiện vật phẩm
                Random rand = new Random();
                if (rand.nextInt(100) < 20) {
                    Item.ItemType type = rand.nextBoolean() ? Item.ItemType.SPEED : Item.ItemType.EXPLOSION_RANGE;
                    game.getGameMap().addItem(new Item(x, y, type));
                    System.out.println("Vật phẩm " + type + " xuất hiện tại (" + x + ", " + y + ").");
                }
            }
        }
    }


    public boolean isExploded() {
        return exploded;
    }

    public boolean isExplosionFinished() {
        return exploded && explosionDuration == 0;
    }

    public boolean isExplosionProcessed() {
        return explosionProcessed;
    }

    public void setExplosionProcessed(boolean explosionProcessed) {
        this.explosionProcessed = explosionProcessed;
    }
}  