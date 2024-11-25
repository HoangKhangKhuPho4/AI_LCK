// File: ai/Balloon.java
package ai;

import java.util.ArrayList;
import java.util.List;

public class Balloon extends Entity {
    private MovementStrategy movementStrategy;
    private int ticksUntilMove;
    private int moveDelay;

    public Balloon(int startX, int startY, MovementStrategy strategy) {
        this.x = startX;
        this.y = startY;
        this.movementStrategy = strategy;
        this.moveDelay = 4;
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
            movementStrategy.move(this, game);
            ticksUntilMove = moveDelay;
            System.out.println(this.getClass().getSimpleName() + " đã di chuyển đến (" + x + ", " + y + ").");
        }

        // Kiểm tra va chạm với người chơi
        Player player = game.getPlayer();
        if (this.x == player.getX() && this.y == player.getY() && player.isAlive()) {
            player.setAlive(false);
            System.out.println("Người chơi bị Balloon tiêu diệt!");
        }

        // Kiểm tra va chạm với AIPlayer
        AIPlayer aiPlayer = game.getAIPlayer();
        if (aiPlayer != null && aiPlayer.isAlive() && this.x == aiPlayer.getX() && this.y == aiPlayer.getY()) {
            aiPlayer.setAlive(false);
            System.out.println("AIPlayer bị Balloon tiêu diệt!");
        }
    }


    @Override
    protected int getExplosionRange() {
        // TODO Auto-generated method stub
        return 0;
    }

//	private void initializeBalloons(int count) {
//		int placed = 0;
//		while (placed < count) {
//
//			int x = rand.nextInt(gameMap.getWidth());
//	if (gameMap.getTile(x, y) == ' ' && !(x == player.getX() && y == player.getY())) {
//				int y = rand.nextInt(gameMap.getHeight());
//					MovementStrategy strategy = null; // Khởi tạo strategy là null
//				switch (placed % 3) {
//				case 0:
//					strategy = new RandomMovementStrategy();
//					break;
//				case 1:
//					strategy = new ChasePlayerStrategy();
//					break;
//				case 2:
//					// Ví dụ đường tuần tra đơn giản
//					List<int[]> patrolPath = new ArrayList<>();
//					patrolPath.add(new int[] { x, y });
//					// Kiểm tra x + 1 có vượt quá giới hạn không
//					if (x + 1 < gameMap.getWidth()) {
//						patrolPath.add(new int[] { x + 1, y });
//						// Kiểm tra y + 1 có vượt quá giới hạn không
//						if (y + 1 < gameMap.getHeight()) {
//							patrolPath.add(new int[] { x + 1, y + 1 });
//						}
//					}
//					if (y + 1 < gameMap.getHeight()) {
//						patrolPath.add(new int[] { x, y + 1 });
//					}
//					strategy = new PatrolStrategy(patrolPath);
//					break;
//				default:
//					strategy = new RandomMovementStrategy();
//				}
//				if (strategy != null) {
//					Balloon balloon = new Balloon(x, y, strategy);
//					balloons.add(balloon);
//					placed++;
//				}
//			}
//		}
//		System.out.println(count + " Balloon đã được khởi tạo.");
//	}

}  