package com.sevenmoor.behaviours;

import com.sevenmoor.agents.ProducerConsumerAgent;
import jade.core.behaviours.TickerBehaviour;

public class ConsumerBehaviour extends TickerBehaviour {
    private final ProducerConsumerAgent agent;
    private BuyerBehaviour buyerBehaviour;

    public ConsumerBehaviour(ProducerConsumerAgent agent, long period) {
        super(agent, period);
        this.agent = agent;
        this.buyerBehaviour = null;
    }

    @Override
    protected void onTick() {
        // Agent must consume goods
        agent.consume();
        System.out.println("["+myAgent.getName()+"] Consuming 1 "+agent.getSupply()+", "+agent.getSupplyQuantity()+" remaining");

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
