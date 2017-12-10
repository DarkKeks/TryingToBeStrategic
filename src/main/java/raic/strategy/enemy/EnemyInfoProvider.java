package raic.strategy.enemy;

import raic.model.Facility;
import raic.strategy.MyStrategy;
import raic.strategy.MyVehicle;
import raic.strategy.Point;
import raic.strategy.Util;

import java.util.ArrayDeque;
import java.util.ArrayList;

public class EnemyInfoProvider {

    public static final int CLUSTER_MIN_DISTANCE = 15;

    private MyStrategy strategy;
    private int lastUpdate = -Util.GROUP_UPDATE_TIMEOUT;

    private Point lastAttackPoint;

    public ArrayList<Group> groups;
    public ArrayList<Group> facilities;
    public Group group;

    public EnemyInfoProvider(MyStrategy strategy) {
        this.strategy = strategy;
        this.groups = new ArrayList<>();
        this.facilities = new ArrayList<>();
        this.lastAttackPoint = new Point(0, 0);
    }

    public void update(Point centerPoint) {
        if(Util.delayCheck(Util.GROUP_UPDATE_TIMEOUT, lastUpdate)) {
            lastUpdate = MyStrategy.world.getTickIndex();

            forceUpdate(centerPoint);
        }
    }

    public void forceUpdate(Point centerPoint) {
        groups.clear();
        facilities.clear();

        for(Facility fac : strategy.facilityById.values()) {
            if(fac.getOwnerPlayerId() != MyStrategy.player.getId()) {
                facilities.add(new Group(fac));
            }
        }

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

        facilities.sort(Comparator.comparingDouble(g -> g.getCenter().sqDist(centerPoint)));

        groups.sort((g1, g2) -> {
            // if groups are small, skip (cause of xor)
            if(!g1.isFacility() && !g2.isFacility() && (g1.unitCount < 10 ^ g2.unitCount < 10))
                return -Integer.compare(g1.unitCount, g2.unitCount); // less is worse, so 9 is worse then 50
            return Double.compare(g1.getCenter().sqDist(centerPoint), g2.getCenter().sqDist(centerPoint));
        });

        group = groups.get(0);
        if(facilities.size() > 0 &&
                !(groups.get(0).getCenter().sqDist(centerPoint) < Util.FAC_DIST_THRESHOLD_SQ) &&
                facilities.get(0).getCenter().sqDist(centerPoint) < group.getCenter().sqDist(centerPoint))
            group = facilities.get(0);
    }

    public Group getGroup() {
        return group;
    }

    public Point getAttackPoint() {
        return group.getCenter();
    }

    public boolean isFacility() {
        return group.isFacility();
    }
}