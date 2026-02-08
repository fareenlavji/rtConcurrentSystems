/**
 * Standard set of request types.
 *
 * @version February 07, 2026
 * @author Lavji, Fareen_543
 */

public enum RequestType {
    JOIN,
    MOVE,
    PICKUP,
    STATE,
    QUIT;

    public static RequestType fromToken(String token) {
        return RequestType.valueOf(token.trim().toUpperCase());
    }

    public String wireToken() {
        return this.name();
    }
}