package raic.strategy;

import raic.model.VehicleType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class Util {

    public static final int DIST_BETW_GROUPS = 74;
    public static final int CENTER_POINT = 119;

    public static final int SANDWICH = 1;
    public static final int SURFACE = 2;
    public static final int SKY = 3;

    public static final int SANDWICH_ORIENTATION_DELAY = 300;

    public static final double SANDWICH_MOVEMENT_SPEED = 0.18;

    public static int getIdxByCoord(int coord) {
        switch (coord) {
            case 18: return 0;
            case 92: return 1;
            case 166: return 2;
            default: return -1;
        }
    }

    public static int getCoordByIdx(int coord) {
        switch (coord) {
            case 0: return 18;
            case 1: return 92;
            case 2: return 166;
            default: return -1;
        }
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new HashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    public static Predicate<MyStrategy> isGroupMovingCondition(final int groupId) {
        return (strategy) -> {
            for(MyVehicle veh : MyStrategy.MY_STRATEGY.vehicleByGroup.get(groupId).values())
                if(!veh.enemy && veh.isMoving())
                    return true;
            return false;
        };
    }

    public static Predicate<MyStrategy> isTypeMovingCondition(final VehicleType type) {
        return (strategy) -> {
            for(MyVehicle veh : MyStrategy.MY_STRATEGY.vehicleByType.get(type).values())
                if(!veh.enemy && veh.isMoving())
                    return true;
            return false;
        };
    }

}
