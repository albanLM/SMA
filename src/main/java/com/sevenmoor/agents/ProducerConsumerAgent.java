package com.sevenmoor.agents;

import com.sevenmoor.behaviours.ConsumerBehaviour;
import com.sevenmoor.behaviours.ProducerBehaviour;
import com.sevenmoor.behaviours.SellerBehaviour;
import com.sevenmoor.behaviours.TakeDownBehaviour;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import static jade.lang.acl.MessageTemplate.MatchPerformative;

/**
* Producer and consumer agent.
* The agents take part in the simulation.
* They can buy the goods they need and sell the ones they produces to keep their satisfaction high.
*/
public class ProducerConsumerAgent extends Agent {
    /**Identifier of the agent.*/
    private int id;
    /** Speed at which the agent produces goods in units/sec. */
    private float productionRate;
    /** Speed at which the agent consumes goods in units/sec. */
    private float consumptionRate;
    /** Speed of decay of the agent while in starvation. */
    private float decayRate;
    /** Type of product produced by this agent. */
    private String product;
    /** Type of supply consumed by this agent. */
    private String supply;
    /** Maximum amount of products this agent can have */
    private long productMaxQuantity;
    /** Money the agent has. Every agent starts with N amount of money. */
    private float money;
    /** Satisfaction of the agent: 0 to 1, higher is better. */
    private float satisfaction;
    /** Sale's price of the product: starts at 1. */
    private float salePrice;
    /** Quantity of supply this agent has. */
    private int supplyQuantity;
    /** Quantity of products this agent has. */
    private int productQuantity;

    /** AID of the parent simulation*/
    private AID simAID;

    /** Parallel behaviour, used to add multiple behaviour that will run in parallel. */
    private ParallelBehaviour parallelBehaviour;
    /** Number of ticks the agent passed without supplies. */
    private int starvationCounter;

    @Override
    protected void setup() {
        // Get the agent arguments
        Object[] args = getArguments();
        if (args != null && args.length != 0) {
            try {
                id = (args[0].getClass().getSimpleName().equals("Integer")) ? (int) args[0] : Integer.parseInt((String) args[0]);
                productionRate = (args[1].getClass().getSimpleName().equals("Float")) ? (float) args[1] : Float.parseFloat((String)args[1]);
                consumptionRate = (args[2].getClass().getSimpleName().equals("Float")) ? (float) args[2] : Float.parseFloat((String)args[2]);
                decayRate = (args[3].getClass().getSimpleName().equals("Float")) ? (float) args[3] : Float.parseFloat((String)args[3]);
                product = (String) args[4];
                supply = (String) args[5];
                productMaxQuantity = (args[6].getClass().getSimpleName().equals("Long")) ? (long) args[6] : Long.parseLong((String) args[6]);
                money = (args[7].getClass().getSimpleName().equals("Float")) ? (float) args[7] : Float.parseFloat((String)args[7]);
                supplyQuantity = (args[8].getClass().getSimpleName().equals("Integer")) ? (int) args[8] : Integer.parseInt((String) args[8]);
                simAID = (AID) args[9];
                satisfaction = 1.0f;
                salePrice = 1.0f;
                productQuantity = 0;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.out.println("Hello World! My name is " + getLocalName());

        register(product + "-seller", "seller-" + this.getLocalName());

        parallelBehaviour = new ParallelBehaviour();
        addBehaviour(parallelBehaviour);

        // Ticker Behaviours - defined by productionRate and consumptionRate
        // Below formulas : (1 / (u/s)) * 1000   ->  s/u * 1000  ->  ms/u
        long productionDelta = (long) (( 1 / this.productionRate) * 1000);
        long consumptionDelta = (long) (( 1 / this.consumptionRate) * 1000);
        parallelBehaviour.addSubBehaviour(new ProducerBehaviour(this, productionDelta));
        parallelBehaviour.addSubBehaviour(new ConsumerBehaviour(this, consumptionDelta));

        // CyclicBehaviour
        parallelBehaviour.addSubBehaviour(new SellerBehaviour(this));
        parallelBehaviour.addSubBehaviour(new TakeDownBehaviour(this));
    }

    /**
     * Contain agent clean-up operations
     */
    public void takeDown() {
        unregister();
    }

    /**
     * Register the goods-selling service in the yellow pages
     */
    private void register(String type, String name) {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(type);
        sd.setName(name);
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    /**
     * Unregister from the yellow pages
     */
    private void unregister() {
        try{
            DFService.deregister(this);
        }
        catch (FIPAException fe) {} //Normal if not registered, do nothing
        //System.out.println("Seller-agent "+getAID().getName()+" terminating.");
    }

    /**
     * Consume one unit of supply. If no supplies left, loose satisfaction.
     */
    public void consume() {
        if (supplyQuantity > 0) {
            --supplyQuantity;
            restoreSatisfaction();
        } else {
            looseSatisfaction();
        }
        salePrice = (supplyQuantity>0) ? supplyQuantity/money : (0.5f + satisfaction*0.5f)/money;
    }

    /**
     * Restore the satisfaction of the agent to 100%.
     */
    private void restoreSatisfaction() {
        satisfaction = 1;
        starvationCounter = 0;
    }

    /**
     * Decrease the satisfaction of the agent and delete it when 0% is reached.
     * The decay is exponential and will rise each tick.
     */
    private void looseSatisfaction() {
        // Decrease the satisfaction of the agent
        satisfaction -= decayRate * Math.pow(2, starvationCounter++);

        // If the agent reached 0 satisfaction, delete it
        if (satisfaction <= 0.0f) {
            doDelete();
        }
    }

    /**
     * Produce one unit of product.
     * The product quantity can't exceed the threshold specified at the creation of the agent.
     */
    public void produce() {
        if(productQuantity < productMaxQuantity) ++productQuantity;
    }

    /**
     * Buy [quantity] of supplies in exchange for [price] money.
     * @param quantity Quantity of supplies to buy.
     * @param price Total price for the purchase.
     * @return Purchase success.
     */
    public boolean buy(int quantity, float price) {
        boolean success;
        if(success = ((money - price*quantity) >= 0)) {
            money -= price*quantity;
            supplyQuantity += quantity;
        }
        return success;
    }

    /**
     * Sell [quantity] of products in exchange for [salePrice] money.
     * @param quantity Quantity of products to sell.
     * @return Sale success.
     */
    public boolean sell(int quantity) {
        boolean success;
        if (success = (supplyQuantity >= quantity)) {
            supplyQuantity -= quantity;
            money += salePrice*quantity;
        }
        return success;
    }

    /**
     * An agent is unsatisfied if his satisfaction is below 50%
     */
    public boolean isSatisfied() {
        return satisfaction >= 0.5f;
    }

    /////////////////////////////////////////////////
    //////////////////// GETTERS ////////////////////
    /////////////////////////////////////////////////
    public int getId() {
        return id;
    }

    public float getProductionRate() {
        return productionRate;
    }

    public float getConsumptionRate() {
        return consumptionRate;
    }

    public String getProduct() {
        return product;
    }

    public String getSupply() {
        return supply;
    }

    public long getProductMaxQuantity() {
        return productMaxQuantity;
    }

    public float getMoney() {
        return money;
    }

    public float getSatisfaction() {
        return satisfaction;
    }

    public float getSalePrice() {
        return salePrice;
    }

    public int getSupplyQuantity() {
        return supplyQuantity;
    }

    public int getProductQuantity() {
        return productQuantity;
    }

    public ParallelBehaviour getParallelBehaviour() {
        return parallelBehaviour;
    }

    public AID getSimAID() {
        return simAID;
    }
}

