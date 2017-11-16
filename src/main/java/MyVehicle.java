package model;

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
    public boolean isMoving;

    public MyVehicle(Vehicle veh) {
        this.id = veh.getId();
        this.x = veh.getX();
        this.y = veh.getY();
        this.radius = veh.getRadius();
        this.playerId = veh.getPlayerId();
        this.durability = veh.getDurability();
        this.maxDurability = getMaxDurability();
        this.maxSpeed = getMaxSpeed();
        this.visionRange = getVisionRange();
        this.squaredVisionRange = getSquaredVisionRange();
        this.groundAttackRange = getGroundAttackRange();
        this.squaredGroundAttackRange = getSquaredGroundAttackRange();
        this.aerialAttackRange = getAerialAttackRange();
        this.squaredAerialAttackRange = getSquaredAerialAttackRange();
        this.groundDamage = getGroundDamage();
        this.aerialDamage = getAerialDamage();
        this.groundDefence = getGroundDefence();
        this.aerialDefence = getAerialDefence();
        this.attackCooldownTicks = getAttackCooldownTicks();
        this.remainingAttackCooldownTicks = getRemainingAttackCooldownTicks();
        this.type = getType();
        this.aerial = isAerial();
        this.selected = isSelected();
        this.groups = veh.getGroups();

        this.lastUpdateTick = this.lastPositionUpdateTick = MyStrategy.MY_STRATEGY.world.getTickIndex();
        this.isMoving = false;
    }

    public void update(VehicleUpdate vehicleUpdate) {
        this.lastUpdateTick = MyStrategy.MY_STRATEGY.world.getTickIndex();
        if(vehicleUpdate.getX() != this.x || vehicleUpdate.getY() != this.y) 
            this.lastPositionUpdateTick = MyStrategy.MY_STRATEGY.world.getTickIndex();
        this.isMoving = this.lastUpdateTick == this.lastPositionUpdateTick;

        this.x = vehicleUpdate.getX();
        this.y = vehicleUpdate.getY();
        this.durability = vehicleUpdate.getDurability();
        this.remainingAttackCooldownTicks = vehicleUpdate.getRemainingAttackCooldownTicks();
        this.selected = vehicleUpdate.isSelected();
        this.groups = vehicleUpdate.getGroups();
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
        return getDistanceTo(unit.x, unit.y);
    }

    public double getSquaredDistanceTo(double x, double y) {
        double dx = x - this.x;
        double dy = y - this.y;
        return dx * dx + dy * dy;
    }

    public double getSquaredDistanceTo(Unit unit) {
        return getSquaredDistanceTo(unit.x, unit.y);
    }
}
