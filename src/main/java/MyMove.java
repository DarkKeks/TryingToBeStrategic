import model.ActionType;
import model.Move;
import model.VehicleType;

public class MyMove {

    public Move move = new Move();

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
    }

    public void clearAndSelect(int left, int top, int right, int bottom, VehicleType type) {
        move.setAction(ActionType.CLEAR_AND_SELECT);
        move.setLeft(left);
        move.setLeft(left);
        move.setTop(top);
        move.setBottom(bottom);
        move.setVehicleType(type);
    }

    public void clearAndSelect(VehicleType type) {
        clearAndSelect(0, 0,1024, 1024, type);
    }

    public void clearAndSelect(int groupId) {
        move.setAction(ActionType.CLEAR_AND_SELECT);
        move.setGroup(groupId);
    }

    public void addToSelection(int left, int top, int right, int bottom, VehicleType type) {
        clearAndSelect(left, top, right, bottom, type);
        move.setAction(ActionType.ADD_TO_SELECTION);
    }

    public void addToSelection(VehicleType type) {
        addToSelection(0, 0, 1024, 1024, type);
    }

    public void addToSelection(int groupId) {
        move.setAction(ActionType.ADD_TO_SELECTION);
        move.setGroup(groupId);
    }

    public void deselect() {
        move.setAction(ActionType.DESELECT);
    }

    public void assign(int groupId) {
        move.setAction(ActionType.ASSIGN);
        move.setGroup(groupId);
    }

    public void dismiss(int groupId) {
        move.setAction(ActionType.DISMISS);
        move.setGroup(groupId);
    }

    public void disband(int groupId){
        move.setAction(ActionType.DISBAND);
        move.setGroup(groupId);
    }

    public void move(double x, double y, double maxSpeed) {
        move.setAction(ActionType.MOVE);
        move.setX(x);
        move.setY(y);
        move.setMaxSpeed(maxSpeed);
    }

    public void move(double x, double y) {
        move(x, y, 1e9);
    }

    public void rotate(double x, double y, double angle, double maxSpeed, double maxAngularSpeed) {
        move.setAction(ActionType.ROTATE);
        move.setX(x);
        move.setY(y);
        move.setAngle(angle);
        move.setMaxSpeed(maxSpeed);
        move.setMaxAngularSpeed(maxAngularSpeed);
    }

    public void rotate(double x, double y, double angle) {
        rotate(x, y, angle, 1e9, 1e9);
    }

    public void scale(double x, double y, double factor) {
        move.setAction(ActionType.SCALE);
        move.setX(x);
        move.setY(y);
        move.setFactor(factor);
    }

    public void nuke(double x, double y, long vehId) {
        move.setAction(ActionType.TACTICAL_NUCLEAR_STRIKE);
        move.setX(x);
        move.setY(y);
        move.setVehicleId(vehId);
    }

}
