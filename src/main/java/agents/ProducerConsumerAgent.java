package agents;

import behaviours.ConsumerBehaviour;
import behaviours.ProducerBehaviour;
import behaviours.SellerBehaviour;
import jade.core.Agent;
import jade.core.behaviours.ParallelBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

/**
* Producer and consumer agent.
* The agents take part in the simulation.
* They can buy the goods they need and sell the ones they produces to keep their satisfaction high.
*/
public class ProducerConsumerAgent extends Agent {
    /**Identifier of the agent.*/
    private final int id;
    /** Speed at which the agent produces goods in units/sec. */
    private final float productionRate;
    /** Speed at which the agent consumes goods in units/sec. */
    private final float consumptionRate;
    /** Speed of decay of the agent while in starvation. */
    private final float decayRate;
    /** Type of product produced by this agent. */
    private final String product;
    /** Type of supply consumed by this agent. */
    private final String supply;
    /** Maximum amount of products this agent can have */
    private final long productMaxQuantity;
    /** Money the agent has. Every agent starts with N amount of money. */
    private float money;
    /** Satisfaction of the agent: 0 to 1, higher is better. */
    private float satisfaction;
    /** Sale's price of the product: starts at 1. */
    private float salePrice;
    /** Quantity of supply this agent has. */
    private float supplyQuantity;
    /** Quantity of products this agent has. */
    private float productQuantity;

    /** Parallel behaviour, used to add multiple behaviour that will run in parallel. */
    private ParallelBehaviour parallelBehaviour;
    /** Number of ticks the agent passed without supplies. */
    private int starvationCounter;

    public ProducerConsumerAgent(int id, float productionRate, float consumptionRate, float decayRate, String producedProduct, String consumedProduct, long productMaxQuantity, int money) {
        this.id = id;
        this.productionRate = productionRate;
        this.consumptionRate = consumptionRate;
        this.decayRate = decayRate;
        this.product = producedProduct;
        this.supply = consumedProduct;
        this.productMaxQuantity = productMaxQuantity;
        this.money = money;
        salePrice = 1.0f;
        satisfaction = 1.0f;
        starvationCounter = 0;
    }

    @Override
    protected void setup() {
        System.out.println("Hello World! My name is " + getLocalName());

        register(product + "-seller", "seller-" + this.getLocalName());

        parallelBehaviour = new ParallelBehaviour();
        addBehaviour(parallelBehaviour);

        // Ticker Behaviours - defined by productionRate and consumptionRate
        // Below formulas : (1 / (u/s)) * 1000   ->  s/u * 1000  ->  ms/u
        long productionDelta = (long) (1 / this.productionRate) * 1000;
        long consumptionDelta = (long) (1 / this.consumptionRate) * 1000;
        parallelBehaviour.addSubBehaviour(new ProducerBehaviour(this, productionDelta));
        parallelBehaviour.addSubBehaviour(new ConsumerBehaviour(this, consumptionDelta));

        // CyclicBehaviour
        parallelBehaviour.addSubBehaviour(new SellerBehaviour(this));
    }

    /**
     * Contain agent clean-up operations
     */
    protected void takeDown() {
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
        try {
            DFService.deregister(this);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
        // Printout a dismissal message
        System.out.println("Seller-agent "+getAID().getName()+" terminating.");
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
     * Subtract [amount] of money from the agent if he has enough.
     * @param amount Amount of money the agent has to pay
     * @return A boolean that tells if the agent had enough to pay
     */
    public boolean pay(float amount) {
        boolean hasEnough;
        if(hasEnough = ((money - amount) >= 0)) {
            money -= amount;
        }
        return hasEnough;
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

    public float getSupplyQuantity() {
        return supplyQuantity;
    }

    public float getProductQuantity() {
        return productQuantity;
    }

    public ParallelBehaviour getParallelBehaviour() {
        return parallelBehaviour;
    }
}

