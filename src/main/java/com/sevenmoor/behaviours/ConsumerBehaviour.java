package com.sevenmoor.behaviours;

import com.sevenmoor.agents.ProducerConsumerAgent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour that allows the agent to consume supplies.
 */
public class ConsumerBehaviour extends TickerBehaviour {
    /** Agent this behaviour is attached to. */
    private final ProducerConsumerAgent agent;

    /** Behaviour created on demand when the agent needs to buy supplies. */
    private BuyerBehaviour buyerBehaviour;

    public ConsumerBehaviour(ProducerConsumerAgent agent, long period) {
        super(agent, period);
        this.agent = agent;
        this.buyerBehaviour = null;
    }

    /**
     * Make the agent consume his goods at a given frequency.
     * If the agent has no supplies left, a BuyerBehaviour is created to allow him to fill is stock.
     * When the agent runs out of supplies and satisfaction, he is destroyed.
     */
    @Override
    protected void onTick() {
        // Agent must consume goods
        agent.consume();
        System.out.println("["+myAgent.getName()+"] Consuming 1 "+agent.getSupply()+", "+agent.getSupplyQuantity()+" remaining");

        //Sending stats to simulation
        ACLMessage statistics = new ACLMessage(ACLMessage.INFORM);
        statistics.addReceiver(agent.getSimAID());
        statistics.setContent(""+agent.getSatisfaction());
        myAgent.send(statistics);

        // Is there any supply left ?
        if (!(agent.getSupplyQuantity() > 0)) {
            // No
            // Is the agent already trying to buy ?
            if (buyerBehaviour == null || buyerBehaviour.done()) {
                // No
                // Tell the agent to buy some supplies
                buyerBehaviour = new BuyerBehaviour(agent);
                agent.getParallelBehaviour().addSubBehaviour(buyerBehaviour);
            } // else: Yes -> just wait
        } // else: Yes -> nothing to do
    }
}
