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
 * Simulation in which the agents will interact with each other.
 * Statistics on its progress are displayed at the end.
*/
public class Simulation extends Agent {
    /** Products that will be exchanged, produced and consumed during the simulation. */
    private String[] products;
    /** Agents that will take part in the simulation. */
    private Agent[] agents;

    private ArrayList<Float> satisfactionRecords;

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

    protected void takeDown(){
        doDelete();
    }

    public String simulationResults() {
        float sum = 0.0f;

        for (float record : satisfactionRecords){
            sum += record;
        }

        float averageSatisfaction = sum/satisfactionRecords.size();
        return "Average satisfaction for the simulation: "+averageSatisfaction;
    }
}
