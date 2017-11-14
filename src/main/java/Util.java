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

}
