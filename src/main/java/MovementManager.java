import java.util.*;

public class MovementManager {
    public static int time = 0;
    public static Deque<Integer> movements = new ArrayDeque<>();
    static {
        for(int i = 0; i < 12; ++i) {
            movements.add(-100);
        }
    }

    public TreeSet<MyMove> delayedMoves = new TreeSet<>(Comparator.comparingInt(a -> a.addTime));

    public MyStrategy strategy;

    public MovementManager(MyStrategy strategy) {
        this.strategy = strategy;
    }

    public void add(MyMove move) {
        move.addTime = time++;
        delayedMoves.add(move);
    }

    public boolean canMove() {
        return delayedMoves.size() > 0 && canDoMove();
    }

    public void move() {
        if(canMove()) {
            for(MyMove myMove : delayedMoves) {
                if(myMove.canBeApplied()) {
                    if(myMove.hasGenerator)
                        myMove.applyGenerator();
                    myMove.apply(strategy.move);
                    registerMovement();
                    if (myMove.hasNext) {
                        MyMove next = myMove.next;
                        next.addTime = (myMove.delay == 0 ? myMove.addTime : time++);
                        next.minTick = strategy.world.getTickIndex() + myMove.delay;
                        delayedMoves.remove(myMove);
                        delayedMoves.add(next);
                    } else {
                        delayedMoves.remove(myMove);
                    }
                    return;
                } else {
                    myMove.retry();
                }
            }
        }
    }

    public void registerMovement() {
        movements.poll();
        movements.add(strategy.world.getTickIndex());
    }

    public boolean canDoMove() {
        return this.strategy.world.getTickIndex() - movements.getFirst() >= 60;
    }
}
