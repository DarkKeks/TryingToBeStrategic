public class Util {

    public static final int DIST_BETW_GROUPS = 74;

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

    public Predicate<MyStrategy> isGroupMovingCondition(final int groupId) {
        return () -> {
            boolean moving = true;
            for(MyVehicle veh : MyStrategy.MY_STRATEGY.vehicleByGroup.get(groupId).values())
                if(veh.isMoving)
                    return true;
            return false;
        };
    }

    public Predicate<MyStrategy> isTypeMovingCondition(final VehicleType type) {
        return () -> {
            boolean moving = true;
            for(MyVehicle veh : MyStrategy.MY_STRATEGY.vehicleByType.get(type).values())
                if(veh.isMoving)
                    return true;
            return false;
        };
    }

}
