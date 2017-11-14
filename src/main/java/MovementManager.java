import java.util.*;

public class MovementManager {
    public static Deque<Integer> movements = new ArrayDeque<>();
    static {
        for(int i = 0; i < 12; ++i) {
            movements.add(-100);
        }
    }

    public Set<MyMove> delayedMoves = new HashSet<>();

    public MyStrategy strategy;

    public MovementManager(MyStrategy strategy) {
        this.strategy = strategy;
    }

    public void add(MyMove move) {
        delayedMoves.add(move);
    }

    public boolean canMove() {
        return delayedMoves.size() > 0 && canDoMove();
    }

    public void move() {
        if(canMove()) {
            for(MyMove myMove : delayedMoves) {
                if(myMove.canBeApplied()) {
                    myMove.apply(strategy.move);
                    if(!myMove.hasNext) delayedMoves.remove(myMove);
                    return;
                } else if(myMove.hasNext && myMove.canDoNext()) {
                    delayedMoves.add(myMove.next);
                    delayedMoves.remove(myMove);
                    return;
                }
            }
        }
    }

    public void registerMovement() {
        movements.poll();
        movements.add(strategy.game.getTickCount());
    }

    public boolean canDoMove() {
        return this.strategy.world.getTickIndex() - movements.getFirst() >= 60;
    }
}
