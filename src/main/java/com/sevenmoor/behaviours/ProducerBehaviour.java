package com.sevenmoor.behaviours;

import com.sevenmoor.agents.ProducerConsumerAgent;
import jade.core.behaviours.TickerBehaviour;

public class ProducerBehaviour extends TickerBehaviour {
    final ProducerConsumerAgent agent;

    public ProducerBehaviour(ProducerConsumerAgent agent, long period) {
        super(agent, period);
        this.agent = agent;
    }

    @Override
    protected void onTick() {
        agent.produce();
    }
}
