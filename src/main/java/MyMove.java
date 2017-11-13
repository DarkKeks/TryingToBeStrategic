import model.Move;
import model.VehicleType;

import java.util.function.Predicate;

public class MyMove {

    public Move move = new Move();

    public void clearAndSelect(int left, int top, int right, int bottom, VehicleType type) {
    }

    public void clearAndSelect(VehicleType type) {
    }

    public void clearAndSelect(int groupId) {
    }

    public void addToSelection(int left, int top, int right, int bottom, VehicleType type) {
    }

    public void addToSelection(VehicleType type) {
    }

    public void addToSelection(int groupId) {
    }

    public void deselect() {
    }

    public void assign(int groupId) {
    }

    public void disband(int groupId){
    }

    public void move(double x, double y, double maxSpeed) {
    }

    public void move(double x, double y) {
    }

    public void rotate(double x, double y, double angle, double maxSpeed, double maxAngularSpeed) {
    }

    public void rotate(double x, double y, double angle) {
    }

    public void scale(double x, double y, double factor) {
    }

    public void nuke(double x, double y, long vehId) {
    }

}
