import javafx.util.Pair;
import model.Vehicle;
import model.VehicleType;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GroupGenerator {

    VehicleType surfaceTypes[] = {VehicleType.TANK, VehicleType.IFV, VehicleType.ARRV};
    VehicleType skyTypes[] = {VehicleType.HELICOPTER, VehicleType.FIGHTER};

    public MyStrategy strategy;

    public GroupGenerator(MyStrategy strategy) {
        this.strategy = strategy;

        strategy.movementManager.add(new MyMove()
                .clearAndSelect(VehicleType.FIGHTER)
                .next(new MyMove().scale(0, 0, 2)));

        initGroups();
    }

    public void initGroups() {
        Map<VehicleType, Pair<Integer, Integer>> surface = new HashMap<>();
        Map<VehicleType, Pair<Integer, Integer>> sky = new HashMap<>();

        for(VehicleType type : surfaceTypes) {
            Point point = getCornerPointOfType(type);
            surface.put(type, new Pair<>(point.x, point.y));
        }

        for(VehicleType type : skyTypes) {
            Point point = getCornerPointOfType(type);
            sky.put(type, new Pair<>(point.x, point.y));
        }

        addMoves(surface, sky);
    }

    private void addMoves(Map<VehicleType, Pair<Integer, Integer>> surface,
                          Map<VehicleType, Pair<Integer, Integer>> sky) {
        Map<VehicleType, MyMove> moves = new HashMap<>();

        boolean two = false, three = false;

        int cnt[] = new int[3];
        for(VehicleType type : surfaceTypes) {
            int y = surface.get(type).getValue();
            cnt[y]++;
            if(cnt[y] == 3) three = true;
            if(cnt[y] == 2) two = true;
        }

        if(!two) {
            int i = 0;
            for(VehicleType type : surfaceTypes) {
                moves.put(type, new MyMove()
                        .clearAndSelect(type)
                        .next(new MyMove()
                                .move(Util.getCoordByIdx(i++),
                                        Util.getCoordByIdx(surface.get(type).getValue()))));
            }
        } else if(!three) {
            boolean used[] = new boolean[3];
            for(VehicleType type : surfaceTypes)
                if(cnt[surface.get(type).getKey()] == 2) 
                    used[surface.get(type).getValue()] = true;

            int free = 0;
            for(int i = 0; i < 3; ++i)
                if(!used[i]) free = i;
            for(VehicleType type : surfaceTypes)
                if(cnt[surface.get(type).getKey()] == 1)
                    moves.put(type, new MyMove()
                            .clearAndSelect(type)
                            .next(new MyMove()
                                    .move(Util.getCoordByIdx(free),
                                            Util.getCoordByIdx(surface.get(type).getValue()))));
        }

        for(VehicleType type : surfaceTypes) {
            if(surface.get(type).getValue() != 2) {
                MyMove move = new MyMove()
                        .clearAndSelect(type)
                        .next(new MyMove()
                                .move(Util.getCoordByIdx(surface.get(type).getKey()),
                                        Util.getCoordByIdx(2)));
                if(!moves.contains(type))
                    moves.put(type, new MyMove()           
                moves.get(type).last()
                    .delay(300)
                    .next(move);
            }
        }

        for(MyMove move : moves.values())
            strategy.movementManager.add(move);
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
