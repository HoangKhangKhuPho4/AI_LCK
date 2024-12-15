


// File: ai/BombExplodedEvent.java
package ai;

import java.util.List;

public class BombExplodedEvent extends Event {
    private List<int[]> explosionTiles;

    public BombExplodedEvent(List<int[]> explosionTiles) {
        this.explosionTiles = explosionTiles;
    }

    public List<int[]> getExplosionTiles() {
        return explosionTiles;
    }
}
