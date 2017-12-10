package raic.strategy;

import raic.RewindClient;
import raic.strategy.enemy.EnemyInfoProvider;
import raic.strategy.math.Vec2D;

import java.awt.*;

public class SandwichController {

    private static double SANDWICH_RADIUS = 70;
    private static double VIEW_COEF = 0.8;

    public MyStrategy strategy;
    public EnemyInfoProvider provider;

    public Point attackPoint;
    public Point orientPoint;
    public Point centerPoint;
    public Point lastNukePoint;
    private Vec2D orientation;
    private double rotationAngle;
    public boolean goingForFacility;

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
        this.orientPoint = new Point(0, 0);
        this.orientation = new Vec2D(1, 0);
    }

    public void tick() {
        updateCenterPoint();
        provider.update(centerPoint);
        attackPoint = provider.getAttackPoint();
        orientPoint = provider.getOrientPoint();
        goingForFacility = provider.isFacility();

        if(orienting) {
            orientation.rotate(new Vec2D(Util.SANDWICH_ANGLE_SPEED * Math.signum(rotationAngle)));
        }

        if(!nukeScaling && MyStrategy.world.getOpponentPlayer().getNextNuclearStrikeTickIndex() != -1) {

            dodgeNuke();
        } else if(MyStrategy.player.getRemainingNuclearStrikeCooldownTicks() == 0 &&
                Util.delayCheck(Util.NUKE_RETRY_DELAY, lastNukeRetry)) {
            lastNukeRetry = MyStrategy.world.getTickIndex();

            doNuke();
        } else if(!orienting && !nukeScaling && shouldOrient() &&
                Util.delayCheck(Util.SANDWICH_ORIENTATION_DELAY, lastOrientationTick)) {
            lastOrientationTick = MyStrategy.world.getTickIndex();

            orient();
        } else if(!orienting && !nukeScaling && Util.delayCheck(Util.ATTACK_MODE_UPDATE_DELAY, lastAttackMoveUpdateTick)) {
            lastAttackMoveUpdateTick = MyStrategy.world.getTickIndex();

            MyMove move;
            Vec2D attackVec = new Vec2D(attackPoint.getX() - centerPoint.getX(),
                    attackPoint.getY() - centerPoint.getY());

            double borderDist = getDistToBorder(true);
            if(borderDist < MyStrategy.game.getIfvGroundAttackRange()) {
                attackVec = getBestMoveDirection(attackVec);
            }

            double closestDist = getClosestDist(false);
            if (closestDist * 2 < MyStrategy.game.getIfvGroundAttackRange()) {
                attackVec.mul(-1);
            }

            move = new MyMove().move(attackVec.x, attackVec.y, Util.SANDWICH_MOVEMENT_SPEED);

            selectSandwichAndMove(move);
        }

        //TODO: rem start
        RewindClient.getInstance().line(centerPoint.getX(), centerPoint.getY(), attackPoint.getX(), attackPoint.getY(), Color.BLACK, 1);
        if(MyStrategy.world.getOpponentPlayer().getNextNuclearStrikeTickIndex() != -1)
            RewindClient.getInstance().circle(MyStrategy.world.getOpponentPlayer().getNextNuclearStrikeX(),
                    MyStrategy.world.getOpponentPlayer().getNextNuclearStrikeY(),
                    50, Color.BLUE, 3);
        if(MyStrategy.world.getMyPlayer().getNextNuclearStrikeTickIndex() != -1)
            RewindClient.getInstance().circle(MyStrategy.world.getMyPlayer().getNextNuclearStrikeX(),
                    MyStrategy.world.getMyPlayer().getNextNuclearStrikeY(),
                    50, Color.ORANGE, 3);
        RewindClient.getInstance().message("Nuke in: " + MyStrategy.player.getRemainingNuclearStrikeCooldownTicks() + "\\n");
        RewindClient.getInstance().circle(centerPoint.getX(), centerPoint.getY(), 4, Color.YELLOW, 1);
        RewindClient.getInstance().circle(attackPoint.getX(), attackPoint.getY(), 4, Color.RED, 1);
        //TODO: rem end
    }

    private boolean shouldOrient() {
        int nukeCooldown = MyStrategy.world.getOpponentPlayer().getRemainingNuclearStrikeCooldownTicks();
        int timeWithoutNuke = MyStrategy.world.getTickIndex() - lastOpponentNuke;
        boolean nukeLongAgo = timeWithoutNuke - Util.getOpponentPlayerNuclearStrikeDelay() > 50;
        boolean nuke = nukeLongAgo || nukeCooldown > 70;

        rotationAngle = orientAngle();
        boolean needed = Math.abs(rotationAngle) > 0.1;

        boolean fac = !goingForFacility;

        return nuke && needed && fac;
    }

    private double orientAngle() {
        double current = orientation.angle();
        Vec2D newRot = new Vec2D(attackPoint.getX() - centerPoint.getX(), attackPoint.getY() - centerPoint.getY());
        double needed = newRot.angle();

        double res = Math.PI * 10;
        for(int i = -3; i <= 3; ++i)
            res = Util.absMin(res, needed - current + i * Math.PI);
        return res;
    }


    private void orient() {
        MyStrategy.movementManager.add(new MyMove()
                .clearAndSelect(Util.SANDWICH)
                .next(new MyMove()
                    .rotate(centerPoint.getX(), centerPoint.getY(), rotationAngle, 0.0, Util.SANDWICH_ANGLE_SPEED)
                    .next(new MyMove()
                            .condition(Util.isGroupMovingCondition(Util.SANDWICH).negate())
                            .onApply(() -> this.orienting = false))));
        orienting = true;
    }

    private void doNuke() {
        // TODO: chose closest enemy
        if(provider.getGroup().isFacility()) return;
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

    private void dodgeNuke() {
        orienting = false;
        lastOpponentNuke = MyStrategy.world.getTickIndex();
        lastNukePoint = new Point(MyStrategy.world.getOpponentPlayer().getNextNuclearStrikeX(),
                MyStrategy.world.getOpponentPlayer().getNextNuclearStrikeY());
        if (lastNukePoint.sqDist(centerPoint) < SANDWICH_RADIUS * SANDWICH_RADIUS) {
            selectSandwichAndMove(new MyMove()
                    .scale(lastNukePoint.getX(), lastNukePoint.getY(), 10)
                    .next(new MyMove()
                            .condition(strategy -> MyStrategy.world.getOpponentPlayer().getNextNuclearStrikeTickIndex() == -1)
                            .scale(lastNukePoint.getX(), lastNukePoint.getY(), 0.1)
                            .next(new MyMove()
                                    .condition(Util.isGroupMovingCondition(Util.SANDWICH).negate())
                                    .onApply(() -> nukeScaling = false))));
            nukeScaling = true;
        }
    }

    private double getDistToBorder(boolean isAerialAllowed) {
        double distToBorder = 1e9;
        for(MyVehicle veh : strategy.vehicleByGroup.get(Util.SANDWICH)) {
            if(veh.alive && (!isAerialAllowed && veh.isAerial())) continue;
            distToBorder = Math.min(distToBorder, veh.x);
            distToBorder = Math.min(distToBorder, veh.y);
            distToBorder = Math.min(distToBorder, 1024 - veh.x);
            distToBorder = Math.min(distToBorder, 1024 - veh.y);
        }
        return distToBorder;
    }

    private double getClosestDist(boolean isAerialAllowed) {
        double dist = 1e9;

        if(provider.getGroup().isFacility())
            return dist;

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

    private void selectSandwichAndMove(MyMove move) {
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

    public Vec2D getVecToBorder() {
        double distToBorder = 1e9;
        Vec2D vec = null;

        int dx[] = new int[]{-1024, 1024, 0,    0};
        int dy[] = new int[]{0,     0,    1024, -1024};
        int c[]  = new int[]{0,     1024, 1024, 0};

        for(int i = 0; i < 4; ++i) {
            for(MyVehicle veh : strategy.vehicleByGroup.get(group)) {
                if(veh.alive) continue;
                double newX = veh.getX() + dx[i];
                double newY = veh.getY() + dy[i];
                double dist = 0;
                if(newX < 0 || newX > 1024)
                    dist = Math.abs(c[i] - veh.getX());
                if(newY < 0 || newY > 1024)
                    dist = Math.abs(c[i] - veh.getY());
                if(dist != 0 && dist < distToBorder) {
                    distToBorder = dist;
                    vec = new Vec2D(dx[i], dy[i]);
                }
            }
        }
        if(vec == null) return new Vec2D();
        return vec.length(distToBorder);
    }

    public Vec2D getAvoidBorderDirection(Vec2D borderDirection, Vec2D attackDirection) {
        if(Math.abs(attackDirection.angle() - borderDirection.angle()) > Math.PI / 2)
            return attackDirection;

        double minAngle = 228;
        Vec2D bestVec = null;
        for(int i = 0; i < 4; ++i) {
            Vec2D vec = new Vec2D(i * Math.PI / 2);
            vec.mul(100);

            double angleToBorder = Math.abs(borderDirection.angle() - vec.angle());
            double angle = Math.abs(attackDirection.angle() - vec.angle());
            if(compareDoubleLess(Math.PI / 2, angleToBorder) && angle < minAngle) {
                minAngle = angle;
                bestVec = vec;
            }
        }
        return bestVec;
    }

    public static boolean compareDoubleLess(double a, double b) {
        return a < b + 1e-5;
    }
}
