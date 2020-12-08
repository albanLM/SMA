// Simulation in which the agents will interact with each other.
// Statistics on its progress are displayed at the end.
public class Simulation {
    // Agents that will take part in the simulation.
    private final MyAgent[] agents;
    // Products that will be exchanged, produced and consumed during the simulation.
    private final Product[] products;

    public Simulation(MyAgent[] agents, Product[] products) {
        this.agents = agents;
        this.products = products;
    }

    public void simulate() {

    }

    public String simulationResults() {
        return "";
    }
}
