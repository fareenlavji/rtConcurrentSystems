import java.util.concurrent.ThreadLocalRandom; // Thread - safe
/**
 * Components enums
 *
 * @author Dr. Rami Sabouni,
 * Systems and Computer Engineering,
 * Carleton University
 * @version 1.0, January 07th, 2025
 * @version 2.0, January 10th, 2026
 *
 * @author Lavji, F
 * @version 2.1, March 14, 2026
 */
public enum Components {
    Frame,
    PropulsionUnit,
    ControlFirmware;

    /**
     * Pick a random value of the Components enum.
     *
     * @return a random Component.
     */
    public static Components getRandomComponent() {
        // Switched out with thread-safe Randomizer
        int i = ThreadLocalRandom.current().nextInt(values().length);
        return values()[i];
    }
}
