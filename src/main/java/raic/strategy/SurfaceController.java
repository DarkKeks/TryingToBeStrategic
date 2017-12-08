package raic.strategy;

import raic.RewindClient;
import raic.strategy.math.Vec2D;

import java.awt.*;

public class SurfaceController extends SandwichController {

    public SurfaceController(MyStrategy strategy) {
        super(strategy, Util.SANDWICH, Util.SANDWICH_MOVEMENT_SPEED, Util.SANDWICH_ANGLE_SPEED, 70);
    }

    @Override
    public void tick() {
        updateCenterPoint();
        provider.update(centerPoint);
        attackPoint = provider.getSurfaceGroup().getCenter();
        goingForFacility = provider.isFacility();

        boolean anyAlive = false;
        for(MyVehicle veh : strategy.vehicleByGroup.get(Util.SURFACE))
            anyAlive |= veh.alive;

        if(orienting) {
            orientation.rotate(new Vec2D(angleSpeed * Math.signum(rotationAngle)));
        }

        if(anyAlive && !nukeScaling && MyStrategy.world.getOpponentPlayer().getNextNuclearStrikeTickIndex() != -1) {
            dodgeNuke();
        } else if(MyStrategy.player.getRemainingNuclearStrikeCooldownTicks() == 0 &&
                Util.delayCheck(Util.NUKE_RETRY_DELAY, lastNukeRetry)) {
            lastNukeRetry = MyStrategy.world.getTickIndex();

            doNuke();
        } else if(anyAlive && !orientationScheduled && !nukeScaling && shouldOrient(provider.getSurfaceAttackGroup()) &&
                Util.delayCheck(Util.SANDWICH_ORIENTATION_DELAY , lastOrientationTick)) {
            lastOrientationTick = MyStrategy.world.getTickIndex();

            orient();
        } else if(anyAlive && !orientationScheduled && !nukeScaling && Util.delayCheck(Util.ATTACK_MODE_UPDATE_DELAY, lastAttackMoveUpdateTick)) {
            lastAttackMoveUpdateTick = MyStrategy.world.getTickIndex();

            move();
        }

        //TODO: rem start
        if(anyAlive) RewindClient.getInstance().line(centerPoint.getX(), centerPoint.getY(), attackPoint.getX(), attackPoint.getY(), Color.BLACK, 1);
        if(MyStrategy.world.getOpponentPlayer().getNextNuclearStrikeTickIndex() != -1)
            RewindClient.getInstance().circle(MyStrategy.world.getOpponentPlayer().getNextNuclearStrikeX(),
                    MyStrategy.world.getOpponentPlayer().getNextNuclearStrikeY(),
                    50, Color.BLUE, 3);
        if(MyStrategy.world.getMyPlayer().getNextNuclearStrikeTickIndex() != -1)
            RewindClient.getInstance().circle(MyStrategy.world.getMyPlayer().getNextNuclearStrikeX(),
                    MyStrategy.world.getMyPlayer().getNextNuclearStrikeY(),
                    50, Color.ORANGE, 3);
        RewindClient.getInstance().message("Nuke in: " + MyStrategy.player.getRemainingNuclearStrikeCooldownTicks() + "\\n");
        if(anyAlive) RewindClient.getInstance().circle(centerPoint.getX(), centerPoint.getY(), 4, Color.YELLOW, 1);
        if(anyAlive) RewindClient.getInstance().circle(attackPoint.getX(), attackPoint.getY(), 4, Color.RED, 1);

        if(anyAlive) RewindClient.getInstance().line(centerPoint.getX(), centerPoint.getY(), centerPoint.x + orientation.x * 100, centerPoint.y + orientation.y * 100, Color.BLACK, 1);
        //TODO: rem end
    }
}
