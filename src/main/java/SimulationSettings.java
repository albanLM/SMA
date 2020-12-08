// Settings to be used for the simulation.
public class SimulationSettings {
    // Number of agents to be created for the simulation.
    public final int agentCount;
    // Names of the products to be created for the simulation.
    public final String[] productNames;
    // How much money the agent will have at the start of the simulation
    public final float startMoney;
    // How much time the simulation will run.
    public final float simulationDuration;

    public SimulationSettings(int agentCount, String[] productNames, float simulationDuration, float startMoney) {
        this.agentCount = agentCount;
        this.productNames = productNames;
        this.startMoney = startMoney;
        this.simulationDuration = simulationDuration;
    }
}
