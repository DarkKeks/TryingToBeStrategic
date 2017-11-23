package raic.strategy;

import raic.strategy.enemy.EnemyInfoProvider;

public class SandwichController {

    public MyStrategy strategy;
    public EnemyInfoProvider provider;

    public int lastOrientationTick = -Util.SANDWICH_ORIENTATION_DELAY;

    public SandwichController(MyStrategy strategy) {
        this.strategy = strategy;
        this.provider = new EnemyInfoProvider(strategy);
    }

    public void tick() {
        if(strategy.world.getTickIndex() - lastOrientationTick > Util.SANDWICH_ORIENTATION_DELAY) {
            orient();
        }
    }

    private void orient() {

    }
}
