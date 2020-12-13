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

/**
 * Behaviour that encapsulates the buyer aspect of the Agent
 */
public class BuyerBehaviour extends Behaviour {

    /**
     * The Agent possessing the behaviour
     */
    final ProducerConsumerAgent agent;

    /**
     * The sellers available for the sale of the product
     */
    private ArrayList<AID> sellerAgents;

    /**
     * The state used to specialize the behaviour (0: calling for proposal, 1: waiting for proposals, 2: listening to confirmation, 3: exiting)
     */
    private int state;

    /**
     * The number of replies the agent has got from the sellers
     */
    private int repliesCount;

    /**
     * The AID of the best dealer
     */
    private AID bestSeller;

    /**
     * The price given proposed by the best dealer
     */
    private float bestPrice;

    /**
     * The quantity of consumed good purchased
     */
    private int boughtQuantity;

    /**
     * The start date of the action to account for timeouts
     */
    private Date start;

    /**
     * Creates a new BuyerBehaviour instance
     * @param agent the Agent to which this Behaviour applies
     */
    public BuyerBehaviour(ProducerConsumerAgent agent) {
        super(agent);
        this.agent = agent;
        state = 0;
        repliesCount = 0;
    }

    /**
     * Decides which behaviour to take depending on the state
     */
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
     * Sends a CALL_FOR_PROPOSAL to all agent's supply sellers
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
            for(int i = 0; i < result.length; ++i){
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
     * Listens for PROPOSAL messages, select the best offer, and rejects the other ones
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
     * Listens for CONFIRM message and applies transaction
     */
    private void listenForConfirmation() {
        // Prepare the template to get proposals
        MessageTemplate mt = MatchPerformative(ACLMessage.CONFIRM);
        // Receive all proposals/refusals from seller agents
        ACLMessage reply = myAgent.receive(mt);
        if (reply != null) {
            // Apply the transaction
            if(agent.buy(boughtQuantity, bestPrice)){
                System.out.println("["+myAgent.getName()+"] Confirming purchase on client side");
            }

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
