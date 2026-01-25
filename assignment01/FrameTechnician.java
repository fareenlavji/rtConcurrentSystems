/**
* Technician specializing in Frames, has an infinite supply of Frame components.
*/
public class FrameTechnician extends Technician {

    /**
    * Setup the FrameTechnician.
    *
    * @param monitor The AssemblyMonitor to report and work alongside with.
    */
    public FrameTechnician(AssemblyMonitor monitor) {
        super(monitor, Component.FRAME);
    }
}
