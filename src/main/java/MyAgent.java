import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

// Producer and consumer agent.
// The agents take part in the simulation.
// They can buy the goods they need and sell the ones they produces to keep their satisfaction high.
public class MyAgent extends Agent {
    // Identifier of the agent
    private final int id;
    // Speed at which the agent produces goods: random or passed by parameter.
    private final float productionRate;
    // Speed at which the agent consumes goods: random or passed by parameter.
    private final float consumptionRate;
    // Type of product produced by this agent.
    private final Product producedProduct;
    // Type of product consumed by this agent.
    private final Product consumedProduct;
    // Money the agent has. Every agent starts with N amount of money.
    private float money;
    // Satisfaction of the agent: 0 to 1, higher is better.
    private float satisfaction;
    // Sale's price of the product: starts at 1.
    private float salePrice;

    public MyAgent(int id, float productionRate, float consumptionRate, Product producedProduct, Product consumedProduct, int money) {
        this.id = id;
        this.productionRate = productionRate;
        this.consumptionRate = consumptionRate;
        this.producedProduct = producedProduct;
        this.consumedProduct = consumedProduct;
        this.money = money;
        salePrice = 1.0f;
        satisfaction = 1.0f;
    }

    @Override
    protected void setup() {
        System.out.println("Hello World! My name is "+getLocalName());

        register("goods-seller", producedProduct.name + "-seller");
    }

    // Put agent clean-up operations here
    protected void takeDown() {
        unregister();
    }

    // Registers the goods-selling service in the yellow pages
    public void register(String type, String name) {
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

    // Unregisters from the yellow pages
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

    public void consume() {

    }

    public void produce() {

    }

    public void buy() {

    }

    public void sell() {

    }

    public boolean isSatisfied() {
        return false;
    }

    public float getMoney() {
        return money;
    }

    public void setMoney(float money) {
        this.money = money;
    }

    public float getSatisfaction() {
        return satisfaction;
    }

    public void setSatisfaction(float satisfaction) {
        this.satisfaction = satisfaction;
    }

    public float getProductionRate() {
        return productionRate;
    }

    public float getConsumptionRate() {
        return consumptionRate;
    }

    public float getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(float salePrice) {
        this.salePrice = salePrice;
    }

    public Product getProducedProduct() {
        return producedProduct;
    }

    public Product getConsumedProduct() {
        return consumedProduct;
    }
}
