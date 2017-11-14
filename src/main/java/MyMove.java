import model.ActionType;
import model.Move;
import model.VehicleType;

import java.util.function.Predicate;

public class MyMove {

    public static int ID = 0;
    public int hash;

    public boolean hasNext;
    public MyMove next;
    public Predicate<MyStrategy> condition = (strategy) -> true;
    public int delay;

    public int lastCalled = -1000000;
    public boolean applied = false;

    public Move move = new Move();

    public MyMove() {
        this.hash = ID++;
        move.setAction(ActionType.NONE);
    }

    public MyMove last() {
        MyMove res = this;
        while(res.hasNext) res = res.next;
    }

    public boolean canBeApplied() {
        return !applied && condition.test(MyStrategy.MY_STRATEGY);
    }

    public boolean canDoNext() {
        return applied && MyStrategy.MY_STRATEGY.world.getTickIndex() >= lastCalled + delay;
    }

    public MyMove condition(Predicate<MyStrategy> condition) {
        this.condition = condition;
        return this;
    }

    public MyMove next(MyMove next, int delay) {
        this.hasNext = true;
        this.delay = delay;
        this.next = next;
        return this;
    }

    public MyMove next(MyMove next) {
        return next(next, 0);
    }

    public void apply(Move move) {
        move.setAction(this.move.getAction());

        move.setGroup(this.move.getGroup());

        move.setLeft(this.move.getLeft());
        move.setRight(this.move.getRight());
        move.setTop(this.move.getTop());
        move.setBottom(this.move.getBottom());

        move.setX(this.move.getX());
        move.setY(this.move.getY());
        move.setAngle(this.move.getAngle());
        move.setFactor(this.move.getFactor());

        move.setMaxSpeed(this.move.getMaxSpeed());
        move.setMaxAngularSpeed(this.move.getMaxAngularSpeed());

        move.setVehicleType(this.move.getVehicleType());

        move.setVehicleId(this.move.getVehicleId());
        move.setFacilityId(this.move.getFacilityId());

        lastCalled = MyStrategy.MY_STRATEGY.world.getTickIndex();
        applied = true;
    }

    public MyMove clearAndSelect(int left, int top, int right, int bottom, VehicleType type) {
        move.setAction(ActionType.CLEAR_AND_SELECT);
        move.setLeft(left);
        move.setRight(right);
        move.setTop(top);
        move.setBottom(bottom);
        move.setVehicleType(type);
        return this;
    }

    public MyMove clearAndSelect(VehicleType type) {
        return clearAndSelect(0, 0,1024, 1024, type);
    }

    public MyMove clearAndSelect(int groupId) {
        move.setAction(ActionType.CLEAR_AND_SELECT);
        move.setGroup(groupId);
        return this;
    }

    public MyMove addToSelection(int left, int top, int right, int bottom, VehicleType type) {
        clearAndSelect(left, top, right, bottom, type);
        move.setAction(ActionType.ADD_TO_SELECTION);
        return this;
    }

    public MyMove addToSelection(VehicleType type) {
        return addToSelection(0, 0, 1024, 1024, type);
    }

    public MyMove addToSelection(int groupId) {
        move.setAction(ActionType.ADD_TO_SELECTION);
        move.setGroup(groupId);
        return this;
    }

    public MyMove deselect() {
        move.setAction(ActionType.DESELECT);
        return this;
    }

    public MyMove assign(int groupId) {
        move.setAction(ActionType.ASSIGN);
        move.setGroup(groupId);
        return this;
    }

    public MyMove dismiss(int groupId) {
        move.setAction(ActionType.DISMISS);
        move.setGroup(groupId);
        return this;
    }

    public MyMove disband(int groupId){
        move.setAction(ActionType.DISBAND);
        move.setGroup(groupId);
        return this;
    }

    public MyMove move(double x, double y, double maxSpeed) {
        move.setAction(ActionType.MOVE);
        move.setX(x);
        move.setY(y);
        move.setMaxSpeed(maxSpeed);
        return this;
    }

    public MyMove move(double x, double y) {
        return move(x, y, 1e9);
    }

    public MyMove rotate(double x, double y, double angle, double maxSpeed, double maxAngularSpeed) {
        move.setAction(ActionType.ROTATE);
        move.setX(x);
        move.setY(y);
        move.setAngle(angle);
        move.setMaxSpeed(maxSpeed);
        move.setMaxAngularSpeed(maxAngularSpeed);
        return this;
    }

    public MyMove rotate(double x, double y, double angle) {
        return rotate(x, y, angle, 1e9, 1e9);
    }

    public MyMove scale(double x, double y, double factor) {
        move.setAction(ActionType.SCALE);
        move.setX(x);
        move.setY(y);
        move.setFactor(factor);
        return this;
    }

    public MyMove nuke(double x, double y, long vehId) {
        move.setAction(ActionType.TACTICAL_NUCLEAR_STRIKE);
        move.setX(x);
        move.setY(y);
        move.setVehicleId(vehId);
        return this;
    }

    @Override
    public int hashCode() {
        return hash;
    }
}
