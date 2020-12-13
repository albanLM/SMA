package com.sevenmoor;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;
import jade.wrapper.AgentContainer;
import jade.wrapper.StaleProxyException;

import java.util.ArrayList;
import java.util.Random;

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
        if (args != null && args.length>3){
            try {
               settings = new SimulationSettings(args);
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
            Random rand = new Random();
            float productionRate = Math.abs(rand.nextFloat()) * (3.0f - 0.5f) + 0.5f;
            float consumptionRate = Math.abs(rand.nextFloat()) * (2f - 0.1f) + 0.1f;
            float decayRate = Math.abs(rand.nextFloat()) * (0.5f - 0.1f) + 0.1f;
            long productMaxQuantity = 10 + Math.abs(rand.nextLong()) % (100 - 10 + 1);
            int supplyQuantity = 10 + Math.abs(rand.nextInt()) % (30 - 10 + 1);
            String produces = settings.productNames[rand.nextInt(settings.productNames.length)];
            String consumes = settings.productNames[rand.nextInt(settings.productNames.length)];

            Object[] args = {i, productionRate, consumptionRate, decayRate, produces, consumes, productMaxQuantity, settings.startMoney, supplyQuantity};
            try {
                agentContainer.createNewAgent("PCA_" + i, "com.sevenmoor.agents.ProducerConsumerAgent", args).start();
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

    public String simulationResults() {
        // TODO: Print stats
        return "";
    }
}
