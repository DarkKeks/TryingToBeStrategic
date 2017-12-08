package raic.strategy;

import raic.RewindClient;
import raic.strategy.math.Vec2D;

import java.awt.*;

public class SkyController extends SandwichController {

    private boolean goingForOwnSurface;

    public SkyController(MyStrategy strategy) {
        super(strategy, Util.SKY, Util.SKY_MOVEMENT_SPEED, Util.SKY_ANGLE_SPEED, 45);
    }

    @Override
    public void tick() {
        updateCenterPoint();
        provider.update(strategy.surfaceController.centerPoint);
        attackPoint = provider.getSkyGroup().getCenter();

        boolean anyAlive = false;
        for(MyVehicle veh : strategy.vehicleByGroup.get(Util.SKY))
            anyAlive |= veh.alive;

        goingForOwnSurface = !provider.getSurfaceGroup().isFacility();

        if(orienting) {
            orientation.rotate(new Vec2D(angleSpeed * Math.signum(rotationAngle)));
        }

        if(anyAlive) {
            if (!nukeScaling && MyStrategy.world.getOpponentPlayer().getNextNuclearStrikeTickIndex() != -1) {
                dodgeNuke();
            } else if (!orientationScheduled && !nukeScaling && shouldOrient(provider.getSkyGroup()) &&
                    Util.delayCheck(Util.SKY_ORIENTATION_DELAY, lastOrientationTick)) {
                lastOrientationTick = MyStrategy.world.getTickIndex();

                orient();
            } else if (!orientationScheduled && !nukeScaling && Util.delayCheck(Util.ATTACK_MODE_UPDATE_DELAY, lastAttackMoveUpdateTick)) {
                lastAttackMoveUpdateTick = MyStrategy.world.getTickIndex();

                move();
            }
        }

        //TODO: rem start
        if(anyAlive) {
            RewindClient.getInstance().line(centerPoint.getX(), centerPoint.getY(), attackPoint.getX(), attackPoint.getY(), Color.BLACK, 1);
            RewindClient.getInstance().circle(centerPoint.getX(), centerPoint.getY(), 4, Color.YELLOW, 1);
            RewindClient.getInstance().circle(attackPoint.getX(), attackPoint.getY(), 4, Color.RED, 1);
            RewindClient.getInstance().line(centerPoint.getX(), centerPoint.getY(), centerPoint.x + orientation.x * 100, centerPoint.y + orientation.y * 100, Color.BLACK, 1);
        }
        //TODO: rem end
    }

    @Override
    public void move() {
        if(goingForOwnSurface) {
            if(strategy.sandwichReady)
                attackPoint = strategy.surfaceController.centerPoint;
            else
                return;
        }

        MyMove move;
        Vec2D attackVec = new Vec2D(attackPoint.getX() - centerPoint.getX(),
                attackPoint.getY() - centerPoint.getY());

        if (!goingForOwnSurface) {
            double closestDist = getClosestDist(group, provider.getSkyGroup(), true);
            if(provider.getSkyGroup().unitCount > 20 && closestDist * 3 < MyStrategy.game.getFighterVisionRange()) {
                attackVec.mul(-1);
            }
        }

        Vec2D border = getVecToBorder(true);
        if (border.length() < MyStrategy.game.getFighterVisionRange()) {
            attackVec = getAvoidBorderDirection(border, attackVec);
        }

        move = new MyMove().move(attackVec.x, attackVec.y, speed);

        selectSandwichAndMove(move);
    }
}
