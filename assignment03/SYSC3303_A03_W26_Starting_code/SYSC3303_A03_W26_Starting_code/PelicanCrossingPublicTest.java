package SYSC3303_A03_W26_Starting_code.SYSC3303_A03_W26_Starting_code;
/**
 * ============================================================
 * Pelican Crossing State Machine - Test Harness - Students
 * ============================================================

 * @author Dr. Rami Sabouni,
 * Systems and Computer Engineering,
 * Carleton University
 * @version 1.0, February 8, 2026
 */

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PelicanCrossingPublicTest {

    @Test
    void init_entersOperationalCarsGreenNoPed() {
        PelicanCrossing fsm = new PelicanCrossing();
        assertEquals(PelicanCrossing.State.OPERATIONAL_CARS_GREEN_NO_PED, fsm.getState());
        assertEquals(PelicanCrossing.CarSignal.GREEN, fsm.getCarSignal());
        assertEquals(PelicanCrossing.PedSignal.DONT_WALK_ON, fsm.getPedSignal());
    }

    @Test
    void pedsWaiting_beforeMinGreen_remembersWaitingButStaysInCarsGreen() {
        PelicanCrossing fsm = new PelicanCrossing();

        fsm.dispatch(PelicanCrossing.Event.PEDS_WAITING);

        assertEquals(PelicanCrossing.State.OPERATIONAL_CARS_GREEN_PED_WAIT, fsm.getState());
        assertEquals(PelicanCrossing.CarSignal.GREEN, fsm.getCarSignal());
        assertEquals(PelicanCrossing.PedSignal.DONT_WALK_ON, fsm.getPedSignal());
    }


    @Test
    void pedWaiting_duringMinGreen_thenAutoYellow_whenMinGreenEnds() {
        PelicanCrossing fsm = new PelicanCrossing();
        fsm.dispatch(PelicanCrossing.Event.PEDS_WAITING);
        assertEquals(PelicanCrossing.State.OPERATIONAL_CARS_GREEN_PED_WAIT, fsm.getState());

        // Finish remaining green ticks
        fsm.tick(PelicanCrossing.GREEN_TOUT);

        // Student solution goes straight to yellow when green min ends in a ped-wait path
        assertEquals(PelicanCrossing.State.OPERATIONAL_CARS_YELLOW, fsm.getState());
        assertEquals(PelicanCrossing.CarSignal.YELLOW, fsm.getCarSignal());
    }

    @Test
    void walkTimesOut_thenFlash() {
        PelicanCrossing fsm = new PelicanCrossing();
        fsm.tick(PelicanCrossing.GREEN_TOUT);
        fsm.dispatch(PelicanCrossing.Event.PEDS_WAITING);
        fsm.tick(PelicanCrossing.YELLOW_TOUT);

        fsm.tick(PelicanCrossing.WALK_TOUT);

        assertEquals(PelicanCrossing.State.OPERATIONAL_PEDS_FLASH, fsm.getState());
        assertEquals(PelicanCrossing.CarSignal.RED, fsm.getCarSignal());
        assertTrue(
                fsm.getPedSignal() == PelicanCrossing.PedSignal.DONT_WALK_ON ||
                        fsm.getPedSignal() == PelicanCrossing.PedSignal.DONT_WALK_OFF
        );
    }

    @Test
    void offlineMode_offFromAnyOperationalState_setsSafeAndFlashes() {
        PelicanCrossing fsm = new PelicanCrossing();

        // Move to another operational state
        fsm.dispatch(PelicanCrossing.Event.PEDS_WAITING);
        assertTrue(fsm.getState().name().startsWith("OPERATIONAL_"));

        // OFF should work from here
        fsm.dispatch(PelicanCrossing.Event.OFF);

        assertTrue(fsm.getState().name().startsWith("OFFLINE_"));
        // Safe outputs in offline are flashing amber + dont-walk flashing
        assertTrue(
                fsm.getCarSignal() == PelicanCrossing.CarSignal.FLASHING_AMBER_ON ||
                        fsm.getCarSignal() == PelicanCrossing.CarSignal.FLASHING_AMBER_OFF
        );
        assertTrue(
                fsm.getPedSignal() == PelicanCrossing.PedSignal.DONT_WALK_ON ||
                        fsm.getPedSignal() == PelicanCrossing.PedSignal.DONT_WALK_OFF
        );

        PelicanCrossing.State s1 = fsm.getState();
        fsm.dispatch(PelicanCrossing.Event.Q_TIMEOUT);
        PelicanCrossing.State s2 = fsm.getState();
        assertNotEquals(s1, s2, "Offline should toggle between flash states on each tick");
    }

    @Test
    void offlineMode_onEvent_returnsToOperationalCarsGreenNoPed() {
        PelicanCrossing fsm = new PelicanCrossing();

        // Put FSM into offline mode
        fsm.dispatch(PelicanCrossing.Event.OFF);
        assertTrue(fsm.getState().name().startsWith("OFFLINE_"));

        // Dispatch ON event
        fsm.dispatch(PelicanCrossing.Event.ON);

        // Assert FSM returns to OPERATIONAL_CARS_GREEN_NO_PED
        assertEquals(PelicanCrossing.State.OPERATIONAL_CARS_GREEN_NO_PED, fsm.getState());
        assertEquals(PelicanCrossing.CarSignal.GREEN, fsm.getCarSignal());
        assertEquals(PelicanCrossing.PedSignal.DONT_WALK_ON, fsm.getPedSignal());
    }
}