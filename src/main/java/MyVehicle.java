import model.Unit;
import model.Vehicle;
import model.VehicleType;
import model.VehicleUpdate;

import java.util.Arrays;

import static java.lang.StrictMath.hypot;

@SuppressWarnings({"AbstractClassWithoutAbstractMethods", "WeakerAccess"})
public class MyVehicle {
    public long playerId;
    public int durability;
    public int maxDurability;
    public double maxSpeed;
    public double visionRange;
    public double squaredVisionRange;
    public double groundAttackRange;
    public double squaredGroundAttackRange;
    public double aerialAttackRange;
    public double squaredAerialAttackRange;
    public int groundDamage;
    public int aerialDamage;
    public int groundDefence;
    public int aerialDefence;
    public int attackCooldownTicks;
    public int remainingAttackCooldownTicks;
    public VehicleType type;
    public boolean aerial;
    public boolean selected;
    public int[] groups;
    public long id;
    public double x;
    public double y;
    public double radius;

    public int lastUpdateTick;
    public int lastPositionUpdateTick;
    public boolean enemy;

    public MyVehicle(Vehicle veh) {
        this.id = veh.getId();
        this.x = veh.getX();
        this.y = veh.getY();
        this.radius = veh.getRadius();
        this.playerId = veh.getPlayerId();
        this.durability = veh.getDurability();
        this.maxDurability = veh.getMaxDurability();
        this.maxSpeed = veh.getMaxSpeed();
        this.visionRange = veh.getVisionRange();
        this.squaredVisionRange = veh.getSquaredVisionRange();
        this.groundAttackRange = veh.getGroundAttackRange();
        this.squaredGroundAttackRange = veh.getSquaredGroundAttackRange();
        this.aerialAttackRange = veh.getAerialAttackRange();
        this.squaredAerialAttackRange = veh.getSquaredAerialAttackRange();
        this.groundDamage = veh.getGroundDamage();
        this.aerialDamage = veh.getAerialDamage();
        this.groundDefence = veh.getGroundDefence();
        this.aerialDefence = veh.getAerialDefence();
        this.attackCooldownTicks = veh.getAttackCooldownTicks();
        this.remainingAttackCooldownTicks = veh.getRemainingAttackCooldownTicks();
        this.type = veh.getType();
        this.aerial = veh.isAerial();
        this.selected = veh.isSelected();
        this.groups = veh.getGroups();

        this.lastUpdateTick = this.lastPositionUpdateTick = MyStrategy.MY_STRATEGY.world.getTickIndex();
        this.enemy = this.playerId != MyStrategy.MY_STRATEGY.player.getId();
    }

    public void update(VehicleUpdate vehicleUpdate) {
        this.lastUpdateTick = MyStrategy.MY_STRATEGY.world.getTickIndex();
        if(vehicleUpdate.getX() != this.x || vehicleUpdate.getY() != this.y) 
            this.lastPositionUpdateTick = MyStrategy.MY_STRATEGY.world.getTickIndex();

        this.x = vehicleUpdate.getX();
        this.y = vehicleUpdate.getY();
        this.durability = vehicleUpdate.getDurability();
        this.remainingAttackCooldownTicks = vehicleUpdate.getRemainingAttackCooldownTicks();
        this.selected = vehicleUpdate.isSelected();
        this.groups = vehicleUpdate.getGroups();
    }

    public boolean isMoving() {
        return lastPositionUpdateTick == MyStrategy.MY_STRATEGY.world.getTickIndex();
    }
    
    public double getRadius() {
        return radius;
    }

    public long getId() {
        return id;
    }

    public final double getX() {
        return x;
    }

    public final double getY() {
        return y;
    }

    public long getPlayerId() {
        return playerId;
    }

    public int getDurability() {
        return durability;
    }

    public int getMaxDurability() {
        return maxDurability;
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public double getVisionRange() {
        return visionRange;
    }

    public double getSquaredVisionRange() {
        return squaredVisionRange;
    }

    public double getGroundAttackRange() {
        return groundAttackRange;
    }

    public double getSquaredGroundAttackRange() {
        return squaredGroundAttackRange;
    }

    public double getAerialAttackRange() {
        return aerialAttackRange;
    }

    public double getSquaredAerialAttackRange() {
        return squaredAerialAttackRange;
    }

    public int getGroundDamage() {
        return groundDamage;
    }

    public int getAerialDamage() {
        return aerialDamage;
    }

    public int getGroundDefence() {
        return groundDefence;
    }

    public int getAerialDefence() {
        return aerialDefence;
    }

    public int getAttackCooldownTicks() {
        return attackCooldownTicks;
    }

    public int getRemainingAttackCooldownTicks() {
        return remainingAttackCooldownTicks;
    }

    public VehicleType getType() {
        return type;
    }

    public boolean isAerial() {
        return aerial;
    }

    public boolean isSelected() {
        return selected;
    }

    public int[] getGroups() {
        return Arrays.copyOf(groups, groups.length);
    }

    public double getDistanceTo(double x, double y) {
        return hypot(x - this.x, y - this.y);
    }

    public double getDistanceTo(Unit unit) {
        return getDistanceTo(unit.getX(), unit.getY());
    }

    public double getSquaredDistanceTo(double x, double y) {
        double dx = x - this.x;
        double dy = y - this.y;
        return dx * dx + dy * dy;
    }

    public double getSquaredDistanceTo(Unit unit) {
        return getSquaredDistanceTo(unit.getX(), unit.getY());
    }
}
