/**
 * Components enums
 *
 * @author Dr. Rami Sabouni,
 * Systems and Computer Engineering,
 * Carleton University
 * @version 1.0, January 07th, 2025
 * @version 2.0, January 10th, 2026
 */

import java.util.Random;

public enum Components {
    Frame,
    PropulsionUnit,
    ControlFirmware;

    private static final Random random = new Random();

    /**
     * Pick a random value of the Components enum.
     * @return a random Component.
     */
    public static Components getRandomComponent() {
        return values()[random.nextInt(values().length)];
    }
}