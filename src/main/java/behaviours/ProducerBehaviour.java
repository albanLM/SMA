package behaviours;

import agents.ProducerConsumerAgent;
import jade.core.Agent;
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
