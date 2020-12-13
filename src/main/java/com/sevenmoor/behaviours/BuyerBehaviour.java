package com.sevenmoor.behaviours;

import com.sevenmoor.agents.ProducerConsumerAgent;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;
import java.util.Date;

import static jade.lang.acl.MessageTemplate.MatchPerformative;

public class BuyerBehaviour extends Behaviour {
    final ProducerConsumerAgent agent;
    private ArrayList<AID> sellerAgents;
    private int state;
    private int repliesCount;
    private AID bestSeller;
    private float bestPrice;
    private int boughtQuantity;
    private Date start;

    public BuyerBehaviour(ProducerConsumerAgent agent) {
        super(agent);
        this.agent = agent;
        state = 0;
        repliesCount = 0;
    }

    @Override
    public void action(){
        start = new Date();
        switch (state) {
            case 0:
                callForProposal();
                break;
            case 1:
                listenForProposals();
                break;
            case 2:
                listenForConfirmation();
                break;
            default:
                break;
        }
    }

    /**
     * Send a CALL_FOR_PROPOSAL to all agent's supply sellers
     */
    private void callForProposal() {
        // Update the list of seller agents
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(agent.getSupply() + "-seller");
        template.addServices(sd);

        sellerAgents = new ArrayList<AID>();
        try {
            DFAgentDescription[] result = DFService.search(agent, template);
            System.out.println("Found the following seller agents:");
            for (int i = 0; i < result.length; ++i) {
                sellerAgents.add(result[i].getName());
                System.out.println(result[i].getName());
            }
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }

        // Send the cfp to all sellers
        ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
        for (AID sellerAgent : sellerAgents) {
            cfp.addReceiver(sellerAgent);
        }
        myAgent.send(cfp);
        System.out.println("["+myAgent.getName()+"] Sent call for proposal");

        // Update the attributes values
        state = 1;
    }

    /**
     * Listen for PROPOSAL messages and select the best offer
     */
    private void listenForProposals() {
        // Prepare the template to get proposals
        MessageTemplate mt = MatchPerformative(ACLMessage.PROPOSE);

        // Receive all proposals/refusals from seller agents
        ACLMessage reply = myAgent.receive(mt);
        if (reply != null) {
            // Reply received
            int sellerQuantity = Integer.parseInt(reply.getUserDefinedParameter("quantity"));
            float price = Float.parseFloat(reply.getUserDefinedParameter("price"));
            System.out.println("["+myAgent.getName()+"] Received proposal for quantity="+sellerQuantity+" and price="+price);
            // Is this offer the best so far ?
            if (bestSeller == null || price < bestPrice) {
                // Yes:
                // Remember the price and the sender
                bestPrice = price;
                bestSeller = reply.getSender();
            }
            repliesCount++;
            // Have all replies been received ?
            if (repliesCount >= sellerAgents.size()||new Date().getTime()-start.getTime()>1000) {
                // Yes:
                // Has the agent enough money to make an offer ?
                if (agent.getMoney() >= bestPrice) {
                    // Yes:
                    // Send the purchase order to the seller that provided the best offer
                    ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                    order.addReceiver(bestSeller);
                    // Calculate the quantity of products the agent can buy
                    boughtQuantity = Math.min((int) (agent.getMoney() / bestPrice), sellerQuantity);
                    String quantity = String.valueOf(boughtQuantity);
                    order.addUserDefinedParameter("quantity", quantity);
                    myAgent.send(order);

                    // Remove this agent from those who will receive a REJECTED_PROPOSAL
                    sellerAgents.remove(bestSeller);
                    // Go to state 2 and listen for confirmation
                    state = 2;
                }
                else {
                    // Go to state 3 and quit the purchase
                    state = 3;
                }

                // Send a REJECT_PROPOSAL to all sellers that should receive it
                ACLMessage reject = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
                System.out.println("["+myAgent.getName()+"] Rejecting bad offers");
                for (AID seller : sellerAgents) {
                    reject.addReceiver(seller);
                }
                agent.send(reject);
            }
        }
        else {
            if(new Date().getTime()-start.getTime()>1000){
                myAgent.removeBehaviour(this);
            }
            block();
        }
    }

    /**
     * Listen for CONFIRM message and apply transaction
     */
    private void listenForConfirmation() {
        // Prepare the template to get proposals
        MessageTemplate mt = MatchPerformative(ACLMessage.CONFIRM);
        // Receive all proposals/refusals from seller agents
        ACLMessage reply = myAgent.receive(mt);
        if (reply != null) {
            // Apply the transaction
            System.out.println("["+myAgent.getName()+"] Confirming purchase on client side");
            agent.buy(boughtQuantity, bestPrice); // TODO: Verify purchase success

            // Go to state 3 and quit the purchase
            state = 3;
        } else {
            if(new Date().getTime()-start.getTime()>10000){
                myAgent.removeBehaviour(this);
            }
            block();
        }
    }

    @Override
    public boolean done() {
        return state == 3;
    }
}
