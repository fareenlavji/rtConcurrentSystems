/**
* The technician who specializes in propulsions and has an infinite supply of said components.
*/
public class PropulsionTechnician extends Technician {
    public PropulsionTechnician(AssemblyMonitor monitor) {
        super(monitor, Component.PROPULSION);
    }
}
