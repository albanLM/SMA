import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;

// Simulation in which the agents will interact with each other.
// Statistics on its progress are displayed at the end.
public class Simulation extends Agent {
    // Agents that will take part in the simulation.
    private final Agent[] agents;
    // Products that will be exchanged, produced and consumed during the simulation.
    private final String[] products;
    // TODO: Add a timer to know if the simulation is done or not

    private final SimulationSettings settings;

    @Override
    protected void setup() {
        super.setup();

        addBehaviour(new WakerBehaviour(this, settings.simulationDuration * 1000 /* milliseconds to seconds conversion */) {
            @Override
            protected void onWake() {
                super.onWake();
                endSimulation();
            }
        });
    }

    private void endSimulation() {
        // TODO: Destroy all the agents
        // TODO: Display the results
        // TODO: Destroy self
    }

    public Simulation(SimulationSettings settings) {
        this.settings = settings;
        this.agents = null; // TODO: Create the agents
        this.products = settings.productNames;
    }

    public void simulate() {
        // Add the agents to the container
    }

    // Indicate if the simulation has finished to run
    public boolean isFinished() {
        return false;
    }

    public String simulationResults() {
        return "";
    }
}
