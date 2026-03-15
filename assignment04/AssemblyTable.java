/**
 * This class is for the assembly table in this Autonomous Drone Assembly Line.
 * The table serves as a common place where components are placed by the agent and taken by the technician.
 * The table accepts components from the Agent and notifies all technicians that they are available.
 * The table determines when each technician is allowed to take the components, based on what their missing components are.
 * The table lets the right Technician assemble a drone, then notifies the Agent that the table is empty.
 * The table will allow components to be placed and taken until 20 drones are assembled.
 *
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
public class AssemblyTable {
    private final int SIZE = 2; //Capacity of table
    private static final int MAX_DRONES = 20;
    private Components[] components = new Components[SIZE]; //List of components on the table
    private boolean tableFull = false; //True if there is at least 1 component on the table
    private int dronesMade = 0; //Running total of drones assembled

    // NEW --> Addition of Event Logger (Assignment04_Requirement01)
    private final EventLogger logger = EventLogger.getInstance();

    /**
     * Method used to allow an Agent to place components on the table when table is empty.
     *
     * @param components1   First component to be placed by Agent
     * @param components2   Second component to be placed by Agent
     */
    public synchronized void addComponents(Components components1, Components components2) {
        //Make agent wait until table is empty to place components
        while (tableFull) {
            logger.waitStart(Thread.currentThread().getName());

            //Exit if no more drones are required to be assembled
            if (this.dronesMade == MAX_DRONES){ return; }

            try {
                wait(); //Tells agent to wait until notified
                logger.waitEnd(Thread.currentThread().getName());
            } catch (InterruptedException e) { e.printStackTrace(); }
        }

        //Exit if no more drones are required to be assembled
        if (this.dronesMade == MAX_DRONES){ return; }

        //Components are placed on table
        components[0] = components1;
        components[1] = components2;
        logger.logEventKV(Thread.currentThread().getName(), "PLACED_COMPONENTS",
                "components", "[" + components1 + "," + components2 + "]",
                "drones", String.valueOf(dronesMade));


        // Random delay to simulate real scenario
        try {
            logger.logEvent(Thread.currentThread().getName(), "WORK_START");
            Thread.sleep((int)(Math.random() * 1000));
            logger.logEvent(Thread.currentThread().getName(), "WORK_END");
        } catch (InterruptedException e) {}

        tableFull = true;   //Table is now full
        logger.logEventKV(Thread.currentThread().getName(), "TABLE_FULL",
                "components", "[" + components1 + "," + components2 + "]",
                "drones", String.valueOf(dronesMade));

        System.out.println("[" + Thread.currentThread().getName() + "] "
                + components1.toString() + " and "
                + components2.toString() + " placed on the table.");

        // Notify all Technicians that table is full
        logger.logEvent(Thread.currentThread().getName(), "READY");
        notifyAll();
    }

    /**
     * Method used by Technicians to obtain components on table and assemble a drone
     *
     * @param components    The component the Technician has an infinite supply of (Used to determine if Technician is eligible to take the components on the table)
     */
    public synchronized void getComponents(Components components) {

        //Makes Technician wait until the table is full or until the two required components from the Agent is available
        while (!canTake(components)) {
            //If MAX_DRONES have been assembled, do not assemble another
            if (this.dronesMade == MAX_DRONES){ return; }

            try {
                logger.waitStart(Thread.currentThread().getName());
                wait(); //Make the Technician wait until notified that new components are available
                logger.waitEnd(Thread.currentThread().getName());
            } catch (InterruptedException e) { e.printStackTrace(); }
        }

        logger.logEventKV(Thread.currentThread().getName(), "PICKED_UP",
                "components", "[" + this.components[0] + "," + this.components[1] + "]",
                "drones", String.valueOf(dronesMade));


        System.out.println("[" + Thread.currentThread().getName() + "] Drone assembled.");
        System.out.println("[" + Thread.currentThread().getName() + "] Waiting for remaining components...");
        this.dronesMade++;  //Increase running total of drones assembled
        logger.logEventKV(Thread.currentThread().getName(), "DRONE_ASSEMBLED", "drones", String.valueOf(dronesMade));
        System.out.println("[Counter] Drones assembled: " + this.dronesMade);
        System.out.println("--------------------------------------------------------------");
        //Clear components and set table to empty
        this.components[0] = null;
        this.components[1] = null;
        tableFull = false;
        logger.logEventKV(Thread.currentThread().getName(), "TABLE_EMPTY", "drones", String.valueOf(dronesMade));

        // Random delay to simulate real scenario
        try {
            logger.logEvent(Thread.currentThread().getName(), "WORK_START");
            Thread.sleep((int)(Math.random() * 1000));
            logger.logEvent(Thread.currentThread().getName(), "WORK_END");
        } catch (InterruptedException e) {}

        logger.logEvent(Thread.currentThread().getName(), "READY");
        notifyAll(); //Notify Technicians and Agent that components have changed
    }

    /**
     * Method used to check if the component given is one of the two components on the table.
     *
     * @param techOwns    Component from Technician (to check if Technician can accept the components on the table)
     * @return            True if component is on the table, false otherwise.
     */
    private boolean canTake(Components techOwns) {
        if (!tableFull) return false;
        if (this.components[0] == null || this.components[1] == null) return false;
        return (this.components[0] != techOwns && this.components[1] != techOwns);
    }


    /**
     * Getter method for getDronesMade.
     *
     * @return dronesMade
     */
    public int getDronesAssembled(){
        return this.dronesMade;
    }

    /**
     * Returns the maximum number of drones needed for the job to complete.
     *
     * @return The maximum number of drones for job completion.
     */
    public int getMaxDrones() { return MAX_DRONES; }

    /**
     * Method used to create new Technician threads; keeps consistency of naming conventions between threads
     *
     * @param t     Common table between Technicians and Agent
     * @param i     Component that Technician will have an infinite supply of
     * @return      Created Technician thread
     */
    private static Thread makeNewTechnician(AssemblyTable t, Components i){
        return new Thread(new Technician(t, i), "Technician-" + i.toString());
    }

    /**
     * Method used to run the program. The program creates all threads and starts them
     *
     * @param args
     */
    public static void main(String[] args) {
        // Threads for each Technician and the Agent
        Thread techFrame, techProp, techCtrl, agent;

        // Common table for all threads
        AssemblyTable assemblyTable = new AssemblyTable();

        // Start the logger (daemon flusher inside)
        EventLogger logger = EventLogger.getInstance();


        // Create file with standardized naming
        String runId = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String logFile = "assembly_log_" + runId + ".txt";
        logger.setLogFileName(logFile);
        logger.setFlushIntervalMs(250L);

        logger.start(); // background daemon flusher
        // System-wide lifecycle anchors for analyzer
        logger.logEventKV("AssemblyLine", "SYSTEM_START", "drones", "0");
        logger.logEventKV("AssemblyLine", "ASSEMBLY_TABLE_READY", "drones", "0");

        // Create threads with clear names (you already do this in makeNewTechnician)
        agent     = new Thread(new Agent(assemblyTable), "Agent");
        techFrame = makeNewTechnician(assemblyTable, Components.Frame);
        techProp  = makeNewTechnician(assemblyTable, Components.PropulsionUnit);
        techCtrl  = makeNewTechnician(assemblyTable, Components.ControlFirmware);

        // Check-in logs before starting threads
        logger.logEventKV("AssemblyLine", "FRAME_TECH_CHECKIN", "drones", "0");
        techFrame.start();
        logger.logEventKV("AssemblyLine", "PROPULSION_TECH_CHECKIN", "drones", "0");
        techProp.start();
        logger.logEventKV("AssemblyLine", "CONTROL_TECH_CHECKIN", "drones", "0");
        techCtrl.start();
        logger.logEventKV("AssemblyLine", "AGENT_CHECKIN", "drones", "0");
        agent.start();

        // Wait for completion
        try {
            agent.join();
            techFrame.join();
            techProp.join();
            techCtrl.join();
        } catch (InterruptedException e) { e.printStackTrace(); }

        // Completion summary and SYSTEM_END
        logger.logEventKV("AssemblyLine", "JOB_COMPLETED", "drones", String.valueOf(assemblyTable.getDronesAssembled()));
        logger.logEventKV("AssemblyLine", "SYSTEM_END", "drones", String.valueOf(assemblyTable.getDronesAssembled()));

        // Stop logger and flush remaining records
        logger.stop();
        System.out.println("All threads finished; system terminated.");


        // Run the analyzer AFTER logs are fully flushed (prints to console AND writes metrics.txt)
        try {
            LogAnalyzer.main(new String[]{ logFile });
            System.out.println("Metrics written to metrics.txt");
        } catch (Exception e) { e.printStackTrace(); }
    }
}
