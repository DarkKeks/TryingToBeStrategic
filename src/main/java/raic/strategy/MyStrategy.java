package raic.strategy;

import raic.RewindClient;
import raic.model.*;
import java.util.*;

@SuppressWarnings("WeakerAccess")
public final class MyStrategy implements Strategy {

    public static MyStrategy MY_STRATEGY;

    public boolean sandwichReady;
    public SandwichController sandwichController;

    private Random random;

    public Player player;
    public World world;
    public Game game;
    public Move move;
    public MovementManager movementManager;

    public Map<VehicleType, Integer> groupByType = new HashMap<>();

    public Map<Long, MyVehicle> vehicleById = new HashMap<>();
    public DefaultHashMap<VehicleType, Map<Long, MyVehicle> > vehicleByType = new DefaultHashMap<>(HashMap::new);
    public DefaultHashMap<Integer, Map<Long, MyVehicle> > vehicleByGroup = new DefaultHashMap<>(HashMap::new);

    public MyStrategy() {
        MY_STRATEGY = this;
    }

    @Override
    public void move(Player player, World world, Game game, Move move) {
        this.player = player;
        this.world = world;
        this.game = game;
        this.move = move;

        boolean isFirstTick = random == null;

        if(isFirstTick) {
            System.out.println(game.getRandomSeed());
            random = new Random(game.getRandomSeed());
            movementManager = new MovementManager(this);
            sandwichController = new SandwichController(this);
            groupByType.put(VehicleType.ARRV, 2);
            groupByType.put(VehicleType.TANK, 3);
            groupByType.put(VehicleType.IFV, 4);
            groupByType.put(VehicleType.HELICOPTER, 5);
            groupByType.put(VehicleType.FIGHTER, 6);

        }

        debugRender();

        initMove();
        if(isFirstTick) new GroupGenerator(this);

        process();
        movementManager.move();
    }

    private void debugRender() {
        RewindClient rc = RewindClient.getInstance();
        for(MyVehicle veh : vehicleById.values()) {
            rc.livingUnit(veh.getX(),
                    veh.getY(),
                    veh.getRadius(),
                    veh.getDurability(),
                    veh.getMaxDurability(),
                    (veh.getPlayerId() == player.getId() ? RewindClient.Side.OUR : RewindClient.Side.ENEMY),
                    0,
                    RewindClient.idByType(veh.getType()),
                    0,
                    0,
                    veh.isSelected());
        }
    }

    private void initMove() {
        for(Vehicle veh : world.getNewVehicles()) {
            MyVehicle myVeh = new MyVehicle(veh);
            vehicleById.put(veh.getId(), myVeh);
            vehicleByType.get(veh.getType()).put(veh.getId(), myVeh);
        }

        for(VehicleUpdate update : world.getVehicleUpdates()) {
            long id = update.getId();
            if(update.getDurability() > 0) {
                MyVehicle veh = vehicleById.get(id);
                veh.update(update);
                for(int group : update.getGroups())
                    vehicleByGroup.get(group).put(id, veh);
            } else {
                for(int group : update.getGroups())
                    vehicleByGroup.get(group).remove(id);
                vehicleByType.get(vehicleById.get(id).getType()).remove(id);
                vehicleById.remove(id);
            }
        }
    }

    private void process() {
        if(sandwichReady)
            sandwichController.tick();
    }
}
