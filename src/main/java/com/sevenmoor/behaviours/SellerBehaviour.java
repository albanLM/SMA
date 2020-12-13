package com.sevenmoor.behaviours;

import com.sevenmoor.agents.ProducerConsumerAgent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import javafx.animation.KeyFrame;

import java.util.Date;

import static jade.lang.acl.MessageTemplate.MatchPerformative;

public class SellerBehaviour extends CyclicBehaviour {
    private final ProducerConsumerAgent agent;
    private int state;
    private Date start;

    public SellerBehaviour(ProducerConsumerAgent agent) {
        super(agent);
        this.agent = agent;
        state = 0;
    }

    @Override
    public void action() {
        start = new Date();
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
                System.out.println("["+myAgent.getName()+"] Confirming sale for quantity="+quantity);
                agent.send(reply);

                // Complete the sale
                agent.sell(quantity); // TODO: Verify sale success

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
