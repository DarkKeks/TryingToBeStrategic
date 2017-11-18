import javafx.util.Pair;
import model.VehicleType;

import java.awt.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GroupGenerator {

    VehicleType allTypes[] = {VehicleType.TANK, VehicleType.IFV, VehicleType.ARRV, VehicleType.FIGHTER, VehicleType.HELICOPTER};
    VehicleType surfaceTypes[] = {VehicleType.TANK, VehicleType.IFV, VehicleType.ARRV};
    VehicleType skyTypes[] = {VehicleType.FIGHTER, VehicleType.HELICOPTER};

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
                .clearAndSelect(0, 0, 1024, 1024)
                .next(new MyMove()
                        .assign(Util.SANDWICH)));

        strategy.movementManager.add(new MyMove()
                .clearAndSelect(surfaceTypes[0])
                .next(new MyMove()
                        .addToSelection(surfaceTypes[1])
                        .next(new MyMove()
                                .addToSelection(surfaceTypes[2])
                                .next(new MyMove()
                                        .assign(Util.SURFACE)))));

        strategy.movementManager.add(new MyMove()
                .clearAndSelect(skyTypes[0])
                .next(new MyMove()
                        .addToSelection(skyTypes[1])
                        .next(new MyMove()
                                .assign(Util.SKY))));


        MyMove move = addPositionMoves(surface, sky);
        addUniteMoves(move);

        move.last().next(new MyMove()
                .condition(Util.isGroupMovingCondition(Util.SANDWICH).negate())
                .clearAndSelect(Util.SANDWICH)
                .next(new MyMove()
                        .scale(Util.CENTER_POINT, Util.CENTER_POINT, 0.1, Util.SANDWICH_MOVEMENT_SPEED)
                        .next(new MyMove()
                                .condition(Util.isGroupMovingCondition(Util.SANDWICH).negate())
                                .move(900, 900, Util.SANDWICH_MOVEMENT_SPEED))));

        strategy.movementManager.add(move);
    }

    private void addUniteMoves(MyMove move) {
        // SPLIT
        MyMove res = new MyMove()
                .condition(Util.isGroupMovingCondition(Util.SANDWICH).negate())
                .generator(strategy -> {
                    final MyMove[] result = new MyMove[1];
                    final boolean[] first = {true};
                    strategy.vehicleById.values()
                            .stream()
                            .filter(veh -> !veh.enemy)
                            .filter(Util.distinctByKey(MyVehicle::getY))
                            .sorted((a, b) -> Double.compare(Math.abs(b.getY() - Util.CENTER_POINT), Math.abs(a.getY() - Util.CENTER_POINT)))
                            .forEach(veh -> {
                                MyMove newMove = new MyMove()
                                        .clearAndSelect(0, veh.getY() - 1, 1024, veh.getY() + 1)
                                        .next(new MyMove().move(0, (veh.getY() - Util.CENTER_POINT) / 0.706));
                                if (first[0]) {
                                    result[0] = newMove;
                                    first[0] = false;
                                } else
                                    result[0].last().next(newMove);
                            });
                    return result[0];
                });
        // TILT
        res.last().next(new MyMove()
                .condition(Util.isGroupMovingCondition(Util.SANDWICH).negate()));
        int i = 0;
        for(VehicleType type : surfaceTypes)
            res.last().next(new MyMove().clearSelectMove(type, 0, (1.0 - i++) * 4.5));
        i = 0;
        for(VehicleType type : skyTypes)
            res.last().next(new MyMove().clearSelectMove(type, 0, (1.0 - i++) * 4.5));
        // MERGE
        res.last().next(new MyMove()
                .condition(Util.isGroupMovingCondition(Util.SANDWICH).negate())
                .generator(strategy -> {
                    MyMove result = new MyMove();
                    for(VehicleType type : allTypes) {
                        Point p = getCornerPointOfType(type);
                        result.last().next(new MyMove()
                                .clearSelectMove(type, Util.getCoordByIdx(1) - Util.getCoordByIdx(p.x), 0));
                    }
                    return result;
                }));
        move.last().next(res);
    }

    private MyMove addPositionMoves(Map<VehicleType, Pair<Integer, Integer>> surface,
                                  Map<VehicleType, Pair<Integer, Integer>> sky) {
        MyMove result = new MyMove();

        addSkyMoves(result, sky);
        addSurfaceMoves(result, surface);

        return result;
    }

    private void addSkyMoves(MyMove result, Map<VehicleType, Pair<Integer, Integer>> sky) {
        if(Objects.equals(sky.get(skyTypes[0]).getValue(), sky.get(skyTypes[1]).getValue())) {
            int c = (sky.get(skyTypes[0]).getValue() != 2 ? 1 : -1);
            result.clearSelectMove(skyTypes[0], c * Util.DIST_BETW_GROUPS, 0);
        }

        boolean first = true;
        for(VehicleType type : skyTypes) {
            if(sky.get(type).getValue() != 1) {
                MyMove move = new MyMove()
                        .clearSelectMove(type, 0, (1 - sky.get(type).getValue()) * Util.DIST_BETW_GROUPS);
                if(first) move.condition(Util.isGroupMovingCondition(Util.SKY).negate());
                first = false;

                result.last().next(move);
            }
        }
    }

    public void addSurfaceMoves(MyMove result, Map<VehicleType, Pair<Integer, Integer>> surface) {
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
                if(first) move.condition(Util.isGroupMovingCondition(Util.SURFACE).negate());
                first = false;

                result.last().next(move);
            }
        }
    }

    public Point getCornerPointOfType(VehicleType type) {
        MyVehicle v = strategy.vehicleById.values().stream()
                .filter(veh -> veh.getType() == type)
                .sorted(Comparator.comparingDouble(MyVehicle::getX).thenComparing(Comparator.comparingDouble(MyVehicle::getY)))
                .findFirst().get();
        Point point = new Point((int)v.getX(), (int)v.getY());

        point.setLocation(Util.getIdxByCoord(point.x),
                Util.getIdxByCoord(point.y));
        return point;
    }
}
