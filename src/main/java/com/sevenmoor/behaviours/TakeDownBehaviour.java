package com.sevenmoor.behaviours;

import com.sevenmoor.agents.ProducerConsumerAgent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import static jade.lang.acl.MessageTemplate.MatchPerformative;

/**
 * Behaviour that handles shutdown message for a ProducerConsumerAgent
 */
public class TakeDownBehaviour extends CyclicBehaviour {

    private ProducerConsumerAgent agent;

    /**
     * Creates an instance of the Behaviour
     * @param agent The agent that needs to handle shutdown messages
     */
    public TakeDownBehaviour(ProducerConsumerAgent agent){
        this.agent = agent;
    }

    /**
     * Listens for message with a REQUEST performative,
     * and if the message is a shutdown message, kills the agent properly.
     */
    @Override
    public void action() {
        MessageTemplate mt = MatchPerformative(ACLMessage.REQUEST);
        ACLMessage msg = myAgent.receive(mt);
        if (msg != null){
            if(msg.getContent().equals("shutdown")){
                agent.doDelete();
            }
        }
        else {
            block();
        }
    }
}
