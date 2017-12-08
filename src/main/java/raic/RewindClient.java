package raic;

import raic.model.VehicleType;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Locale;

public class RewindClient {

    private static RewindClient INSTANCE;

    private Socket socket;
    private OutputStream outputStream;

    public enum Side {
        OUR(-1),
        NEUTRAL(0),
        ENEMY(1);
        final int side;

        Side(int side) {
            this.side = side;
        }
    }

    public enum AreaType {
        UNKNOWN(0),
        FOREST(1),
        SWAMP(2),
        RAIN(3),
        CLOUD(4),;
        final int areaType;

        AreaType(int areaType) {
            this.areaType = areaType;
        }
    }

    public enum FacilityType {
        CONTROL_CENTER(0),
        VEHICLE_FACTORY(1);
        final int facilityType;

        FacilityType(int facilityType) {
            this.facilityType = facilityType;
        }
    }

    /**
     * Should be send on end of move function all turn primitives can be rendered after that point
     */
    public void endFrame() {
        send("{\"type\":\"end\"}");
    }

    public void circle(double x, double y, double r, Color color, int layer) {
        send(String.format("{\"type\": \"circle\", \"x\": %f, \"y\": %f, \"r\": %f, \"color\": %d, \"layer\": %d}", x, y, r, color.getRGB(), layer));
    }

    public void rect(double x1, double y1, double x2, double y2, Color color, int layer) {
        send(String.format("{\"type\": \"rectangle\", \"x1\": %f, \"y1\": %f, \"x2\": %f, \"y2\": %f, \"color\": %d, \"layer\": %d}", x1, y1, x2, y2, color.getRGB(), layer));
    }

    public void line(double x1, double y1, double x2, double y2, Color color, int layer) {
        send(String.format("{\"type\": \"line\", \"x1\": %f, \"y1\": %f, \"x2\": %f, \"y2\": %f, \"color\": %d, \"layer\": %d}", x1, y1, x2, y2, color.getRGB(), layer));
    }

    public void popup(double x, double y, double r, String text) {
        send(String.format("{\"type\": \"popup\", \"x\": %f, \"y\": %f, \"r\": %f, \"text\": \"%s \"}", x, y, r, text));
    }

    public void livingUnit(double x, double y, double r, int hp, int maxHp,
                    Side side) {
        livingUnit(x, y, r, hp, maxHp, side, 0, null, 0, 0, false);
    }

    public void areaDescription(int cellX, int cellY, AreaType areaType) {
        send(String.format("{\"type\": \"area\", \"x\": %d, \"y\": %d, \"area_type\": %d}", cellX, cellY, areaType.areaType));
    }

    /**
     * Pass arbitrary user message to be stored in frame
     * Message content displayed in separate window inside viewer
     * Can be used several times per frame
     *
     * @param msg .
     */
    public void message(String msg) {
        String s = "{\"type\": \"message\", \"message\" : \"" + msg + " \"}";
        send(s);
    }

    /**
     * Facility - rectangle with texture and progress bars
     * @param cell_x - x cell of top left facility part
     * @param cell_y - y cell of top left facility part
     * @param type - type of facility
     * @param side - enemy, ally or neutral
     * @param production - current production progress, set to 0 if no production
     * @param max_production - maximum production progress, used together with `production`
     * @param capture - current capture progress, should be in range [-max_capture, max_capture],
* where negative values mean that facility is capturing by enemy
     * @param max_capture - maximum capture progress, used together with `capture`
     */

    public void facility(int cell_x, int cell_y, FacilityType type, Side side, int production, int max_production, int capture, int max_capture) {
        send(String.format("{\"type\": \"facility\", \"x\": %d, \"y\": %d, \"facility_type\": %d, \"enemy\": %d, \"production\": %d, \"max_production\": %d, \"capture\": %d, \"max_capture\": %d}",
                cell_x, cell_y, type.facilityType, side.side, production, max_production, capture, max_capture));
    }

    /**
     * @param x           - x pos of the unit
     * @param y           - y pos of the unit
     * @param r           - radius of the unit
     * @param hp          - current health
     * @param maxHp       - max possible health
     * @param side        - owner of the unit
     * @param course      - rotation of the unit - angle in radians [0, 2 * pi) counter clockwise
     * @param type        - id of unit type
     * @param remCooldown -
     * @param maxCooldown -
     */
    public void livingUnit(double x, double y, double r, int hp, int maxHp,
                    Side side, double course, VehicleType type,
                    int remCooldown, int maxCooldown, boolean selected) {
        send(String.format(
                "{\"type\": \"unit\", \"x\": %f, \"y\": %f, \"r\": %f, \"hp\": %d, \"max_hp\": %d, \"enemy\": %d, \"unit_type\":%d, \"course\": %.3f," +
                        "\"rem_cooldown\":%d, \"cooldown\":%d, \"selected\":%d }",
                x, y, r, hp, maxHp, side.side, idByType(type), course, remCooldown, maxCooldown, selected ? 1 : 0));
    }

    void close() {
        try {
            outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public RewindClient(String host, int port) {
        try {
            socket = new Socket(host, port);
            socket.setTcpNoDelay(true);
            outputStream = socket.getOutputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public RewindClient() {
        this("127.0.0.1", 9111);
    }

    private void send(String buf) {
        try {
            outputStream.write(buf.getBytes());
            outputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static int idByType(VehicleType type) {
        switch (type) {
            case TANK:       return 1;
            case IFV:        return 2;
            case ARRV:       return 3;
            case HELICOPTER: return 4;
            case FIGHTER:    return 5;
            default: return 0;
        }

    }

    public static RewindClient getInstance() {
        if(INSTANCE == null) INSTANCE = new RewindClient();
        return INSTANCE;
    }
}