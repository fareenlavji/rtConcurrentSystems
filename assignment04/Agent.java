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
 */

public class Agent implements Runnable {

    private AssemblyTable assemblyTable;    //The common table between Agent and Technicians

    /**
     * Constructor for Agent
     *
     * @param t     The common table between Agent and Technicians
     */
    public Agent(AssemblyTable t){
        this.assemblyTable = t;
    }

    /**
     * Method used when Agent thread is ran
     */
    public void run(){
        Components components1, components2;
        System.out.println("[" + Thread.currentThread().getName() + "] Waiting to place first components on the table...");
        while (this.assemblyTable.getDronesAssembled() < 20){   //Will loop until 20 drones have been assembled
            //Randomly selects two different components
            components1 = Components.getRandomComponent();
            components2 = Components.getRandomComponent();
            while (components1 == components2){     //If components are the same, select and new second component
                components2 = Components.getRandomComponent();
            }
            this.assemblyTable.addComponents(components1, components2);    //Places the two selected components on the table
            // Sleep for between 0 and 5 seconds before calculating n!
            try {
                Thread.sleep((int)(Math.random() * 5000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        //All drones have been assembled
        System.out.println("[" + Thread.currentThread().getName() + "] 20 drones assembled, ending...");
    }
}