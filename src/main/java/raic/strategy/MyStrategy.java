package raic.strategy;

import raic.RewindClient;
import raic.model.*;
import raic.strategy.enemy.Group;

import java.awt.*;
import java.util.*;

@SuppressWarnings("WeakerAccess")
public final class MyStrategy implements Strategy {

    public static MyStrategy MY_STRATEGY;

    public boolean sandwichReady;
    public SandwichController sandwichController;

    public Move lastSelection = new Move();

    private Random random;

    public static Player player;
    public static World world;
    public static Game game;
    public static Move move;
    public static MovementManager movementManager;

    public Map<VehicleType, Integer> groupByType = new HashMap<>();

    public Map<Long, MyVehicle> vehicleById = new HashMap<>();
    public ArrayList<MyVehicle> vehicles = new ArrayList<>();
    public DefaultHashMap<VehicleType, ArrayList<MyVehicle> > vehicleByType = new DefaultHashMap<>(ArrayList::new);
    public DefaultHashMap<Integer, ArrayList<MyVehicle> > vehicleByGroup = new DefaultHashMap<>(ArrayList::new);

    public Map<Long, Facility> facilityById = new HashMap<>();
    public DefaultHashMap<FacilityType, ArrayList<Facility> > facilityByType = new DefaultHashMap<>(ArrayList::new);

    public static TerrainType[][] terrain;
    public static WeatherType[][] weather;

    public MyStrategy() {
        MY_STRATEGY = this;
    }

    @Override
    public void move(Player _player, World _world, Game _game, Move _move) {
        player = _player;
        world = _world;
        game = _game;
        move = _move;

        boolean isFirstTick = random == null;

        if(isFirstTick) {
            random = new Random(game.getRandomSeed());
            terrain = world.getTerrainByCellXY();
            weather = world.getWeatherByCellXY();
            movementManager = new MovementManager(this);
            sandwichController = new SandwichController(this);
            groupByType.put(VehicleType.ARRV, 2);
            groupByType.put(VehicleType.TANK, 3);
            groupByType.put(VehicleType.IFV, 4);
            groupByType.put(VehicleType.HELICOPTER, 5);
            groupByType.put(VehicleType.FIGHTER, 6);
        }

        initMove();
        initFacilities();

        debugRender();

        if(isFirstTick) new GroupGenerator(this);

        process();
        movementManager.move();
    }

    private void initFacilities() {
        for(Facility fac : world.getFacilities()) {
            facilityById.put(fac.getId(), fac);
            facilityByType.get(fac.getType()).add(fac);
        }
    }

    private void drawInColors() {
        //TODO: rem start
        for(MyVehicle veh : vehicles) {
            if(veh.alive) {
                int c = 0;
                for(int i = 0; i < 5; ++i) if(GroupGenerator.allTypes[i] == veh.getType()) c = i;
                RewindClient.getInstance().circle(veh.getX(), veh.getY(), 2, color[c], 2);
            }
        }
        //TODO: rem end
    }

    private void debugRender() {
        //TODO: rem start
        RewindClient rc = RewindClient.getInstance();

        rc.message("My Score: " + player.getScore() + "\\n");
        rc.message("Opponent Score: " + world.getOpponentPlayer().getScore() + "\\n");

        for(MyVehicle veh : vehicleById.values()) {
            rc.livingUnit(veh.getX(),
                    veh.getY(),
                    veh.getRadius(),
                    veh.getDurability(),
                    veh.getMaxDurability(),
                    (veh.getPlayerId() == player.getId() ? RewindClient.Side.OUR : RewindClient.Side.ENEMY),
                    0,
                    veh.getType(),
                    0,
                    0,
                    veh.isSelected());
        }
        for(Facility fac : facilityById.values()) {
            rc.facility((int)Math.round(fac.getLeft() / 32),
                    (int)Math.round(fac.getTop() / 32),
                    (fac.getType() == FacilityType.CONTROL_CENTER ? RewindClient.FacilityType.CONTROL_CENTER : RewindClient.FacilityType.VEHICLE_FACTORY),
                    (fac.getOwnerPlayerId() == -1 ? RewindClient.Side.NEUTRAL :
                            (fac.getOwnerPlayerId() == player.getId() ? RewindClient.Side.OUR : RewindClient.Side.ENEMY)),
                    fac.getProductionProgress(),
                    Util.getMaxProduction(fac),
                    (int)Math.round(fac.getCapturePoints()),
                    (int)Math.round(game.getMaxFacilityCapturePoints()));
        }
        //TODO: rem end
    }

    private void initMove() {
        for(Vehicle veh : world.getNewVehicles()) {
            MyVehicle myVeh = new MyVehicle(veh);
            if(!vehicleById.containsKey(veh.getId())) {
                vehicles.add(myVeh);
                vehicleByType.get(veh.getType()).add(myVeh);
            }
            vehicleById.put(veh.getId(), myVeh);
        }

        for(VehicleUpdate update : world.getVehicleUpdates()) {
            long id = update.getId();
            MyVehicle veh = vehicleById.get(id);
            if(update.getDurability() > 0) {
                for(int group : update.getGroups())
                    if(!veh.isInGroup(group)) vehicleByGroup.get(group).add(veh);
            } else {
                vehicleById.remove(id);
            }
            veh.update(update);
        }
    }

    private Color[] color = new Color[]{Color.RED, Color.YELLOW, Color.GREEN, Color.MAGENTA, Color.BLUE, Color.BLACK};

    private void process() {
        if(sandwichReady)
            sandwichController.tick();

        //TODO: rem start
        for(int i = 0; i < sandwichController.provider.groups.size(); ++i) {
            Group group = sandwichController.provider.groups.get(i);
            if(!group.isFacility()) {
                RewindClient.getInstance().circle(group.getCenter().getX(), group.getCenter().getY(),
                        2.2, color[i % color.length], 1);
            } else {
                Facility fac = group.facility;
                Color col = color[i % color.length];
                RewindClient.getInstance().rect(fac.getLeft() - 1, fac.getTop() - 1,
                        fac.getLeft() + 65, fac.getTop() + 65,
                        new Color(col.getRed(), col.getGreen(), col.getBlue(), 50), 1);
            }
        }
        //TODO: rem end
    }
}
