import javafx.util.Pair;
import model.VehicleType;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

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
                .next(new MyMove()
                        .addToSelection(surfaceTypes[1])
                        .next(new MyMove()
                                .addToSelection(surfaceTypes[2])
                                .next(new MyMove()
                                        .assign(1)))));

        MyMove move = addPositionMoves(surface, sky);

        addUniteMoves();

        strategy.movementManager.add(move);
    }

    private void addUniteMoves() {

    }

    private MyMove addPositionMoves(Map<VehicleType, Pair<Integer, Integer>> surface,
                                  Map<VehicleType, Pair<Integer, Integer>> sky) {
        MyMove result = new MyMove();

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
                        if(i[0]++ != e.getValue().getKey())
                            result.last().next(new MyMove().clearSelectMove(type,
                                    (i[0] - 1 - e.getValue().getKey()) * Util.DIST_BETW_GROUPS, 0));
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
            if(cur != free)
                if(Math.abs(cur - free) <= 1)
                    result.last().next(new MyMove().clearSelectMove(curType,
                            (free - cur) * Util.DIST_BETW_GROUPS, 0));
                else {
                    MyMove select = new MyMove();
                    boolean first = true;
                    for (VehicleType type : surfaceTypes) if (type != curType) {
                        if (first) {
                            select.clearAndSelect(type);
                            first = false;
                        } else
                            select.last().next(new MyMove().addToSelection(type));
                    }
                    select.last().next(new MyMove()
                            .move((free > cur ? 1 : -1) * Util.DIST_BETW_GROUPS, 0));

                    result.last().next(select);
                }
        }

        boolean first = true;
        for(VehicleType type : surfaceTypes) {
            if(surface.get(type).getValue() != 1) {
                MyMove move = new MyMove()
                        .clearSelectMove(type, 0, (1 - surface.get(type).getValue()) * Util.DIST_BETW_GROUPS);
                if(first) move.condition(Util.isGroupMovingCondition(1).negate());
                first = false;

                result.last().next(move);
            }
        }

        return result;
    }  

    public Point getCornerPointOfType(VehicleType type) {
        Point point = new Point(1025, 1025);
        for(MyVehicle veh : strategy.vehicleById.values())
            if(veh.getType() == type)
                if (veh.getX() < point.getX() || veh.getY() < point.getY())
                    point.setLocation(veh.getX(), veh.getY());

        point.setLocation(Util.getIdxByCoord(point.x),
                Util.getIdxByCoord(point.y));
        return point;
    }
}
