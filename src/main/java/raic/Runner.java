package raic;

import model.*;
import strategy.MyStrategy;

import java.io.IOException;
import java.util.Locale;

public final class Runner {
    private final RemoteProcessClient remoteProcessClient;
    private final String token;

    public static void main(String[] args) throws IOException {
        new Runner(args.length == 3 ? args : new String[] {"127.0.0.1", "31001", "0000000000000000"}).run();
    }

    private Runner(String[] args) throws IOException {
        remoteProcessClient = new RemoteProcessClient(args[0], Integer.parseInt(args[1]));
        token = args[2];
    }

    @SuppressWarnings("WeakerAccess")
    public void run() throws IOException {
        Locale.setDefault(Locale.US);
        try {
            remoteProcessClient.writeTokenMessage(token);
            remoteProcessClient.writeProtocolVersionMessage();
            remoteProcessClient.readTeamSizeMessage();
            Game game = remoteProcessClient.readGameContextMessage();

            RewindClient.getInstance().message("Hello World");
            RewindClient.getInstance().endFrame();

            Strategy strategy = new MyStrategy();

            PlayerContext playerContext;

            while ((playerContext = remoteProcessClient.readPlayerContextMessage()) != null) {
                Player player = playerContext.getPlayer();
                if (player == null) {
                    break;
                }

                RewindClient.getInstance().message("Step " + playerContext.getWorld().getTickIndex());

                Move move = new Move();
                strategy.move(player, playerContext.getWorld(), game, move);

                RewindClient.getInstance().endFrame();

                remoteProcessClient.writeMoveMessage(move);
            }

            RewindClient.getInstance().message("Bye!");
            RewindClient.getInstance().endFrame();
            RewindClient.getInstance().close();

        } finally {
            remoteProcessClient.close();
        }
    }
}
