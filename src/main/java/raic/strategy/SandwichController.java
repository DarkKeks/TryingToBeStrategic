package raic.strategy;

import raic.RewindClient;
import raic.model.VehicleType;
import raic.strategy.enemy.EnemyInfoProvider;
import raic.strategy.math.Vec2D;

import java.awt.*;

public class SandwichController {

    private static double SANDWICH_RADIUS = 70;
    private static double VIEW_COEF = 0.8;

    public MyStrategy strategy;
    public EnemyInfoProvider provider;

    public Point centerPoint;
    public Point lastNukePoint;
    private Vec2D orientation;

    public int lastOrientationTick = -Util.SANDWICH_ORIENTATION_DELAY;
    public int lastAttackMoveUpdateTick = -Util.ATTACK_MODE_UPDATE_DELAY;
    public int lastNukeRetry = -Util.NUKE_RETRY_DELAY;
    public int lastOpponentNuke = -123123;

    private boolean orienting = false;
    private boolean nukeScaling = false;

    public SandwichController(MyStrategy strategy) {
        this.strategy = strategy;
        this.provider = new EnemyInfoProvider(strategy);
        this.centerPoint = new Point(119, 119);
        this.orientation = new Vec2D(1, 0);
    }

    public void tick() {
        updateCenterPoint();
        provider.update(centerPoint);
        Point attackPoint = provider.getAttackPoint();

        int nukeCd = MyStrategy.world.getOpponentPlayer().getRemainingNuclearStrikeCooldownTicks();
        int nukeRemainingTicks = Math.min(nukeCd, 1200 - nukeCd);

        if(!orienting) {
            if(!nukeScaling && MyStrategy.world.getOpponentPlayer().getNextNuclearStrikeTickIndex() != -1) {
                lastOpponentNuke = MyStrategy.world.getTickIndex();
                lastNukePoint = new Point(MyStrategy.world.getOpponentPlayer().getNextNuclearStrikeX(),
                        MyStrategy.world.getOpponentPlayer().getNextNuclearStrikeY());
                if (lastNukePoint.sqDist(centerPoint) < SANDWICH_RADIUS * SANDWICH_RADIUS) {
                    selectAndMove(new MyMove()
                            .scale(lastNukePoint.getX(), lastNukePoint.getY(), 10)
                            .next(new MyMove()
                                    .condition(strategy -> MyStrategy.world.getOpponentPlayer().getNextNuclearStrikeTickIndex() == -1)
                                    .scale(lastNukePoint.getX(), lastNukePoint.getY(), 0.1)
                                    .next(new MyMove()
                                            .condition(Util.isGroupMovingCondition(Util.SANDWICH).negate())
                                            .onApply(() -> nukeScaling = false))));
                    nukeScaling = true;
                }
            } else if(MyStrategy.player.getRemainingNuclearStrikeCooldownTicks() == 0 &&
                    Util.delayCheck(Util.NUKE_RETRY_DELAY, lastNukeRetry)) {
                lastNukeRetry = MyStrategy.world.getTickIndex();

                doNuke();
            } else if(!nukeScaling &&
                    (nukeRemainingTicks > 70 || MyStrategy.world.getTickIndex() - lastOpponentNuke > 1200 + 50) &&
                    Util.delayCheck(Util.SANDWICH_ORIENTATION_DELAY, lastOrientationTick)) {
                lastOrientationTick = MyStrategy.world.getTickIndex();

                orient(attackPoint);
            } else if(!nukeScaling && Util.delayCheck(Util.ATTACK_MODE_UPDATE_DELAY, lastAttackMoveUpdateTick)) {
                lastAttackMoveUpdateTick = MyStrategy.world.getTickIndex();

                MyMove move;
                double closestDist = getClosestDist(false);
                if(closestDist * 2 > MyStrategy.game.getIfvGroundAttackRange()) {
                    move = new MyMove().move(
                            attackPoint.getX() - centerPoint.getX(),
                            attackPoint.getY() - centerPoint.getY(),
                            Util.SANDWICH_MOVEMENT_SPEED);
                } else {
                    move = new MyMove().move(
                            -(attackPoint.getX() - centerPoint.getX()),
                            -(attackPoint.getY() - centerPoint.getY()),
                            Util.SANDWICH_MOVEMENT_SPEED);
                }

                selectAndMove(move);
            }
        }

        //TODO: rem start
        RewindClient.getInstance().message("Nuke in: " + MyStrategy.player.getRemainingNuclearStrikeCooldownTicks());
        RewindClient.getInstance().circle(centerPoint.getX(), centerPoint.getY(), 4, Color.YELLOW, 1);
        RewindClient.getInstance().circle(attackPoint.getX(), attackPoint.getY(), 4, Color.RED, 1);
        //TODO: rem end
    }


    private void doNuke() {
        if(getClosestDist(true) > 70) return;

        double maxDmg = -1e9;
        MyVehicle bestAttack = null;
        MyVehicle bestView = null;
        for(MyVehicle veh : provider.getGroup().vehicles) {
            if(veh.alive) {
                MyVehicle attackVeh = null;
                for (MyVehicle myVeh : strategy.vehicleByGroup.get(Util.SANDWICH)) {
                    if (myVeh.alive) {
                        double viewDistance = Util.getViewDistance(myVeh) * VIEW_COEF;
                        if (myVeh.getSquaredDistanceTo(veh.getX(), veh.getY()) <= viewDistance * viewDistance)
                            attackVeh = myVeh;
                    }
                }
                if(attackVeh != null) {
                    int possibleDamage = 0;
                    for(MyVehicle dmgVeh : strategy.vehicles) {
                        if(dmgVeh.alive) {
                            possibleDamage += (dmgVeh.enemy ? 1 : -1) * Math.max(0, (int)(99 - veh.getDistanceTo(dmgVeh.getX(), dmgVeh.getY()) * 2));
                        }
                    }
                    if(possibleDamage > maxDmg) {
                        maxDmg = possibleDamage;
                        bestView = attackVeh;
                        bestAttack = veh;
                    }
                }
            }
        }
        //TODO: rem start
        if(bestAttack != null)
            for(MyVehicle dmgVeh : strategy.vehicles) {
                int damage = (dmgVeh.enemy ? 1 : -1) * Math.max(0, (int)(99 - bestAttack.getDistanceTo(dmgVeh.getX(), dmgVeh.getY()) * 2)) + 100;

                if(damage != 100)
                    RewindClient.getInstance().circle(dmgVeh.getX(), dmgVeh.getY(), 2.3,
                            new Color((255 * (200 - damage)) / 200, (255 * damage) / 200, 0), 3);
            }
        //TODO: rem end
        if(bestAttack != null && maxDmg > 1500) {
            MyStrategy.movementManager.add(new MyMove()
                    .nuke(bestAttack.getX(), bestAttack.getY(), bestView.getId()));

            //TODO: rem start
            RewindClient.getInstance().circle(bestAttack.getX(), bestAttack.getY(), 3, Color.GREEN, 3);
            RewindClient.getInstance().circle(bestView.getX(), bestView.getY(), 3, Color.MAGENTA, 3);
            //TODO: rem end
        }
    }

    private void orient(Point attackPoint) {
        //TODO: rem start
        RewindClient.getInstance().line(centerPoint.getX(), centerPoint.getY(), attackPoint.getX(), attackPoint.getY(), Color.BLACK, 1);
        //TODO: rem end

        double current = orientation.angle();
        double negCurrent = (current > 0 ? current - Math.PI : current + Math.PI);
        Vec2D newRot = new Vec2D(attackPoint.getX() - centerPoint.getX(), attackPoint.getY() - centerPoint.getY());
        double needed = newRot.angle();
        double res = Util.absMin(needed - current, needed - negCurrent);
        if(Math.abs(res) < 0.05) return;
        orientation = newRot;
        MyStrategy.movementManager.add(new MyMove()
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

    private double getClosestDist(boolean isAerialAllowed) {
        double dist = 1e9;
        for(MyVehicle a : strategy.vehicleByGroup.get(Util.SANDWICH)) {
            if(!a.alive || (!isAerialAllowed && a.isAerial())) continue;
            for(MyVehicle b : provider.getGroup().vehicles) {
                if(!b.alive || (!isAerialAllowed && b.isAerial())) continue;
                double newDist = a.getSquaredDistanceTo(b.getX(), b.getY());
                if(newDist < dist) {
                    dist = newDist;
                }
            }
        }
        return Math.sqrt(dist);
    }

    private void selectAndMove(MyMove move) {
        if(strategy.lastSelection.getGroup() != Util.SANDWICH)
            MyStrategy.movementManager.add(new MyMove()
                    .clearAndSelect(Util.SANDWICH)
                    .next(move));
        else
            MyStrategy.movementManager.add(move);
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
