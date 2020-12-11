package behaviours;

import agents.ProducerConsumerAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

public class BuyerBehaviour extends CyclicBehaviour {
    final ProducerConsumerAgent agent;

    public BuyerBehaviour(ProducerConsumerAgent agent) {
        super(agent);
        this.agent = agent;
    }

    @Override
    public void action() {
        // Update the list of seller agents
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(agent.getSupply() + "-seller");
        template.addServices(sd);
        try {
            DFAgentDescription[] result = DFService.search(agent, template);
            System.out.println("Found the following seller agents:");
            AID[] sellerAgents = new AID[result.length];
            for (int i = 0; i < result.length; ++i) {
                sellerAgents[i] = result[i].getName();
                System.out.println(sellerAgents[i].getName());
            }
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }

        // TODO: Emit CFP to all sellers
        // TODO: Listen for answers
        // TODO: Send ACCEPT_PROPOSAL to selected and REJECT_PROPOSAL to others
        // TODO: Listen for CONFIRM and apply transaction
    }
}
