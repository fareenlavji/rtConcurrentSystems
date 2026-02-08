import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Router application that manages/handles client requests and server responses.
 *
 * @version February 07, 2026
 * @author Lavji, Fareen_543
 */
public class Router {

    private static final int ROUTER_PORT = 5000;
    private static final int SERVER_PORT = 6000;
    private static final String SERVER_HOST = "localhost";

    private final DatagramSocket socket;
    private final InetAddress serverAddress;

    public Router() throws Exception {
        this.socket = new DatagramSocket(ROUTER_PORT);
        this.serverAddress = InetAddress.getByName(SERVER_HOST);

        System.out.println("[router.app] Listening on UDP port " + ROUTER_PORT);
        System.out.println("[router.app] Forwarding to server " + SERVER_HOST + ":" + SERVER_PORT);
    }

    public static void main(String[] args) throws Exception {
        new Router().runForever();
    }

    public void runForever() throws IOException {
        byte[] buf = new byte[2048];

        while (true) {
            // Receive from the client
            DatagramPacket clientPkt = new DatagramPacket(buf, buf.length);
            socket.receive(clientPkt);

            InetAddress clientAddr = clientPkt.getAddress();
            int clientPort = clientPkt.getPort();

            System.out.println("\n[router.app] RX from client " + clientAddr + ":" + clientPort);
            Protocol.printPacket("[router.app] RX", clientPkt);

            // Forward identical bytes to the server
            byte[] forwardBytes = Protocol.copyExactPayload(clientPkt);
            DatagramPacket toServer = new DatagramPacket(forwardBytes, forwardBytes.length, serverAddress, SERVER_PORT);
            socket.send(toServer);

            System.out.println("[router.app] FWD -> server " + serverAddress + ":" + SERVER_PORT);

            // Receive from server
            DatagramPacket serverPkt = new DatagramPacket(buf, buf.length);
            socket.receive(serverPkt);

            System.out.println("[router.app] RX from server " + serverPkt.getAddress() + ":" + serverPkt.getPort());
            Protocol.printPacket("[router.app] RX", serverPkt);

            // Forward identical bytes back to the originating client
            byte[] responseBytes = Protocol.copyExactPayload(serverPkt);
            DatagramPacket toClient = new DatagramPacket(responseBytes, responseBytes.length, clientAddr, clientPort);
            socket.send(toClient);

            System.out.println("[router.app] FWD -> client " + clientAddr + ":" + clientPort);
        }
    }
}