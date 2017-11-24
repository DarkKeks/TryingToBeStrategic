package raic.strategy.enemy;

import raic.model.VehicleType;
import raic.strategy.MyVehicle;
import raic.strategy.Point;

import java.util.ArrayList;
import java.util.EnumMap;

public class Group {

    public Point center;
    public int unitCount;
    public EnumMap<VehicleType, Integer> count;
    public ArrayList<MyVehicle> vehicles;

    public Group() {
        unitCount = 0;
        count = new EnumMap<>(VehicleType.class);
        vehicles = new ArrayList<>();
    }

    public void add(MyVehicle veh) {
        unitCount++;
        vehicles.add(veh);
        count.put(veh.getType(), count.getOrDefault(veh.getType(), 0) + 1);
    }

    public Point getCenter() {
        if(center == null) {
            double x, y;
            int count = 0;
            x = y = 0;
            for(MyVehicle veh : vehicles) {
                x += veh.getX();
                y += veh.getY();
                count++;
            }
            center = (count > 0 ? new Point(x / count, y / count) : new Point(0, 0));
        }
        return center;
    }
}
