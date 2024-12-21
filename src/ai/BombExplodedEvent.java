// File: ai/BombExplodedEvent.java
package ai;

import java.util.List;

public class BombExplodedEvent extends Event {
    private Bomb bomb;               // Quả bom vừa nổ
    private List<int[]> explosionTiles; // Danh sách toạ độ chịu ảnh hưởng

    // Constructor mới: nhận cả bomb lẫn explosionTiles
    public BombExplodedEvent(Bomb bomb, List<int[]> explosionTiles) {
        this.bomb = bomb;
        this.explosionTiles = explosionTiles;
    }

    public Bomb getBomb() {
        return bomb;
    }

    public List<int[]> getExplosionTiles() {
        return explosionTiles;
    }
}
