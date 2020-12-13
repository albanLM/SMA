package com.sevenmoor;

import com.sevenmoor.agents.ProducerConsumerAgent;
import com.sun.xml.internal.ws.util.StringUtils;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentContainer;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;

import javax.xml.stream.events.StartDocument;
import java.util.ArrayList;
import java.util.Random;

import static jade.lang.acl.MessageTemplate.MatchPerformative;

/**
 * Simulation in which the agents will interact with each other. This class manages agent creation and destruction, depending on the parameters given to it upon creation.
 * Statistics on its progress are displayed at the end. It is itself an agent.
*/
public class Simulation extends Agent {
    /**
     * Products that will be exchanged, produced and consumed during the simulation.
     */
    private String[] products;

    /**
     * Agents that will take part in the simulation.
     */
    private Agent[] agents;

    /**
     * An array of records of the satisfaction of agents at relevant points in the simulation.
     */
    private ArrayList<Float> satisfactionRecords;

    /**
     * The settings of the simulation passed upon agent creation.
     */
    private SimulationSettings settings;

    /**
     * Override of the setup method in Agent. It ensures setting reception, the registering of the of a behaviour to schedule the end of the simulation,
     * as well as another behaviour to receive statistics.
     */
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

        satisfactionRecords = new ArrayList<Float>();

        ParallelBehaviour parallel = new ParallelBehaviour();

        // Add a behaviour to be called at the end of the simulation
        parallel.addSubBehaviour(new WakerBehaviour(this, settings.simulationDuration * 1000 /* milliseconds to seconds conversion */) {
            @Override
            protected void onWake() {
                super.onWake();
                endSimulation();
            }
        });

        startSimulation();

        parallel.addSubBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                MessageTemplate mt = MatchPerformative(ACLMessage.INFORM);
                ACLMessage msg = myAgent.receive(mt);
                if (msg != null){
                    float satisfaction = Float.parseFloat(msg.getContent());
                    satisfactionRecords.add(satisfaction);
                }
                else {
                    block();
                }
            }
        });

        addBehaviour(parallel);
    }

    /**
     * Encapsulates the steps needed to create the simulation. More specifically, it creates ProducerConsumerAgent instances in its container,
     * and give random ranged parameters for its properties, as long as a pair of products from the settings of the simulation.
     */
    public void startSimulation() {
        AgentContainer agentContainer = getContainerController();

        for (int i = 0; i < settings.agentCount; i++) {
            Random rand = new Random();
            float productionRate = Math.abs(rand.nextFloat()) * (3.0f - 0.5f) + 0.5f;
            float consumptionRate = Math.abs(rand.nextFloat()) * (2f - 0.1f) + 0.1f;
            float decayRate = Math.abs(rand.nextFloat()) * (0.001f - 0.0001f) + 0.1f;
            long productMaxQuantity = 10 + Math.abs(rand.nextLong()) % (100 - 10 + 1);
            int supplyQuantity = 10 + Math.abs(rand.nextInt()) % (30 - 10 + 1);
            String produces = settings.productNames[rand.nextInt(settings.productNames.length)];
            String consumes = settings.productNames[rand.nextInt(settings.productNames.length)];

            Object[] args = {i, productionRate, consumptionRate, decayRate, produces, consumes, productMaxQuantity, settings.startMoney, supplyQuantity,this.getAID()};
            try {
                agentContainer.createNewAgent("PCA_" + i, "com.sevenmoor.agents.ProducerConsumerAgent", args).start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
            System.out.println("Created agent PCA_" + i);
        }
    }

    /**
     * Encapsulates all the states of the end of the simulation. It first searches for agents belonging to the simulation (prefix: PCA_),
     * and then send a shutdown message to them. It proceeds to print the simulation results and destroys itself.
     */
    private void endSimulation(){
        //Shutting subordinate agents
        AMSAgentDescription[] subordinates;

        try {
            SearchConstraints c = new SearchConstraints();
            c.setMaxResults (new Long(-1));
            subordinates = AMSService.search( this, new AMSAgentDescription(), c );

            ACLMessage shutdown = new ACLMessage(ACLMessage.REQUEST);
            shutdown.setContent("shutdown");

            for (int i=0; i<subordinates.length;i++) {
                if (subordinates[i].getName().getLocalName().startsWith("PCA_")){
                    shutdown.addReceiver(subordinates[i].getName());
                }
            }
            this.send(shutdown);
        }
        catch (Exception e) {
            System.out.println( "Problem searching AMS: " + e );
            e.printStackTrace();
        }

        //Print simulation results
        System.out.println("###################");
        System.out.println("Simulation results");
        System.out.println(simulationResults());

        takeDown();
    }

    /**
     * Method used to delete the instance of the simulation
     */
    protected void takeDown(){
        doDelete();
    }

    /**
     * Computes the statistics to print
     * @return A String describing the results of the simulation.
     */
    public String simulationResults() {
        float sum = 0.0f;

        for (float record : satisfactionRecords){
            sum += record;
        }

        float averageSatisfaction = sum/satisfactionRecords.size();
        return "Average satisfaction for the simulation: "+averageSatisfaction;
    }
}
