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
 */
public class AssemblyTable {
    private final int SIZE = 2;                                 //Capacity of table
    private Components[] components = new Components[SIZE];     //List of components on the table
    private boolean tableFull = false;                          //True if both components have been placed on the table
    private int dronesMade = 0;                                  //Running total of drones assembled

    /**
     * Method used to allow an Agent to place components on the table when table is empty
     * @param components1   First component to be placed by Agent
     * @param components2   Second component to be placed by Agent
     */
    public synchronized void addComponents(Components components1, Components components2) {
        while (tableFull) { //Makes agent wait until table is empty to place components
            if (this.dronesMade == 20){ //Will exit if no more drones are required to be assembled
                return;
            }
            try {
                wait(); //Tells agent to wait until notified
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (this.dronesMade == 20){ //Will exit if no more drones are required to be assembled
            return;
        }

        //Components are placed on table
        components[0] = components1;
        components[1] = components2;

        // Random delay to simulate real scenario
        try {
            Thread.sleep((int)(Math.random() * 1000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        tableFull = true;   //Table is now full
        System.out.println("[" + Thread.currentThread().getName() + "] " + components1.toString() + " and " + components2.toString() + " placed on the table.");
        notifyAll();    //Notify all Technicians that table is full
    }

    /**
     * Method used by Technicians to obtain components on table and assemble a drone
     *
     * @param components    The component the Technician has an infinite supply of (Used to determine if Technician is eligible to take the components on the table)
     */
    public synchronized void getComponents(Components components)
    {
        while (!tableFull || componentsContains(components)) { //Makes Technician wait until the table is full and until the two required components from the Agent is available
            if (this.dronesMade == 20){ //If 20 drones have been assembled, do not assemble another
                return;
            }
            try {
                wait(); //Make the Technician wait until notified that new components are available
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("[" + Thread.currentThread().getName() + "] Drone assembled.");
        System.out.println("[" + Thread.currentThread().getName() + "] Waiting for remaining components...");
        this.dronesMade++;  //Increase running total of drones assembled
        System.out.println("[Counter] Drones assembled: " + this.dronesMade);
        System.out.println("--------------------------------------------------------------");
        //Clear components and set table to empty
        this.components[0] = null;
        this.components[1] = null;
        tableFull = false;

        // Random delay to simulate real scenario
        try {
            Thread.sleep((int)(Math.random() * 1000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        notifyAll();    //Notify Technicians and Agent that components have changed
    }

    /**
     * Method used to check if the component given is one of the two components on the table
     *
     * @param components    Component from Technician (used to check if Technician can accept the components on the table)
     * @return True if component is on the table, false otherwise
     */
    private boolean componentsContains (Components components){
        //If there are no components on the table, or one of the components on the table is the same as the component given from the Technician, return True; false otherwise
        return (this.components[0] == null || this.components[1] == null || (this.components[0] == components || this.components[1] == components));
    }

    /**
     * Getter method for getDronesMade.
     *
     * @return dronesMade
     */
    public synchronized int getDronesAssembled(){
        return this.dronesMade;
    }

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
    public static void main (String[] args){

        Thread TechnicianFrame, TechnicianPropulsion, TechnicianControl, agent;  //Threads for each Technician and the Agent
        AssemblyTable assemblyTable;                                            //Table

        assemblyTable = new AssemblyTable();                                                //Common Table for all Technicians and Agent
        agent = new Thread(new Agent(assemblyTable), "Agent");                //Agent thread created
        TechnicianFrame = makeNewTechnician(assemblyTable, Components.Frame);             //Frame Technician created
        TechnicianPropulsion = makeNewTechnician(assemblyTable, Components.PropulsionUnit);             //Propulsion Technician created
        TechnicianControl = makeNewTechnician(assemblyTable, Components.ControlFirmware);             //Control Firmware Technician created

        //Start all Technician and Agent threads
        TechnicianFrame.start();
        TechnicianPropulsion.start();
        TechnicianControl.start();
        agent.start();
    }
}