import model.*;

import java.util.*;

@SuppressWarnings("WeakerAccess")
public final class MyStrategy implements Strategy {

    public static MyStrategy MY_STRATEGY;

    private Random random;

    public Player player;
    public World world;
    public Game game;
    public Move move;
    public MovementManager movementManager;

    public Map<VehicleType, Integer> groupByType = new HashMap<>();
    public Map<Long, Vehicle> vehicleById = new HashMap<>();

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
            random = new Random(game.getRandomSeed());
            movementManager = new MovementManager(this);
        }

        initMove();
        if(isFirstTick) new GroupGenerator(this);

        debugRender();

        if(movementManager.canMove())
            process();
        movementManager.move();
    }

    private void debugRender() {
        RewindClient rc = RewindClient.getInstance();
        for(Vehicle veh : vehicleById.values()) {
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
            vehicleById.put(veh.getId(), veh);
        }

        for(VehicleUpdate update : world.getVehicleUpdates()) {
            long id = update.getId();
            if(update.getDurability() > 0)
                vehicleById.put(id, new Vehicle(vehicleById.get(id), update));
            else
                vehicleById.remove(update.getId());
        }
    }

    private void process() {

    }
}
