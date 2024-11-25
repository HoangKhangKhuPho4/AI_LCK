package ai;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RandomMovementStrategy implements MovementStrategy {
    private Random rand = new Random();

    @Override
    public void move(Entity entity, Game game) {
        int[][] directions = {
                {0, -1}, // Lên
                {0, 1},  // Xuống
                {-1, 0}, // Trái
                {1, 0}   // Phải
        };
        List<int[]> dirList = Arrays.asList(directions);
        Collections.shuffle(dirList, rand);
        GameMap map = game.getGameMap();
        for (int[] dir : dirList) {
            int newX = entity.getX() + dir[0];
            int newY = entity.getY() + dir[1];
            if (map.isWalkable(newX, newY)) {
                entity.setX(newX);
                entity.setY(newY);
                break;
            }
        }
    }
}