package raic.strategy.enemy;

import raic.strategy.MyStrategy;
import raic.strategy.MyVehicle;
import raic.strategy.Point;
import raic.strategy.Util;

public class EnemyInfoProvider {
	
    private MyStrategy strategy;
    private int lastUpdate = -1000;

    private Group group;

    public EnemyInfoProvider(MyStrategy strategy) {
        this.strategy = strategy;
    }

    public void update() {
        if(strategy.world.getTickIndex() - Util.GROUP_UPDATE_TIMEOUT > lastUpdate) {
            lastUpdate = strategy.world.getTickIndex();
            forceUpdate();
        }
    }

    public void forceUpdate() {
        group = new Group();
        for(MyVehicle veh : strategy.vehicleById.values()) {
            if(veh.enemy)
                group.add(veh);
        }
    }

    public Point getAttackPoint() {
        if(group == null) forceUpdate();
        return group.getCenter();
    }

    public Point getNukePoint() {
        return getAttackPoint();
    }
}