package com.sevenmoor.behaviours;

import com.sevenmoor.agents.ProducerConsumerAgent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import static jade.lang.acl.MessageTemplate.MatchPerformative;

/**
 * Behaviour that allows the agent to make transactions with potential buyers.
 */
public class SellerBehaviour extends CyclicBehaviour {
    /** Agent this behaviour is attached to. */
    private final ProducerConsumerAgent agent;

    /** Current state of the transaction. From 0 to 1, with :
     * 0: Idle, listening for calls of proposal
     * 1: A proposal has been sent and the agent listens for answer
     */
    private int state;

    public SellerBehaviour(ProducerConsumerAgent agent) {
        super(agent);
        this.agent = agent;
        state = 0;
    }

    /**
     * Make the agent listen for proposals and sell his goods.
     * The sales are managed with states. Each state corresponds to a different step of the transaction.
     * When a transaction is complete, the agent comes back to state 0 and idles until a new call for proposal is received.
     */
    @Override
    public void action() {
        switch (state) {
            case 0:
                listenForProposal();
                break;
            case 1:
                listenForAnswer();
                break;
            default:
                break;
        }
    }

    /**
     * Listen for CFP (CALL_FOR_PROPOSAL) messages and send sale information.
     */
    private void listenForProposal() {
        // Filter the messages that match a CFP (call for proposal)
        MessageTemplate mt = MatchPerformative(ACLMessage.CFP);
        ACLMessage msg = agent.receive(mt);

        // Does the filter contains a message ?
        if (msg != null) {
            System.out.println("["+myAgent.getName()+"] Received proposal call");
            // Yes:
            // Process the message and create a reply
            String title = msg.getContent();
            ACLMessage reply = msg.createReply();
            reply.setPerformative(ACLMessage.PROPOSE);
            // Add the quantity and price of the product
            String quantity = String.valueOf(agent.getProductQuantity());
            String salePrice = String.valueOf(agent.getSalePrice());
            reply.addUserDefinedParameter("quantity", quantity);
            reply.addUserDefinedParameter("price", salePrice);

            System.out.println("["+myAgent.getName()+"] Proposing price="+salePrice+" and quantity="+quantity);
            // Send the reply
            agent.send(reply);

            // Go to answer listening state
            state = 1;
        }
        else {
            // No:
            // Wait for another message
            block();
        }
    }

    /**
     * Listen to the ACCEPT_PROPOSAL or REJECT_PROPOSAL messages and respectively send a confirmation or exit the transaction.
     */
    private void listenForAnswer() {
        // Filter the messages that match a ACCEPT_PROPOSAL or REJECT_PROPOSAL
        MessageTemplate mt = MessageTemplate.or(MatchPerformative(ACLMessage.ACCEPT_PROPOSAL), MatchPerformative(ACLMessage.REJECT_PROPOSAL));
        ACLMessage msg = agent.receive(mt);

        // Does the filter contains a message ?
        if (msg != null) {
            // Yes:
            // Process the message
            int performative = msg.getPerformative();
            // Has the agent accepted the proposal ?
            if (performative == ACLMessage.ACCEPT_PROPOSAL) {
                // Yes:
                // Send a confirmation
                String quantityString = msg.getUserDefinedParameter("quantity");
                int quantity = Integer.parseInt(quantityString);

                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.CONFIRM);

                agent.send(reply);

                // Complete the sale
                if(agent.sell(quantity)){
                    System.out.println("["+myAgent.getName()+"] Confirming sale for quantity="+quantity);
                }

                state = 0;
            } else {
                // No:
                // Back to proposal listening state
                state = 0;
            }
        }
        else {
            // No:
            // Wait for another message
            block();
        }
    }
}
