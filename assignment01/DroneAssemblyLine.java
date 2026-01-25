/**
 * Driver program: runs the system until 20 drones are assembled.
 */
public class DroneAssemblyLine {
    public static final int MAX_DRONES = 20;

    public static void main(String[] args) {
        System.out.println("=== Autonomous Drone Assembly Line (Cigarette Smokers Variant) ===");
        System.out.println("Max drones: " + MAX_DRONES);
        System.out.println();

        AssemblyMonitor monitor = new AssemblyMonitor(MAX_DRONES);

        Thread agentThread = new Thread(new Agent(monitor), "Agent");

        Thread frameThread = new Thread(new FrameTechnician(monitor), "FrameTechnician");
        Thread propulsionThread = new Thread(new PropulsionTechnician(monitor), "PropulsionTechnician");
        Thread firmwareThread = new Thread(new ControlFirmwareTechnician(monitor), "ControlFirmwareTechnician");

        // Start technicians first (optional). Either order is fine because state-based guards prevent lost signals.
        frameThread.start();
        propulsionThread.start();
        firmwareThread.start();
        agentThread.start();

        // Wait for threads to finish
        try {
            agentThread.join();
            frameThread.join();
            propulsionThread.join();
            firmwareThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println();
        System.out.println("=== All " + monitor.getAssembledCount() + " drones assembled. System terminated. ===");
    }
}
