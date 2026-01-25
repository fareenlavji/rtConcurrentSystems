
/**
 * Abstract base for concrete Technician classes.
 * Each technician has an infinite supply of ONE component (myComponent).
 */
public abstract class Technician implements Runnable {
    protected final AssemblyMonitor monitor;
    protected final Component myComponent;

    /**
    * Setup the technician
    *
    * @param monitor The AssemblyMonitor to report and work alongside.
    * @param myComponent The allocated component the technician specializes in.
    */
    public Technician(AssemblyMonitor monitor, Component myComponent) {
        this.monitor = monitor;
        this.myComponent = myComponent;
    }

    @Override
    public void run() {
        while (monitor.waitForTurn(myComponent)) {
            // Assemble outside monitor (do NOT hold the lock while "working").
            try {
                Thread.sleep((long) (Math.random() * 500));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break; // terminate cleanly
            }

            monitor.completeAssembly();
        }

        System.out.println(Thread.currentThread().getName() + " terminated.");
    }
}
