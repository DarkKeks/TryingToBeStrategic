import model.Move;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Queue;
import java.util.function.Consumer;

public class MovementManager {
    public static Deque<Integer> movements = new ArrayDeque<>();
    static {
        for(int i = 0; i < 12; ++i) {
            movements.add(-100);
        }
    }

    public Queue<Consumer<MyMove>> delayedMoves = new ArrayDeque<>();

    public MyStrategy strategy;

    public MovementManager(MyStrategy strategy) {
        this.strategy = strategy;
    }

    public boolean canMove() {
        return delayedMoves.size() > 0 && canDoMove();
    }

    public void move() {
        if(canMove()) {

        }
    }

    public void registerMovement() {
        movements.poll();
        movements.add(strategy.game.getTickCount());
    }

    public boolean canDoMove() {
        return movements.getFirst() - this.strategy.game.getTickCount() >= 60;
    }
}
