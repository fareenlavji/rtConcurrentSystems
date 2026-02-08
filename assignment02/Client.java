import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

/**
 * Client application that packages and sends player requests to the server via router.
 *
 * @version February 07, 2026
 * @author Lavji, Fareen_543
 */
public class Client {

    private static final String ROUTER_HOST = "localhost";
    private static final int ROUTER_PORT = 5000;

    private final DatagramSocket socket;
    private final InetAddress routerAddress;

    private int playerId = -1;

    public Client() throws Exception {
        this.socket = new DatagramSocket(); // ephemeral
        this.routerAddress = InetAddress.getByName(ROUTER_HOST);

        System.out.println("[client.app] Local UDP port: " + socket.getLocalPort());
        System.out.println("[client.app] Router target: " + ROUTER_HOST + ":" + ROUTER_PORT);
    }

    public static void main(String[] args) throws Exception {
        new Client().run();
    }

    public void run() throws IOException {
        Scanner sc = new Scanner(System.in);

        // JOIN
        System.out.print("Enter player name: ");
        String playerName = sc.nextLine().trim();

        String joinReq = joinBuilder(playerName);
        sendRequest(joinReq);

        String joinResp = receiveResponse();

        // Parse response type using enum
        String[] joinParts = Protocol.split(joinResp);
        if (joinParts.length >= 2) {
            try {
                ResponseType rt = ResponseType.fromToken(joinParts[0]);
                if (rt == ResponseType.JOINED) {
                    playerId = Integer.parseInt(joinParts[1]);
                    System.out.println("[client.app] Joined successfully. playerId=" + playerId);
                } else {
                    System.out.println("[client.app] Join failed: " + joinResp);
                    socket.close();
                    return;
                }
            } catch (Exception e) {
                System.out.println("[client.app] Join failed (bad response): " + joinResp);
                socket.close();
                return;
            }
        } else {
            System.out.println("[client.app] Join failed (bad response): " + joinResp);
            socket.close();
            return;
        }

        // Gameplay loop
        System.out.println("\nCommands: MOVE dx dy | PICKUP lootId | STATE | QUIT");

        while (true) {
            System.out.print("> ");
            String line = sc.nextLine().trim();
            if (line.isEmpty()) continue;

            String[] tokens = line.split("\\s+");
            String cmd = tokens[0].toUpperCase();

            String request;

            switch (cmd) {
                case "MOVE":
                    if (tokens.length < 3) {
                        System.out.println("Usage: MOVE dx dy");
                        continue;
                    }
                    int dx = Integer.parseInt(tokens[1]);
                    int dy = Integer.parseInt(tokens[2]);
                    request = moveBuilder(dx, dy);
                    break;

                case "PICKUP":
                    if (tokens.length < 2) {
                        System.out.println("Usage: PICKUP lootId");
                        continue;
                    }
                    int lootId = Integer.parseInt(tokens[1]);
                    request = pickupBuilder(lootId);
                    break;

                case "STATE":
                    request = stateBuilder();
                    break;

                case "QUIT":
                    request = quitBuilder();
                    break;

                default:
                    System.out.println("Unknown command.");
                    continue;
            }

            sendRequest(request);
            String response = receiveResponse();
            System.out.println("[client.app] Response: " + response);

            if ("QUIT".equals(cmd)) break;
        }

        socket.close();
        System.out.println("[client.app] Socket closed. Goodbye.");
    }

    // --- Action/Event Builders (enum-based) ---
    public String joinBuilder(String playerName) {
        return Protocol.buildRequest(RequestType.JOIN, playerName);
    }

    public String moveBuilder(int dx, int dy) {
        return Protocol.buildRequest(
                RequestType.MOVE,
                String.valueOf(playerId),
                String.valueOf(dx),
                String.valueOf(dy)
        );
    }

    public String pickupBuilder(int lootId) {
        return Protocol.buildRequest(
                RequestType.PICKUP,
                String.valueOf(playerId),
                String.valueOf(lootId)
        );
    }

    public String stateBuilder() {
        return Protocol.buildRequest(RequestType.STATE);
    }

    public String quitBuilder() {
        return Protocol.buildRequest(RequestType.QUIT, String.valueOf(playerId));
    }

    // --- UDP ---
    public void sendRequest(String request) throws IOException {
        byte[] bytes = Protocol.toBytes(request);
        DatagramPacket pkt = new DatagramPacket(bytes, bytes.length, routerAddress, ROUTER_PORT);

        System.out.println("\n[client.app] TX (string): " + request);
        Protocol.printBytes("[client.app] TX", bytes);

        socket.send(pkt);
    }

    public String receiveResponse() throws IOException {
        byte[] buf = new byte[2048];
        DatagramPacket pkt = new DatagramPacket(buf, buf.length);
        socket.receive(pkt);

        String resp = Protocol.decode(pkt);

        System.out.println("[client.app] RX (string): " + resp);
        System.out.println("[client.app] RX (bytes) : " + Protocol.toHex(pkt.getData(), pkt.getOffset(), pkt.getLength()));

        return resp;
    }
}