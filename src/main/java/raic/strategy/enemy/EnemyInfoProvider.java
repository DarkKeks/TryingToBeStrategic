package raic.strategy.enemy;

import raic.strategy.MyStrategy;
import raic.strategy.MyVehicle;
import raic.strategy.Point;
import raic.strategy.Util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;

public class EnemyInfoProvider {

    public static final int CLUSTER_MIN_DISTANCE = 15;

    private MyStrategy strategy;
    private int lastUpdate = -Util.GROUP_UPDATE_TIMEOUT;

    public ArrayList<Group> groups;
    public Group group;

    public EnemyInfoProvider(MyStrategy strategy) {
        this.strategy = strategy;
        this.groups = new ArrayList<>();
    }

    public void update(Point centerPoint) {
        if(Util.delayCheck(Util.GROUP_UPDATE_TIMEOUT, lastUpdate)) {
            lastUpdate = strategy.world.getTickIndex();

            forceUpdate(centerPoint);
        }
    }

    public void forceUpdate(Point centerPoint) {
        groups.clear();

        ArrayList<MyVehicle> enemy = new ArrayList<>();
        for(MyVehicle veh : strategy.vehicles)
            if(veh.alive && veh.enemy) enemy.add(veh);

        int n = enemy.size();
        boolean[] used = new boolean[n];

        ArrayDeque<MyVehicle> q = new ArrayDeque<>();
        for(int i = 0; i < n; ++i) {
            if(!used[i]) {
                used[i] = true;
                Group group = new Group();

                q.add(enemy.get(i));
                while(q.size() > 0) {
                    MyVehicle cur = q.poll();
                    group.add(cur);

                    for(int j = 0; j < n; ++j) {
                        if(!used[j]) {
                            MyVehicle target = enemy.get(j);
                            if (cur.getSquaredDistanceTo(target.getX(), target.getY()) <=
                                    CLUSTER_MIN_DISTANCE * CLUSTER_MIN_DISTANCE) {
                                used[j] = true;
                                q.add(target);
                            }
                        }
                    }
                }

                groups.add(group);
            }
        }
        groups.sort(Comparator.comparingDouble(g -> g.getCenter().sqDist(centerPoint)));
        group = groups.get(0);
    }

    public Group getGroup() {
        return group;
    }

    public Point getAttackPoint() {
        return group.getCenter();
    }

    public Point getNukePoint() {
        return getAttackPoint();
    }
}