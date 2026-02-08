/**
 * Standard set of response types.
 *
 * @version February 07, 2026
 * @author Lavji, Fareen_543
 */
public enum ResponseType {
    JOINED,
    MOVE_OK,
    PICKUP_OK,
    PICKUP_FAIL,
    STATE_DATA,
    ERROR,
    QUIT_OK;

    public String wireToken() {
        return this.name();
    }

    public static ResponseType fromToken(String token) {
        return ResponseType.valueOf(token.trim().toUpperCase());
    }
}