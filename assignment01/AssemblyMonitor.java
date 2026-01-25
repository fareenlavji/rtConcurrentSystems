/**
 * Java monitor synchronizing Agent and Technicians for Autonomous Drone Assembly Line.
 */
public class AssemblyMonitor {

	private Component c1, c2, missing;
    private boolean tableOccupied;
    private int assembledCount;
    private final int maxDrones;

	/**
	* Setup the Assembly Monitor.
	* 
	* @param maxDrones The maximum number of drones to be assembled before termination.
	*/
    public AssemblyMonitor(int maxDrones) {
        if (maxDrones <= 0) {
            throw new IllegalArgumentException("maxDrones must be > 0");
        }
        this.maxDrones = maxDrones;
        this.tableOccupied = false;
        this.assembledCount = 0;
    }

    /**
     * Agent places 2 random and distinct components on the table, notifies technicians,
     * then waits until the table is cleared (assembly completed) or quota reached.
	 * 
	 * @param comp1 The first random component placed on the belt by the agent.
	 * @param comp2 The second random, but distinct component, placed on the belt by the agent.
     */
    public synchronized void placeComponents(Component comp1, Component comp2) {
        // Wait until table is free (unless we are done).
        while (!isDone() && tableOccupied) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return; // clean exit if interrupted
            }
        }
        if (isDone()) return;

        // Defensive validation to maintain invariants.
        if (comp1 == null || comp2 == null || comp1 == comp2) {
            throw new IllegalArgumentException("Agent must place two distinct non-null components.");
        }

        // Place components and compute missing.
        this.c1 = comp1;
        this.c2 = comp2;
        this.missing = computeMissing(comp1, comp2);
        this.tableOccupied = true;

        System.out.printf("%s placed: %s + %s (missing %s)%n",
                Thread.currentThread().getName(), comp1, comp2, missing);

        // Wake all technicians (single wait-set => notifyAll).
        notifyAll();

        // Wait until a technician clears the table, or system completes.
        while (!isDone() && tableOccupied) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    /**
     * Technician waits until:
     * - system not done, AND
     * - table occupied, AND
     * - the missing component matches their owned component.
     *
	 * @param myComponent The missing component to be matched to the Technicians.
	 *
     * @return true if technician should proceed with assembly, false if done (terminate).
     */
    public synchronized boolean waitForTurn(Component myComponent) {
        while (!isDone() && (!tableOccupied || missing != myComponent)) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false; // terminate cleanly if interrupted
            }
        }
        return !isDone();
    }

    /**
     * Technician calls this after assembling (outside the monitor).
     * Clears the table, increments the count, and wakes waiting threads.
     */
    public synchronized void completeAssembly() {
        // sanity check
        if (isDone()) {
            // Still wake others so they can exit.
            notifyAll();
            return;
        }

        assembledCount++;
        tableOccupied = false;

        System.out.printf("%s completed drone #%d%n",
                Thread.currentThread().getName(), assembledCount);

        // Wake agent + other technicians.
        notifyAll();
    }

	/**
	* Checks to see if drone assembly is complete.
	*
	* @return true if complete, false otherwise.
	*/
    public synchronized boolean isDone() {
        return assembledCount >= maxDrones;
    }

	/**
	* Returns the total number of assembled drones so far.
	*
	* @return The number of assembled drones.
	*/
    public synchronized int getAssembledCount() {
        return assembledCount;
    }

	/**
	* Relays the missing component.
	*
	* @return the missing component.
	*/
    public synchronized Component getMissing() {
        return missing;
    }

    /**
	* Computes the third component not in (comp1, comp2).
	*
	*@param comp1 the first component placed on the assembly line by the agent.
	*@param comp2 the second component placed on the assembly line by the agent.
	*
	*@return the missing third component.
	*/
    private Component computeMissing(Component comp1, Component comp2) {
        // (comp1, comp2) are guaranteed distinct and non-null by caller validation.
        if ((comp1 == Component.FRAME && comp2 == Component.PROPULSION) ||
            (comp1 == Component.PROPULSION && comp2 == Component.FRAME)) {
            return Component.FIRMWARE;
        }

        if ((comp1 == Component.FRAME && comp2 == Component.FIRMWARE) ||
            (comp1 == Component.FIRMWARE && comp2 == Component.FRAME)) {
            return Component.PROPULSION;
        }

        // Remaining valid pair: PROPULSION + FIRMWARE
        return Component.FRAME;
    }
}
