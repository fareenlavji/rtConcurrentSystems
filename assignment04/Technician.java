/**
 * This class is for the Technicians in this drone making system.
 * The Technician has an infinite supply of one of the three components.
 * The Technician will wait at the table until the other two components are placed, and will then make a drone and assemble it.
 * The Technician will repeat this procedure until 20 drones are assembled in total between all Technicians
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

public class Technician implements Runnable {
    private AssemblyTable assemblyTable; //The common table between Agent and Technicians
    private Components components; //The only component each instance of Technician has an infinite supply of (this component is different between all three Technicians)

    // NEW --> Addition of Event Logger (Assignment04_Requirement01)
    private final EventLogger logger = EventLogger.getInstance();

    /**
     * Constructor for Technician
     *
     * @param t     //The common table between Agent and Technicians
     * @param i     //The component this Technician has an infinite supply of
     */
    public Technician(AssemblyTable t, Components i){
        this.assemblyTable = t;
        this.components = i;
    }

    /**
     * Method used for each Technician thread when ran.
     */
    public void run(){
        String name = Thread.currentThread().getName();
        logger.logEvent(name, "THREAD_START", "Technician with infinite " + components);

        System.out.println("[" + Thread.currentThread().getName() + "] Waiting for remaining components...");

        // Loop until MAX_DRONES have been assembled
        while (this.assemblyTable.getDronesAssembled() != this.assemblyTable.getMaxDrones()) {

            long startAttempt = System.currentTimeMillis();

            //Attempts to obtain the missing components for the Technician (if obtained, drone is assembled)
            logger.logEvent(name, "RETRIEVING_COMPONENT");
            this.assemblyTable.getComponents(this.components);

            // Sleep for between 0 and 5 seconds before calculating n!
            try {
                logger.logEvent(name, "WORK_START");
                Thread.sleep((int)(Math.random() * 5000));
                logger.logEvent(name, "WORK_END");
            } catch (InterruptedException e) {}

            // NEW --> Metric Analysis (Assignment04_Requirement03)
            long endAttempt = System.currentTimeMillis();
            long responseTime = endAttempt - startAttempt;
            logger.logEventKV(name, "RESPONSE_TIME", "duration", String.valueOf(responseTime));
        }

        //All drones have been assembled
        logger.logEvent(name, "THREAD_END",
                String.format("%d drones assembled, ending...", this.assemblyTable.getMaxDrones()));
        System.out.println(String.format("[%s] %d drones assembled, ending...",
                Thread.currentThread().getName(), this.assemblyTable.getMaxDrones()));
    }
}
