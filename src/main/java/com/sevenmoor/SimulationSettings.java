package com.sevenmoor;

/** Settings to be used for the simulation. */
public class SimulationSettings {
    /** Number of com.sevenmoor.agents to be created for the simulation. */
    public final int agentCount;
    /** Names of the products to be created for the simulation. */
    public final String[] productNames;
    /** How much money the agent will have at the start of the simulation */
    public final float startMoney;
    /** How much time the simulation will run in seconds. */
    public final long simulationDuration;

    public SimulationSettings(int agentCount, String[] productNames, long simulationDuration, float startMoney) {
        this.agentCount = agentCount;
        this.productNames = productNames;
        this.startMoney = startMoney;
        this.simulationDuration = simulationDuration;
    }

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
