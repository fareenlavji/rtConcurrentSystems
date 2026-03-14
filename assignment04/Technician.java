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
 */

public class Technician implements Runnable {
    private AssemblyTable assemblyTable;                //The common table between Agent and Technicians
    private Components components;      //The only component each instance of Technician has an infinite supply of (this component is different between all three Technicians)

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
     * Method used for each Technician thread when ran
     */
    public void run(){
        System.out.println("[" + Thread.currentThread().getName() + "] Waiting for remaining components...");
        while (this.assemblyTable.getDronesAssembled() < 20){   //Will loop until 20 drones have been assembled
            this.assemblyTable.getComponents(this.components); //Attempts to obtain the missing components for the Technician (if obtained, drone is assembled)
            // Sleep for up to 5 seconds before performing work
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