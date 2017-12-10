package raic.strategy.enemy;

import raic.model.Facility;
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
    public ArrayList<Group> facilities;
    public Group moveGroup, attackGroup, orientGroup;

    public EnemyInfoProvider(MyStrategy strategy) {
        this.strategy = strategy;
        this.groups = new ArrayList<>();
        this.facilities = new ArrayList<>();
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

        groups.forEach(Group::countUnits);

        facilities.sort(Comparator.comparingDouble(g -> g.getCenter().sqDist(centerPoint)));

        groups.sort((g1, g2) -> {
            if(g1.isAerial() != g2.isAerial())
                return (g2.isAerial() ? -1 : 1);
            return Double.compare(g1.getCenter().sqDist(centerPoint), g2.getCenter().sqDist(centerPoint));
        });

        orientGroup = groups.get(0);

        groups.sort((g1, g2) -> {
            // if both groups are small, skip (cause of xor)
            if(!g1.isFacility() && !g2.isFacility() && (g1.unitCount < 20 ^ g2.unitCount < 20))
                return -Integer.compare(g1.unitCount, g2.unitCount); // less is worse, so 9 is worse then 50
            return Double.compare(g1.getCenter().sqDist(centerPoint), g2.getCenter().sqDist(centerPoint));
        });

        attackGroup = groups.get(0);

        moveGroup = groups.get(0);
        if(facilities.size() > 0 &&
                (moveGroup.getCenter().sqDist(centerPoint) > facilities.get(0).getCenter().sqDist(centerPoint) ||
                        (2 * moveGroup.getCenter().sqDist(centerPoint) > facilities.get(0).getCenter().sqDist(centerPoint) &&
                                moveGroup.unitCount < 100 && !moveGroup.isAerial())))
            moveGroup = facilities.get(0);

        if(!moveGroup.isFacility())
            orientGroup = moveGroup;
    }

    public boolean isFacility() {
        return moveGroup.isFacility();
    }

    public Group getMoveGroup() {
        return moveGroup;
    }

    public Group getOrientGroup() {
        return orientGroup;
    }

    public Group getAttackGroup() {
        return attackGroup;
    }
}