package behaviours;

import agents.ProducerConsumerAgent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import static jade.lang.acl.MessageTemplate.MatchPerformative;

public class SellerBehaviour extends Behaviour {
    private final ProducerConsumerAgent agent;
    private int state;

    public SellerBehaviour(ProducerConsumerAgent agent) {
        super(agent);
        this.agent = agent;
        state = 0;
    }

    @Override
    public void action() {
        switch (state) {
            case 0:
                listenForProposal();
                break;
            case 1:
                listenForConfirmation();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + state);
        }
    }

    private void listenForProposal() {
        // Filter the messages that match a CFP (call for proposal)
        MessageTemplate mt = MatchPerformative(ACLMessage.CFP);
        ACLMessage msg = agent.receive(mt);

        // Does the filter contains a message ?
        if (msg != null) {
            // Yes:
            // Process the message and create a reply
            String title = msg.getContent();
            ACLMessage reply = msg.createReply();


            // TODO: Send PROPOSE answer

//            Integer price = (Integer) catalogue.get(title);
//            if (price != null) {
//                // The requested book is available for sale. Reply with the price
//                reply.setPerformative(ACLMessage.PROPOSE);
//                reply.setContent(String.valueOf(price.intValue()));
//            }
//            else {
//                // The requested book is NOT available for sale.
//                reply.setPerformative(ACLMessage.REFUSE);
//                reply.setContent("not-available");
//            }

            // Send the reply
            agent.send(reply);

            state = 1;
        }
        else {
            // No:
            // Wait for another message
            block();
        }
    }

    private void listenForConfirmation() {
        // Filter the messages that match a ACCEPT_PROPOSAL or REJECT_PROPOSAL
        MessageTemplate mt = MessageTemplate.or(MatchPerformative(ACLMessage.ACCEPT_PROPOSAL), MatchPerformative(ACLMessage.REJECT_PROPOSAL));
        ACLMessage msg = agent.receive(mt);

        // Does the filter contains a message ?
        if (msg != null) {
            // Yes:
            // Process the message
            int performative = msg.getPerformative();

            if (performative == ACLMessage.ACCEPT_PROPOSAL) {
                // TODO: Apply transaction
            } else {
                state = 2;
            }
        }
        else {
            // No:
            // Wait for another message
            block();
        }
    }

    @Override
    public boolean done() {
        return state == 2;
    }
}
