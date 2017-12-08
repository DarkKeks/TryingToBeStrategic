package raic.strategy;

import raic.model.VehicleType;

import java.util.*;

public class  GroupGenerator {

    public static VehicleType allTypes[] = {VehicleType.TANK, VehicleType.IFV, VehicleType.ARRV, VehicleType.FIGHTER, VehicleType.HELICOPTER};
    VehicleType surfaceTypes[] = {VehicleType.TANK, VehicleType.IFV, VehicleType.ARRV};
    VehicleType skyTypes[] = {VehicleType.FIGHTER, VehicleType.HELICOPTER};

    public MyStrategy strategy;

    public GroupGenerator(MyStrategy strategy) {
        this.strategy = strategy;

        initGroups();
    }

    public void initGroups() {
        Map<VehicleType, Point> surface = new HashMap<>();
        Map<VehicleType, Point> sky = new HashMap<>();

        for(VehicleType type : surfaceTypes) {
            Point point = getCornerPointOfType(type);
            point.set(Util.getIdxByCoord(point.intX()), Util.getIdxByCoord(point.intY()));
            surface.put(type, point);
        }

        for(VehicleType type : skyTypes) {
            Point point = getCornerPointOfType(type);
            point.set(Util.getIdxByCoord(point.intX()), Util.getIdxByCoord(point.intY()));
            sky.put(type, point);
        }

        MyStrategy.movementManager.add(new MyMove()
                .clearAndSelect(surfaceTypes[0])
                .next(new MyMove()
                        .addToSelection(surfaceTypes[1])
                        .next(new MyMove()
                                .addToSelection(surfaceTypes[2])
                                .next(new MyMove()
                                        .assign(Util.SURFACE)))));

        MyStrategy.movementManager.add(new MyMove()
                .clearAndSelect(skyTypes[0])
                .next(new MyMove()
                        .addToSelection(skyTypes[1])
                        .next(new MyMove()
                                .assign(Util.SKY))));

        MyMove skyMove = new MyMove();
        addSkyMoves(skyMove, sky);
        addUniteMoves(skyMove, skyTypes, Util.SKY, Util.SKY_CENTER_POINT);

        skyMove.last().next(new MyMove()
                .condition(Util.isGroupMovingCondition(Util.SKY).negate())
                .clearAndSelect(Util.SKY)
                .next(new MyMove()
                        .scale(Util.CENTER_POINT, Util.SKY_CENTER_POINT, 0.1, 0)
                        .next(new MyMove()
                                .condition(Util.isGroupMovingCondition(Util.SKY).negate()))));
        skyMove.last().onApply(() -> strategy.skyReady = true);

        MyStrategy.movementManager.add(skyMove);

        MyMove surfaceMove = new MyMove();
        addSurfaceMoves(surfaceMove, surface);
        addUniteMoves(surfaceMove, surfaceTypes, Util.SANDWICH, Util.CENTER_POINT);

        surfaceMove.last().next(new MyMove()
                .condition(Util.isGroupMovingCondition(Util.SANDWICH).negate())
                .clearAndSelect(Util.SANDWICH)
                .next(new MyMove()
                        .scale(Util.CENTER_POINT, Util.CENTER_POINT, 0.1, 0)
                        .next(new MyMove()
                                .condition(Util.isGroupMovingCondition(Util.SANDWICH).negate()))));
        surfaceMove.last().onApply(() -> strategy.sandwichReady = true);

        MyStrategy.movementManager.add(surfaceMove);
    }

    private void addUniteMoves(MyMove move, VehicleType[] types, int group, double rowCenter) {
        // SPLIT
        MyMove res = new MyMove()
                .condition(Util.isGroupMovingCondition(group).negate())
                .generator(strategy -> {
                    final MyMove[] result = new MyMove[1];
                    final boolean[] first = {true};
                    double expandCoeff = (4.0 * (types.length) + 2) / 10;
                    strategy.vehicleByGroup.get(group)
                            .stream()
                            .filter(veh -> veh.alive && !veh.enemy)
                            .filter(Util.distinctByKey((veh) -> Math.round(veh.getY())))
                            .sorted((a, b) -> Double.compare(Math.abs(b.getY() - rowCenter), Math.abs(a.getY() - rowCenter)))
                            .forEach(veh -> {
                                MyMove newMove = new MyMove()
                                        .clearAndSelect(0, veh.getY() - 1, 1024, veh.getY() + 1)
                                        .next(new MyMove().move(0, (veh.getY() - rowCenter) * expandCoeff));
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
                .condition(Util.isGroupMovingCondition(group).negate()));
        int i = 0;
        for(VehicleType type : types)
            res.last().next(new MyMove().clearSelectMove(type, 0, (1.0 - i++) * 4.5));
        // MERGE
        res.last().next(new MyMove()
                .condition(Util.isGroupMovingCondition(group).negate())
                .generator(strategy -> {
                    MyMove result = new MyMove();
                    for(VehicleType type : types) {
                        Point p = getCornerPointOfType(type);
                        result.last().next(new MyMove()
                                .clearSelectMove(type, Util.getCoordByIdx(1) - p.getX(), 0));
                    }
                    return result;
                }));
        move.last().next(res);
    }

    private void addSkyMoves(MyMove result, Map<VehicleType, Point> sky) {
        if(Objects.equals(sky.get(skyTypes[0]).intX(), sky.get(skyTypes[1]).intX())) { // В одном столбце
            int c = (sky.get(skyTypes[0]).intX() != 2 ? 1 : -1);                       // Если 0 или 1 -> вправо, иначе влево
            result.clearSelectMove(skyTypes[0], c * Util.DIST_BETW_GROUPS, 0);   // Выделить + подвинуть
        }

        boolean first = true;
        for(VehicleType type : skyTypes) {
            MyMove move = new MyMove()
                    .clearSelectMove(type, 0, (3 - sky.get(type).intY()) * Util.DIST_BETW_GROUPS); // Выделить подвинуть
            if(first) move.condition(Util.isGroupMovingCondition(Util.SKY).negate());
            first = false;

            result.last().next(move);
        }
    }

    public void addSurfaceMoves(MyMove result, Map<VehicleType, Point> surface) {
        boolean three = false;
        int cnt[] = new int[3];
        for(VehicleType type : surfaceTypes) {
            int y = surface.get(type).intY();
            cnt[y]++;
            if(cnt[y] == 3) three = true;
        }

        if(!three) {
            final int[] i = {0};
            surface.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue((p1, p2) -> (Integer.compare(p1.intX(), p2.intX()))))
                    .forEach((e) -> {
                        VehicleType type = e.getKey();
                        if (i[0]++ != e.getValue().intX())
                            result.last().next(new MyMove().clearSelectMove(type,
                                    (i[0] - 1 - e.getValue().intX()) * Util.DIST_BETW_GROUPS, 0));
                    });
        }

        // GONNA TEST SOMETHING, DON'T REMOVE
//        } else if(!three) {
//            boolean used[] = new boolean[3];
//            for(VehicleType type : surfaceTypes)
//                if(cnt[surface.get(type).intY()] == 2)
//                    used[surface.get(type).intX()] = true;
//
//            int free = 0;
//            int cur = 0;
//            VehicleType curType = null;
//
//            for(int i = 0; i < 3; ++i)
//                if(!used[i]) free = i;
//            for(VehicleType type : surfaceTypes) {
//                if (cnt[surface.get(type).intY()] == 1) {
//                    cur = surface.get(type).intX();
//                    curType = type;
//                }
//            }
//            if(cur != free)
//                if(Math.abs(cur - free) <= 1)
//                    result.last().next(new MyMove().clearSelectMove(curType,
//                            (free - cur) * Util.DIST_BETW_GROUPS, 0));
//                else {
//                    MyMove select = new MyMove();
//                    boolean first = true;
//                    for (VehicleType type : surfaceTypes) if (type != curType) {
//                        if (first) {
//                            select.clearAndSelect(type);
//                            first = false;
//                        } else
//                            select.last().next(new MyMove().addToSelection(type));
//                    }
//                    select.last().next(new MyMove()
//                            .move((free > cur ? 1 : -1) * Util.DIST_BETW_GROUPS, 0));
//
//                    result.last().next(select);
//                }
//        }

        boolean first = true;
        for(VehicleType type : surfaceTypes) {
            if(surface.get(type).intY() != 1) {
                MyMove move = new MyMove()
                        .clearSelectMove(type, 0, (1 - surface.get(type).intY()) * Util.DIST_BETW_GROUPS);
                if(first) move.condition(Util.isGroupMovingCondition(Util.SURFACE).negate());
                first = false;

                result.last().next(move);
            }
        }
    }

    public Point getCornerPointOfType(VehicleType type) {
        Point point = new Point(1e9, 1e9);
        for(MyVehicle veh : strategy.vehicleByType.get(type))
            if(veh.alive && !veh.enemy && point.compareTo(veh.getX(), veh.getY()) > 0)
                point.set(veh.getX(), veh.getY());
        return point;
    }
}
