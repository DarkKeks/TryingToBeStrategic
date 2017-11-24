package raic.strategy;

import raic.model.VehicleType;
import raic.strategy.enemy.EnemyInfoProvider;
import raic.strategy.math.Vec2D;

public class SandwichController {

    public MyStrategy strategy;
    public EnemyInfoProvider provider;

    private Point centerPoint;
    private Vec2D orientation;

    public int lastOrientationTick = -Util.SANDWICH_ORIENTATION_DELAY;
    public int lastAttackMoveUpdateTick = -Util.ATTACK_MODE_UPDATE_DELAY;

    private boolean orienting = false;

    public SandwichController(MyStrategy strategy) {
        this.strategy = strategy;
        this.provider = new EnemyInfoProvider(strategy);
        this.centerPoint = new Point(119, 119);
        this.orientation = new Vec2D(1, 0);
    }

    public void tick() {
        provider.update();
        Point attackPoint = provider.getAttackPoint();
        if(!orienting) {
            if(Util.delayCheck(Util.SANDWICH_ORIENTATION_DELAY, lastOrientationTick)) {
                lastOrientationTick = strategy.world.getTickIndex();

                updateCenterPoint();
                orient(attackPoint);
            } else if(Util.delayCheck(Util.ATTACK_MODE_UPDATE_DELAY, lastAttackMoveUpdateTick)) {
                lastAttackMoveUpdateTick = strategy.world.getTickIndex();

                updateCenterPoint();

                MyMove move;
                double closestDist = getClosestDist();
                if(closestDist * 2 > strategy.game.getIfvGroundAttackRange()) {
                    move = new MyMove().move(attackPoint.getX() - centerPoint.getX(),
                            attackPoint.getY() - centerPoint.getY(),
                            Util.SANDWICH_MOVEMENT_SPEED);
                } else {
                    move = new MyMove().move(-(attackPoint.getX() - centerPoint.getX()),
                            -(attackPoint.getY() - centerPoint.getY()),
                            Util.SANDWICH_MOVEMENT_SPEED);
                }

                if(strategy.lastSelection.getGroup() != Util.SANDWICH)
                    strategy.movementManager.add(new MyMove()
                            .clearAndSelect(Util.SANDWICH)
                            .next(move));
                else
                    strategy.movementManager.add(move);
            }
        }
    }

    private void orient(Point attackPoint) {
        double current = orientation.angle();
        double negCurrent = (current > 0 ? current - Math.PI : current + Math.PI);
        Vec2D newRot = new Vec2D(attackPoint.getX() - centerPoint.getX(), attackPoint.getY() - centerPoint.getY());
        double needed = newRot.angle();
        double res = Util.absMin(needed - current, needed - negCurrent);
        if(Math.abs(res) < 0.05) return;
        orientation = newRot;
        strategy.movementManager.add(new MyMove()
                .clearAndSelect(Util.SANDWICH)
                .next(new MyMove()
                        .scale(centerPoint.getX(), centerPoint.getY(), 1.1)
                        .next(new MyMove()
                                .condition(Util.isGroupMovingCondition(Util.SANDWICH).negate())
                                .rotate(centerPoint.getX(), centerPoint.getY(), res)
                                .next(new MyMove()
                                        .condition(Util.isGroupMovingCondition(Util.SANDWICH).negate())
                                        .scale(centerPoint.getX(), centerPoint.getY(), 0.1)
                                        .next(new MyMove()
                                                .condition(Util.isGroupMovingCondition(Util.SANDWICH).negate())
                                                .onApply(() -> this.orienting = false))))));
        orienting = true;
    }

    private Point m1, m2;

    private double getClosestDist() {
        double dist = 1e9;
        m1 = new Point(0,0);
        m2 = new Point(0,0);
        for(MyVehicle a : strategy.vehicleByGroup.get(Util.SANDWICH)) {
            if(!a.alive || a.type == VehicleType.HELICOPTER || a.type == VehicleType.FIGHTER) continue;
            for(MyVehicle b : provider.getGroup().vehicles) {
                if(!b.alive || b.type == VehicleType.HELICOPTER || b.type == VehicleType.FIGHTER) continue;
                double newDist = a.getSquaredDistanceTo(b.getX(), b.getY());
                if(newDist < dist) {
                    m1.set(a.getX(), a.getY()); m2.set(b.getX(), b.getY());
                    dist = newDist;
                }
            }
        }
        return Math.sqrt(dist);
    }

    private void updateCenterPoint() {
        double x = 0, y = 0;
        int count = 0;
        for(MyVehicle vehicle : strategy.vehicleByGroup.get(Util.SANDWICH)) {
            if(!vehicle.alive) continue;
            count++;
            x += vehicle.getX();
            y += vehicle.getY();
        }
        if(count > 0)
            centerPoint.set(x / count, y / count);
        else centerPoint.set(0, 0);
    }
}
