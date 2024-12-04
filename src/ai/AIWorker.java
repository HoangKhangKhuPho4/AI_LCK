package ai;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

public class AIWorker extends SwingWorker<Void, Void> {
    private AIPlayer aiPlayer;
    private Game game;

    public AIWorker(AIPlayer aiPlayer, Game game) {
        this.aiPlayer = aiPlayer;
        this.game = game;
    }

    @Override
    protected Void doInBackground() throws Exception {
        aiPlayer.getMovementStrategy().move(aiPlayer, game);
        return null;
    }

    @Override
    protected void done() {
        aiPlayer.ticksUntilMove = aiPlayer.moveDelay;
        System.out.println(aiPlayer.getClass().getSimpleName() + " đã di chuyển đến (" + aiPlayer.getX() + ", " + aiPlayer.getY() + ").");
        // Cập nhật giao diện trên luồng EDT
        SwingUtilities.invokeLater(() -> {
            // Thông báo cho các observer nếu cần
            aiPlayer.notifyObservers(new AIPlayerMovedEvent(aiPlayer.getX(), aiPlayer.getY()));
        });
    }
}