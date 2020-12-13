package com.sevenmoor.behaviours;

import com.sevenmoor.agents.ProducerConsumerAgent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import static jade.lang.acl.MessageTemplate.MatchPerformative;

public class TakeDownBehaviour extends CyclicBehaviour {

    private ProducerConsumerAgent agent;

    public TakeDownBehaviour(ProducerConsumerAgent agent){
        this.agent = agent;
    }

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
