/**
 * This class is for the Agent in this drone assembly system.
 * The Agent selects two random components and places them on the common table.
 * The Agent will repeat this procedure until 20 drones are assembled in total
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
public class Agent implements Runnable {

    private AssemblyTable assemblyTable; //The common table between Agent and Technicians

    // NEW --> Addition of Event Logger (Assignment04_Requirement01)
    private final EventLogger logger = EventLogger.getInstance();

    /**
     * Constructor for Agent
     *
     * @param t     The common table between Agent and Technicians
     */
    public Agent(AssemblyTable t){
        this.assemblyTable = t;
    }

    /**
     * Method used when Agent thread is ran.
     */
    public void run(){
        Components components1, components2;

        String name = Thread.currentThread().getName();
        logger.logEvent(name, "THREAD_START");

        System.out.println("[" + Thread.currentThread().getName() + "] Waiting to place first components on the table...");

        // Loop until MAX_DRONES have been assembled
        while (this.assemblyTable.getDronesAssembled() != assemblyTable.getMaxDrones()) {
            long startAttempt = System.currentTimeMillis();

            // Randomly selects two different components
            components1 = Components.getRandomComponent();
            components2 = Components.getRandomComponent();

            // If components are the same, select and new second component
            while (components1 == components2){ components2 = Components.getRandomComponent(); }
            logger.logEvent(name, "COMPONENTS_SELECTED",
                    components1.toString() + ", " + components2.toString());

            //Places the two selected components on the table
            this.assemblyTable.addComponents(components1, components2);
            logger.logEvent(name, "COMPONENTS_ADDED",
                    components1.toString() + ", " + components2.toString());

            // Sleep for between 0 and 5 seconds before calculating n!
            try {
                logger.logEvent(name, "WORK_START");
                Thread.sleep((int)(Math.random() * 3000));
                logger.logEvent(name, "WORK_END");
            } catch (InterruptedException e) {}

            // NEW --> Metric Analysis (Assignment04_Requirement03)
            long endAttempt = System.currentTimeMillis();
            long responseTime = endAttempt - startAttempt;
            logger.logEventKV(name, "RESPONSE_TIME", "duration", String.valueOf(responseTime));
        }

        //All drones have been assembled
        logger.logEvent(name, "THREAD_END",
                String.format("%d drones assembled, ending...", assemblyTable.getMaxDrones()));
        System.out.println(String.format("[%s] %d drones assembled, ending...",
                Thread.currentThread().getName(), assemblyTable.getMaxDrones()));
    }
}
