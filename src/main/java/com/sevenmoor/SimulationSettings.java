package com.sevenmoor;

/** Settings to be used for the simulation. */
public class SimulationSettings {
    /** Number of ProducerConsumerAgent instances to be created for the simulation. */
    public final int agentCount;

    /** Names of the products to be created for the simulation. */
    public final String[] productNames;

    /** How much money each agent will have at the start of the simulation */
    public final float startMoney;

    /** How much time the simulation will run in seconds. */
    public final long simulationDuration;

    /**
     * Uses the Arguments passed to the simulation to create human-readable settings
     * @param args an Object array that represent the parameters
     */
    public SimulationSettings(Object[] args){
        agentCount = Integer.parseInt((String) args[0]);
        startMoney = Float.parseFloat((String) args[1]);
        simulationDuration = Long.parseLong((String) args[2]);
        productNames = new String[args.length-3];
        for (int i=3; i<args.length; i++){
            productNames[i-3] = (String) args[i];
        }
    }
}
