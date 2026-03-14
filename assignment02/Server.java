import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Handles player requests and sends responses of updated game states to the client via the router.
 *
 * @version February 07, 2026
 * @author Lavji, Fareen_543
 */
public class Server {

    private static final int SERVER_PORT = 6000;

    private final DatagramSocket socket;
    private final GameState state;

    public Server() throws Exception {
        this.socket = new DatagramSocket(SERVER_PORT);
        this.state = new GameState();
        System.out.println("[server.app] Listening on UDP port " + SERVER_PORT);
    }

    public static void main(String[] args) throws Exception {
        new Server().runForever();
    }

    public void runForever() throws IOException {
        byte[] buf = new byte[2048];

        while (true) {
            DatagramPacket requestPkt = new DatagramPacket(buf, buf.length);
            socket.receive(requestPkt);

            System.out.println("\n[server.app] RX from router " + requestPkt.getAddress() + ":" + requestPkt.getPort());
            Protocol.printPacket("[server.app] RX", requestPkt);

            String requestStr = Protocol.decode(requestPkt);
            String responseStr = messageHandler(requestStr);

            byte[] responseBytes = Protocol.toBytes(responseStr);
            DatagramPacket responsePkt = new DatagramPacket(
                    responseBytes, responseBytes.length,
                    requestPkt.getAddress(), requestPkt.getPort()
            );

            System.out.println("[server.app] TX (string): " + responseStr);
            Protocol.printBytes("[server.app] TX", responseBytes);

            socket.send(responsePkt);
        }
    }

    // --- REQUEST HANDLERS ---
    public String messageHandler(String msg) {
        if (msg == null) return Protocol.buildResponse(ResponseType.ERROR, "NULL_MESSAGE");

        msg = msg.trim();
        if (msg.isEmpty()) return Protocol.buildResponse(ResponseType.ERROR, "EMPTY_MESSAGE");

        String[] parts = Protocol.split(msg);

        RequestType type;
        try {
            type = RequestType.fromToken(parts[0]);
        } catch (Exception e) {
            return Protocol.buildResponse(ResponseType.ERROR, "UNKNOWN_ACTION");
        }

        try {
            switch (type) {
                case JOIN:
                    return joinHandler(parts);
                case MOVE:
                    return moveHandler(parts);
                case PICKUP:
                    return pickupHandler(parts);
                case STATE:
                    return stateHandler();
                case QUIT:
                    return quitHandler(parts);
                default:
                    return Protocol.buildResponse(ResponseType.ERROR, "UNKNOWN_ACTION");
            }
        } catch (Exception e) {
            return Protocol.buildResponse(ResponseType.ERROR, e.getClass().getSimpleName());
        }
    }

    private String joinHandler(String[] parts) {
        if (parts.length < 2) return Protocol.buildResponse(ResponseType.ERROR, "JOIN_FORMAT");

        String name = parts[1];
        Player p = state.addNewPlayer(name);

        return Protocol.buildResponse(ResponseType.JOINED, String.valueOf(p.getId()));
    }

    private String moveHandler(String[] parts) {
        if (parts.length < 4) return Protocol.buildResponse(ResponseType.ERROR, "MOVE_FORMAT");

        int playerId = Integer.parseInt(parts[1]);
        int dx = Integer.parseInt(parts[2]);
        int dy = Integer.parseInt(parts[3]);

        state.movePlayer(playerId, dx, dy);
        return ResponseType.MOVE_OK.wireToken();
    }

    private String pickupHandler(String[] parts) {
        if (parts.length < 3) return Protocol.buildResponse(ResponseType.ERROR, "PICKUP_FORMAT");

        int playerId = Integer.parseInt(parts[1]);
        int lootId = Integer.parseInt(parts[2]);

        boolean ok = state.processPickup(playerId, lootId);
        return ok ? ResponseType.PICKUP_OK.wireToken() : ResponseType.PICKUP_FAIL.wireToken();
    }

    private String stateHandler() {
        return state.serialize();
    }

    private String quitHandler(String[] parts) {
        return ResponseType.QUIT_OK.wireToken();
    }
}