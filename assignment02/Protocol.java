import java.net.DatagramPacket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Helper class that processes the message packet protocols.
 *
 * @version February 07, 2026
 * @author Lavji, Fareen_543
 */
public final class Protocol {

    private static final Charset UTF8 = StandardCharsets.UTF_8;

    private Protocol() { }

    // --- Serializing / Deserializing String <-> bytes ---
    public static byte[] toBytes(String s) {
        return s.getBytes(UTF8);
    }

    public static String fromBytes(byte[] b, int off, int len) {
        return new String(b, off, len, UTF8);
    }

    public static String decode(DatagramPacket pkt) {
        return fromBytes(pkt.getData(), pkt.getOffset(), pkt.getLength());
    }

    // --- Parsing (colon-delimited) ---
    public static String[] split(String msg) {
        return msg.split(":");
    }

    /**
     * Parses request type from colon‑delimited message.
     *
     * @param msg The message to parse.
     */
    public static RequestType parseRequestType(String msg) {
        String[] parts = split(msg.trim());
        if (parts.length == 0 || parts[0].trim().isEmpty()) {
            throw new IllegalArgumentException("BAD_FORMAT");
        }
        return RequestType.fromToken(parts[0]);
    }

    /**
     * Parses the response type from the message string.
     *
     * @param msg The message to parse.
     */
    public static ResponseType parseResponseType(String msg) {
        String[] parts = split(msg.trim());
        if (parts.length == 0 || parts[0].trim().isEmpty()) {
            throw new IllegalArgumentException("BAD_FORMAT");
        }
        return ResponseType.fromToken(parts[0]);
    }

    // --- Builders (enums internally; strings over UDP) ---
    public static String buildRequest(RequestType type, String... fields) {
        StringBuilder sb = new StringBuilder(type.wireToken());
        for (String f : fields) sb.append(":").append(f);
        return sb.toString();
    }

    public static String buildResponse(ResponseType type, String... fields) {
        StringBuilder sb = new StringBuilder(type.wireToken());
        for (String f : fields) sb.append(":").append(f);
        return sb.toString();
    }

    // --- Router-safe forwarding (identical bytes) ---

    /**
     * Copies the payload as identical data.
     *
     * @param pkt The packet to copy.
     * @return The copied payload.
     */
    public static byte[] copyExactPayload(DatagramPacket pkt) {
        byte[] out = new byte[pkt.getLength()];
        System.arraycopy(pkt.getData(), pkt.getOffset(), out, 0, pkt.getLength());
        return out;
    }

    // --- DEBUG PRINTING (HEX DUMP) FOR AUDIT LOGGING --- //

    /**
     * Converts byte range to space‑separated hex string.
     *
     * @param b Byte array to convert.
     * @param off Offset to compensate for.
     * @param len Length of the String to parse/ generate.
     */
    public static String toHex(byte[] b, int off, int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = off; i < off + len; i++) {
            sb.append(String.format("%02X ", b[i]));
        }
        return sb.toString().trim();
    }

    /**
     * Prints packet contents as string and hex dump.
     *
     * @param tag Tag to prepend.
     * @param pkt Packet to print.
     */
    public static void printPacket(String tag, DatagramPacket pkt) {
        System.out.println(tag + " (string): " + decode(pkt));
        System.out.println(tag + " (bytes) : " + toHex(pkt.getData(), pkt.getOffset(), pkt.getLength()));
    }

    /**
     * Prints hex dump as bytes.
     *
     * @param tag Tag to prepend.
     * @param bytes Bytes to print.
     */
    public static void printBytes(String tag, byte[] bytes) {
        System.out.println(tag + " (bytes) : " + toHex(bytes, 0, bytes.length));
    }
}