import java.util.Random;

/**
 * Agent thread: randomly places two distinct components and waits for assembly completion.
 */
public class Agent implements Runnable {
    private final AssemblyMonitor monitor;
    private final Random random = new Random();

    public Agent(AssemblyMonitor monitor) {
        this.monitor = monitor;
    }

    @Override
    public void run() {
        Component[] components = Component.values();

        while (!monitor.isDone()) {
            Component comp1 = components[random.nextInt(components.length)];
            Component comp2;
            do {
                comp2 = components[random.nextInt(components.length)];
            } while (comp2 == comp1);

            monitor.placeComponents(comp1, comp2);
        }

        System.out.println(Thread.currentThread().getName() + " terminated.");
    }
}
