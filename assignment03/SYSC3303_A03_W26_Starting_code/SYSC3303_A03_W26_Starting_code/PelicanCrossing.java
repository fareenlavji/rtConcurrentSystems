package SYSC3303_A03_W26_Starting_code.SYSC3303_A03_W26_Starting_code;
/**
 * ============================================================
 * Pelican Crossing State Machine - State Pattern Solution
 * ============================================================
 * <p>
 * Pelican Crossing Controller
 * <p>
 * STUDENT STARTER CODE
 * <p>
 * You MUST NOT:
 *  - Rename this class
 *  - Rename enums or enum values
 *  - Change method signatures
 *  - Remove required fields
 * <p>
 * You MAY:
 *  - Add private helper methods
 *  - Add private classes (State Pattern)
 *  - Add private variables
 * <p>
 * The TA JUnit harness depends on the exact names below.
 *
 * @author Dr. Rami Sabouni,
 * Systems and Computer Engineering,
 * Carleton University
 * @version 1.0, February 8, 2026
 * <p>
 * IMPLEMENTATION: Object-Oriented State Pattern with Inner Classes
 * <p>
 * This implementation uses the State Pattern with private inner classes to encapsulate state-specific behaviour.
 * Each state class handles its own event dispatching and transitions, resulting in clean, maintainable code.
 * This version uses ONLY the State enum variable provided in the starter code. No redundant state objects.
 *
 * @author Lavji, Fareen XXXXXX543
 * @version 1.2, February 28, 2026
 */
public class PelicanCrossing {

    /* =========================================================
     * ENUMS — DO NOT MODIFY NAMES OR VALUES
     * ========================================================= */

    /** Events injected into the state machine */
    public enum Event {
        INIT,
        PEDS_WAITING,
        Q_TIMEOUT,
        OFF,
        ON
    }

    /** Leaf states used for grading and testing */
    public enum State {
        OPERATIONAL_CARS_GREEN_NO_PED,
        OPERATIONAL_CARS_GREEN_PED_WAIT,
        OPERATIONAL_CARS_GREEN_INT,
        OPERATIONAL_CARS_YELLOW,
        OPERATIONAL_PEDS_WALK,
        OPERATIONAL_PEDS_FLASH,

        OFFLINE_FLASH_ON,
        OFFLINE_FLASH_OFF
    }

    /** Output signal for cars */
    public enum CarSignal {
        RED,
        GREEN,
        YELLOW,
        FLASHING_AMBER_ON,
        FLASHING_AMBER_OFF
    }

    /** Output signal for pedestrians */
    public enum PedSignal {
        DONT_WALK_ON,
        DONT_WALK_OFF,
        WALK
    }

    /* =========================================================
     * TIMING CONSTANTS (ticks)
     * DO NOT RENAME — values may be changed if justified
     * ========================================================= */

    public static final int GREEN_TOUT  = 3;   // minimum green duration
    public static final int YELLOW_TOUT = 2;   // yellow duration
    public static final int WALK_TOUT   = 3;   // walk duration
    public static final int PED_FLASH_N = 6;   // number of flashing ticks

    /* =========================================================
     * REQUIRED INTERNAL STATE
     * ========================================================= */

    /** Current leaf state (used by TA tests) */
    private State state;

    /** Output signals (used by TA tests) */
    private CarSignal carSignal;
    private PedSignal pedSignal;

    // Private helper timers and flags
    private int greenTimer;        // Countdown timer for green phase
    private int yellowTimer;       // Countdown timer for the yellow phase
    private int walkTimer;         // Countdown timer for walk phase
    private int flashCounter;      // Counter for pedestrian flashing
    private boolean pedsWaiting;   // Flag: are pedestrians waiting?

    /* =========================================================
     * CONSTRUCTOR
     * ========================================================= */

    public PelicanCrossing() {
        state = State.OPERATIONAL_CARS_GREEN_NO_PED; // safety
        // Initialize to initial state
        dispatch(Event.INIT);
    }

    /* =========================================================
     * REQUIRED PUBLIC API — DO NOT CHANGE SIGNATURES
     * ========================================================= */

    /**
     * Inject an event into the state machine.
     */
    public void dispatch(Event e) {
        // Route by current state to the appropriate handler
        try {
            switch (state != null ? state : null) {
                case OPERATIONAL_CARS_GREEN_NO_PED, null:
                    dispatchOperationalCarsGreenNoPed(e);
                    break;
                case OPERATIONAL_CARS_GREEN_PED_WAIT:
                    dispatchOperationalCarsGreenPedWait(e);
                    break;
                case OPERATIONAL_CARS_GREEN_INT:
                    dispatchOperationalCarsGreenInt(e);
                    break;
                case OPERATIONAL_CARS_YELLOW:
                    dispatchOperationalCarsYellow(e);
                    break;
                case OPERATIONAL_PEDS_WALK:
                    dispatchOperationalPedsWalk(e);
                    break;
                case OPERATIONAL_PEDS_FLASH:
                    dispatchOperationalPedsFlash(e);
                    break;
                case OFFLINE_FLASH_ON:
                    dispatchOfflineFlashOn(e);
                    break;
                case OFFLINE_FLASH_OFF:
                    dispatchOfflineFlashOff(e);
                    break;
                case default:
                    break;
            }
        } catch (NullPointerException ex) { System.out.println("ERROR: Null state transition"); }
    }

    /**
     * Convenience method: advance the clock by n ticks.
     * Each tick corresponds to one Q_TIMEOUT event.
     */
    public void tick(int n) {
        for (int i = 0; i < n; i++) {
            dispatch(Event.Q_TIMEOUT);
        }
    }

    /**
     * Return the current leaf state.
     * Used directly by the TA JUnit harness.
     */
    public State getState() { return state; }

    /**
     * Return the current car signal.
     */
    public CarSignal getCarSignal() { return carSignal; }

    /**
     * Return the current pedestrian signal.
     */
    public PedSignal getPedSignal() { return pedSignal; }

    /* =========================================================
     * PRIVATE DISPATCH HANDLERS (One per state)
     * ========================================================= */
    /**
     * Handle events in the OPERATIONAL_CARS_GREEN_NO_PED state.
     * Initial state: vehicles have green light, no pedestrians waiting.
     */
    private void dispatchOperationalCarsGreenNoPed(Event e) {
        switch (e) {
            case INIT:
                state = State.OPERATIONAL_CARS_GREEN_NO_PED;
                carSignal = CarSignal.GREEN;
                pedSignal = PedSignal.DONT_WALK_ON;
                greenTimer = GREEN_TOUT;
                pedsWaiting = false;
                break;

            case PEDS_WAITING:
                // Pedestrian button pressed during green
                state = State.OPERATIONAL_CARS_GREEN_PED_WAIT;
                pedsWaiting = true;
                // Signals stay the same (still GREEN, DONT_WALK)
                break;

            case Q_TIMEOUT:
                greenTimer--;
                if (greenTimer <= 0) {
                    // Min green reached, transition to interruptible green
                    state = State.OPERATIONAL_CARS_GREEN_INT;
                    // Signal stays GREEN, but now interruptible by pedestrians
                }
                break;

            case OFF:
                // Enter offline mode from any state
                state = State.OFFLINE_FLASH_ON;
                carSignal = CarSignal.FLASHING_AMBER_ON;
                pedSignal = PedSignal.DONT_WALK_ON;
                break;

            case ON:
                // Already operational, ignore
                break;

            default:
                break;
        }
    }

    /**
     * Handle events in the OPERATIONAL_CARS_GREEN_PED_WAIT state.
     * Vehicles have green, but pedestrians are waiting.
     */
    private void dispatchOperationalCarsGreenPedWait(Event e) {
        switch (e) {
            case Q_TIMEOUT:
                greenTimer--;
                if (greenTimer <= 0) {
                    // Min green ended and pedestrians waiting: transition to yellow
                    state = State.OPERATIONAL_CARS_YELLOW;
                    carSignal = CarSignal.YELLOW;
                    yellowTimer = YELLOW_TOUT;
                }
                break;

            case OFF:
                // Enter offline mode
                state = State.OFFLINE_FLASH_ON;
                carSignal = CarSignal.FLASHING_AMBER_ON;
                pedSignal = PedSignal.DONT_WALK_ON;
                break;

            case ON:
                // Already operational, ignore
                break;

            default:
                break;
        }
    }

    /**
     * Handle events in the OPERATIONAL_CARS_GREEN_INT state.
     * Interruptible green: vehicles green; can be interrupted by pedestrians.
     */
    private void dispatchOperationalCarsGreenInt(Event e) {
        switch (e) {
            case PEDS_WAITING:
                // Pedestrian arrived during interruptible green: go to yellow
                state = State.OPERATIONAL_CARS_YELLOW;
                carSignal = CarSignal.YELLOW;
                yellowTimer = YELLOW_TOUT;
                break;

            case Q_TIMEOUT:
                // Stay in interruptible green (wait for pedestrian)
                // Signal stays GREEN
                break;

            case OFF:
                // Enter offline mode
                state = State.OFFLINE_FLASH_ON;
                carSignal = CarSignal.FLASHING_AMBER_ON;
                pedSignal = PedSignal.DONT_WALK_ON;
                break;

            case ON:
                // Already operational, ignore
                break;

            default:
                break;
        }
    }

    /**
     * Handle events in the OPERATIONAL_CARS_YELLOW state.
     * Yellow transition between car and pedestrian phases.
     */
    private void dispatchOperationalCarsYellow(Event e) {
        switch (e) {
            case Q_TIMEOUT:
                yellowTimer--;
                if (yellowTimer <= 0) {
                    // Yellow timeout: transition to pedestrian walk phase
                    state = State.OPERATIONAL_PEDS_WALK;
                    carSignal = CarSignal.RED;
                    pedSignal = PedSignal.WALK;
                    walkTimer = WALK_TOUT;
                    pedsWaiting = false; // Reset flag
                }
                break;

            case OFF:
                // Enter offline mode
                state = State.OFFLINE_FLASH_ON;
                carSignal = CarSignal.FLASHING_AMBER_ON;
                pedSignal = PedSignal.DONT_WALK_ON;
                break;

            case ON:
                // Already operational, ignore
                break;

            default:
                break;
        }
    }

    /**
     * Handle events in the OPERATIONAL_PEDS_WALK state.
     * Pedestrians have WALK signal, vehicles are RED.
     */
    private void dispatchOperationalPedsWalk(Event e) {
        switch (e) {
            case Q_TIMEOUT:
                walkTimer--;
                if (walkTimer <= 0) {
                    // Walk phase timeout: transition to pedestrian flashing
                    state = State.OPERATIONAL_PEDS_FLASH;
                    pedSignal = PedSignal.DONT_WALK_ON;
                    flashCounter = PED_FLASH_N;
                    // Car signal stays RED
                }
                break;

            case OFF:
                // Enter offline mode
                state = State.OFFLINE_FLASH_ON;
                carSignal = CarSignal.FLASHING_AMBER_ON;
                pedSignal = PedSignal.DONT_WALK_ON;
                break;

            case ON:
                // Already operational, ignore
                break;

            default:
                break;
        }
    }

    /**
     * Handle events in the OPERATIONAL_PEDS_FLASH state.
     * Pedestrians see flashing "Don't Walk", then cycle back to vehicles.
     */
    private void dispatchOperationalPedsFlash(Event e) {
        switch (e) {
            case Q_TIMEOUT:
                flashCounter--;
                if (flashCounter <= 0) {
                    // Flash phase complete: back to vehicle green phase
                    state = State.OPERATIONAL_CARS_GREEN_NO_PED;
                    carSignal = CarSignal.GREEN;
                    pedSignal = PedSignal.DONT_WALK_ON;
                    greenTimer = GREEN_TOUT;
                    pedsWaiting = false;
                } else {
                    // Toggle pedestrian signal: even/odd toggle between ON/OFF
                    if (flashCounter % 2 == 0) {
                        pedSignal = PedSignal.DONT_WALK_ON;
                    } else {
                        pedSignal = PedSignal.DONT_WALK_OFF;
                    }
                }
                break;

            case OFF:
                // Enter offline mode
                state = State.OFFLINE_FLASH_ON;
                carSignal = CarSignal.FLASHING_AMBER_ON;
                pedSignal = PedSignal.DONT_WALK_ON;
                break;

            case ON:
                // Already operational, ignore
                break;

            default:
                break;
        }
    }

    /**
     * Handle events in the OFFLINE_FLASH_ON state.
     * System offline: flashing amber lights, safe pedestrian signal.
     */
    private void dispatchOfflineFlashOn(Event e) {
        switch (e) {
            case Q_TIMEOUT:
                // Toggle to flash OFF on the next tick
                state = State.OFFLINE_FLASH_OFF;
                carSignal = CarSignal.FLASHING_AMBER_OFF;
                pedSignal = PedSignal.DONT_WALK_OFF;
                break;

            case ON:
                // Return to operational mode
                state = State.OPERATIONAL_CARS_GREEN_NO_PED;
                carSignal = CarSignal.GREEN;
                pedSignal = PedSignal.DONT_WALK_ON;
                greenTimer = GREEN_TOUT;
                pedsWaiting = false;
                break;

            case OFF:
                // Already offline, ignore
                break;

            default:
                break;
        }
    }

    /**
     * Handle events in the OFFLINE_FLASH_OFF state.
     * System offline: flashing amber lights (off phase), safe pedestrian signal.
     */
    private void dispatchOfflineFlashOff(Event e) {
        switch (e) {
            case Q_TIMEOUT:
                // Toggle back to flash ON the next tick
                state = State.OFFLINE_FLASH_ON;
                carSignal = CarSignal.FLASHING_AMBER_ON;
                pedSignal = PedSignal.DONT_WALK_ON;
                break;

            case ON:
                // Return to operational mode
                state = State.OPERATIONAL_CARS_GREEN_NO_PED;
                carSignal = CarSignal.GREEN;
                pedSignal = PedSignal.DONT_WALK_ON;
                greenTimer = GREEN_TOUT;
                pedsWaiting = false;
                break;

            case OFF:
                // Already offline, ignore
                break;

            default:
                break;
        }
    }
}