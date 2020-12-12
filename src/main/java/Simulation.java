import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;
import jade.wrapper.AgentContainer;
import jade.wrapper.StaleProxyException;

import java.util.ArrayList;

/**
 * Simulation in which the agents will interact with each other.
 * Statistics on its progress are displayed at the end.
*/
public class Simulation extends Agent {
    /** Products that will be exchanged, produced and consumed during the simulation. */
    private String[] products;
    /** Agents that will take part in the simulation. */
    private Agent[] agents;

    private SimulationSettings settings;

    @Override
    protected void setup() {
        super.setup();

        // Get arguments
        // Get the agent arguments
        Object[] args = getArguments();
        if (args != null && args.length != 0) {
            try {
               settings = (SimulationSettings) args[0];
               products = settings.productNames;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Add a behaviour to be called at the end of the simulation
        addBehaviour(new WakerBehaviour(this, settings.simulationDuration * 1000 /* milliseconds to seconds conversion */) {
            @Override
            protected void onWake() {
                super.onWake();
                endSimulation();
            }
        });

        startSimulation();
    }

    public void startSimulation() {
        AgentContainer agentContainer = getContainerController();

        for (int i = 0; i < settings.agentCount; i++) {
            // Agent arguments : id, productionRate, consumptionRate, decayRate, product, supply, productMaxQuantity, money, supplyQuantity
            // TODO: Automate the arguments creation
            Object[] args = {i, 2.0f, 0.5f, 0.1f, "banana", "banana", 50, 5.0f, 10};
            try {
                agentContainer.createNewAgent("PCA_" + i, "main.java.agents.ProducerConsumerAgent", args).start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
            System.out.println("Created agent PCA_" + i);
        }
    }

    private void endSimulation() {
        // TODO: Display the results
        // TODO: Destroy all the agents
        // TODO: Destroy self
    }

    // Indicate if the simulation has finished to run
    public boolean isFinished() {
        return false;
    }

    public String simulationResults() {
        return "";
    }
}
