import model.Vehicle;
import model.VehicleType;

import java.awt.*;

public class GroupGenerator {

    public MyStrategy strategy;

    public GroupGenerator(MyStrategy strategy) {
        this.strategy = strategy;
        initGroups();
    }

    public void initGroups() {
        VehicleType surfaceTypes[] = {VehicleType.TANK, VehicleType.IFV, VehicleType.ARRV};
        VehicleType skyTypes[] = {VehicleType.HELICOPTER, VehicleType.FIGHTER};

        VehicleType posSurface[][] = new VehicleType[3][3];
        VehicleType posSky[][] = new VehicleType[3][3];

        for(VehicleType type : surfaceTypes) {
            Point point = getCornerPointOfType(type);
            posSurface[point.x][point.y] = type;
        }

        for(VehicleType type : skyTypes) {
            Point point = getCornerPointOfType(type);
            posSky[point.x][point.y] = type;
        }

        addMoves(posSurface, posSky);
    }

    private void addMoves(VehicleType[][] posSurface, VehicleType[][] posSky) {
    }

    public Point getCornerPointOfType(VehicleType type) {
        Point point = new Point(1025, 1025);
        for(Vehicle veh : strategy.vehicleById.values())
            if(veh.getType() == type)
                if (veh.getX() < point.getX() || veh.getY() < point.getY())
                    point.setLocation(veh.getX(), veh.getY());

        point.setLocation(Util.getIdxByCoord(point.x),
                Util.getIdxByCoord(point.y));
        return point;
    }
}
