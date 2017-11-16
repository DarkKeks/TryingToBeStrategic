import javafx.util.Pair;
import model.Vehicle;
import model.VehicleType;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GroupGenerator {

    VehicleType surfaceTypes[] = {VehicleType.TANK, VehicleType.IFV, VehicleType.ARRV};
    VehicleType skyTypes[] = {VehicleType.HELICOPTER, VehicleType.FIGHTER};

    public MyStrategy strategy;

    public GroupGenerator(MyStrategy strategy) {
        this.strategy = strategy;

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

        strategy.movementManager.add(new MyMove()
            .clearAndSelect(surfaceTypes[0])
            .addToSelection(surfaceTypes[1])
            .addToSelection(surfaceTypes[2])
            .assign(1));

        Map<VehicleType, MyMove> moves = addPositionMoves(surface, sky);
        addUniteMoves();

        for(MyMove move : moves.values) {
            strategy.movementManager.add(move);
        }
    }

    private void addUniteMoves() {

    }

    private Map<VehicleType, MyMove> addPositionMoves(Map<VehicleType, Pair<Integer, Integer>> surface,
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
            final int[] i = {0};
            surface.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue((p1, p2) -> (Integer.compare(p1.getKey(), p2.getKey()))))
                    .forEach((e) -> {
                        VehicleType type = e.getKey();
                        moves.put(type, new MyMove().clearAssignSelectMove(type, strategy.groupByType(type),
                                (i[0]++ - e.getValue().getKey()) * Util.DIST_BETW_GROUPS, 0));
                    });
        } else if(!three) {
            boolean used[] = new boolean[3];
            for(VehicleType type : surfaceTypes)
                if(cnt[surface.get(type).getValue()] == 2)
                    used[surface.get(type).getKey()] = true;

            int free = 0;
            int cur = 0;
            VehicleType curType = null;

            for(int i = 0; i < 3; ++i)
                if(!used[i]) free = i;
            for(VehicleType type : surfaceTypes) {
                if (cnt[surface.get(type).getValue()] == 1) {
                    cur = surface.get(type).getKey();
                    curType = type;
                }
            }
            if(Math.abs(cur - free) <= 1)
                moves.put(curType, new MyMove().clearSelectMove(curType, strategy.groupByType(type),
                        (free - cur) * Util.DIST_BETW_GROUPS, 0));
            else if(cur != free)
                for(VehicleType type : surfaceTypes)
                    if(type != curType)
                        moves.put(type, new MyMove().clearSelectMove(type, strategy.groupByType(type),
                                (free > cur ? 1 : -1) * Util.DIST_BETW_GROUPS, 0));
        }

        for(VehicleType type : surfaceTypes) {
            if(surface.get(type).getValue() != 1) {
                MyMove move = new MyMove()
                        .condition(Util.isGroupMovingCondition(1))
                        .clearSelectMove(type, 0, (1 - surface.get(type).getValue()) * Util.DIST_BETW_GROUPS);
                if(!moves.containsKey(type))
                    moves.put(type, new MyMove());
                moves.get(type).last()
                    .next(move);
            }
        }

        return moves;
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
