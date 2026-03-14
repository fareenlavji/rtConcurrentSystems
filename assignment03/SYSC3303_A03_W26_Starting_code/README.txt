================================================================================
PELICAN CROSSING STATE MACHINE - README
================================================================================
PROJECT OVERVIEW
================================================================================
Project: Pelican Pedestrian Crossing Controller - Finite State Machine
Language: Java 8+
Design Pattern: Enum + Switch-Based State Machine
Implementation: PelicanCrossing.java
Test Harness: PelicanCrossingPublicTest.java

This implementation models a traffic light controller that safely manages the
intersection between vehicle traffic and pedestrian crossing using a finite
state machine with event-driven behaviour.

SOURCE FILES
================================================================================
Primary Implementation: PelicanCrossing.java - Complete FSM implementation
Required for Testing: PelicanCrossingPublicTest.java - Provided test harness

DESIGN APPROACH
================================================================================
This solution uses the Enum + Switch-Based approach (allowed alternative to
State Pattern). This design was chosen because:
    1. Fixed State Set: The crossing has exactly eight states that never change
    2. Simple Logic: Each state behaviour is straightforward (update timers/signals)
    3. Performance: Direct switch dispatch is fast and lightweight
    4. Clarity: Easy to understand the complete flow in one place
    5. Embedded Fit: Suitable for embedded systems with resource constraints
The architecture:
  • Single State variable (the provided State enum)
  • Constructor initializes automatically
  • dispatch(Event) routes via switch on current state
  • Private handler method per state processes events
  • Each handler directly updates state and signals

COMPILATION & EXECUTION
================================================================================
Compile:
  $ javac PelicanCrossing.java
  Expected: No errors or warnings
Run Manual Test:
  PelicanCrossing fsm = new PelicanCrossing();
  System.out.println(fsm.getState());       // OPERATIONAL_CARS_GREEN_NO_PED
  System.out.println(fsm.getCarSignal());   // GREEN
  System.out.println(fsm.getPedSignal());   // DONT_WALK_ON
Run Provided Test Harness:
  $ javac -cp junit-jupiter-api-5.x.x.jar \
      PelicanCrossing.java PelicanCrossingPublicTest.java
  $ java -cp junit-platform-console-standalone-1.x.x.jar \
      org.junit.platform.console.ConsoleLauncher --scan-classpath
  Expected Results:
      init_entersOperationalCarsGreenNoPed PASSED
      pedsWaiting_beforeMinGreen_remembersWaitingButStaysInCarsGreen PASSED
      pedWaiting_duringMinGreen_thenAutoYellow_whenMinGreenEnds PASSED
      walkTimesOut_thenFlash PASSED
      offlineMode_offFromAnyOperationalState_setsSafeAndFlashes PASSED

IMPLEMENTATION DETAILS
================================================================================
State Variable: Single State enum (provided in starter code)
  • Tracks current leaf state
  • Updated directly by dispatch handlers
  • Returned by getState() for test harness
Signal Variables: CarSignal and PedSignal enums
  • Updated as state enters or transitions
  • Maintained by dispatch handlers
  • Accessible via getCarSignal() and getPedSignal()
Private Timers:
  • greenTimer: Countdown for minimum green phase
  • yellowTimer: Countdown for yellow phase
  • walkTimer: Countdown for pedestrian walk phase
  • flashCounter: Counter for pedestrian flashing
Private Flags:
  • pedsWaiting: Boolean flag for pedestrian button pressed
Constructor: Calls dispatch(Event.INIT) to initialize FSM
Dispatch: Routes to handler method based on current state
Handlers: 8 private methods (one per state), each processing events for that state

STATE MACHINE BEHAVIOR
================================================================================
HIGH-LEVEL FLOW:
  Operational Mode (Normal Operation):
    ├─ Vehicles Enabled Phase
    │  ├─ OPERATIONAL_CARS_GREEN_NO_PED: Green for cars, no pedestrians waiting
    │  ├─ OPERATIONAL_CARS_GREEN_PED_WAIT: Green for cars, pedestrians waiting
    │  └─ OPERATIONAL_CARS_GREEN_INT: Interruptible green (waiting for ped)
    ├─ Yellow Transition
    │  └─ OPERATIONAL_CARS_YELLOW: 2-tick yellow transition
    └─ Pedestrians Enabled Phase
       ├─ OPERATIONAL_PEDS_WALK: 3-tick walk phase
       └─ OPERATIONAL_PEDS_FLASH: 6-tick flashing "Don't Walk"
  Offline Mode (Safety/Maintenance):
    ├─ OFFLINE_FLASH_ON: Flashing amber + flashing don't walk
    └─ OFFLINE_FLASH_OFF: Amber off + pedestrian signal off

TIMING BEHAVIOR
================================================================================
Green Phase (Minimum 3 ticks):
  • Vehicles receive GREEN signal
  • Pedestrians see DONT_WALK_ON
  • If pedestrian arrives before timeout, transition to yellow when min-green ends
  • If no pedestrian, enter interruptible green (stay green indefinitely)
Yellow Phase (2 ticks):
  • Vehicles receive YELLOW signal
  • Pedestrians see DONT_WALK_ON
Walk Phase (3 ticks):
  • Pedestrians receive WALK signal
  • Vehicles receive RED signal
Pedestrian Flashing (6 ticks):
  • Pedestrian signal alternates between DONT_WALK_ON and DONT_WALK_OFF
  • Vehicles receive RED signal
  • After flashing completes, cycle returns to vehicle green
Offline Phase:
  • Car signal alternates between FLASHING_AMBER_ON and FLASHING_AMBER_OFF
  • Pedestrian signal alternates between DONT_WALK_ON and DONT_WALK_OFF
  • Each Q_TIMEOUT event toggles the flashing state

SAFETY GUARANTEES
================================================================================
Property 1: Vehicles Always Safe
  ├─ Cars are RED outside vehicle-enabled phases
  ├─ Cars cannot be GREEN during pedestrian phases
  └─ Enforced by state machine structure
Property 2: Pedestrians Always Safe
  ├─ Pedestrians show DONT_WALK outside pedestrian-enabled phases
  ├─ Pedestrians cannot be WALK during vehicle green
  └─ Enforced by state machine structure

EVENTS
================================================================================
INIT:
  • Initialization event
  • Triggers first entry to OPERATIONAL_CARS_GREEN_NO_PED
  • Called automatically in constructor
PEDS_WAITING:
  • Pedestrian button pressed
  • Marks pedestrians as waiting
  • If during min-green, triggers yellow when minimum ends
  • If during interruptible green, immediately triggers yellow
Q_TIMEOUT:
  • Simulated clock tick (one unit of time)
  • Decrements internal timers
  • Triggers state transitions when timers expire
  • No real-time delays or threads used
OFF:
  • Enter offline mode from any operational state
  • Sets safe signals (flashing amber and flashing don't walk)
  • Enters OFFLINE_FLASH_ON state
ON:
  • Exit offline mode from any offline state
  • Returns to OPERATIONAL_CARS_GREEN_NO_PED
  • Reinitialize timers and pedestrian flag

SIGNAL OUTPUTS
================================================================================
Car Signal (5 possible values):
  • RED: Vehicles must stop
  • GREEN: Vehicles may proceed
  • YELLOW: Vehicles prepare to stop
  • FLASHING_AMBER_ON: Offline mode (amber on)
  • FLASHING_AMBER_OFF: Offline mode (amber off)
Pedestrian Signal (3 possible values):
  • WALK: Pedestrians may cross
  • DONT_WALK_ON: Steady don't walk
  • DONT_WALK_OFF: Flashing don't walk (off phase)