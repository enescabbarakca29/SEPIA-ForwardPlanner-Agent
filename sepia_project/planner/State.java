package sepia_project.planner;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class State {

    private final String peasantLocation; 
    
    public enum PeasantHolding {
        NOTHING,
        GOLD,
        WOOD
    }
    private final PeasantHolding holding;

    private final int currentGoldTally;
    private final int currentWoodTally;

    private final Map<String, Integer> goldMineCapacities;
    private final Map<String, Integer> forestCapacities;
    
    
    private final int peasantCount;
    private final int foodSupply;

    
    public State(String initialPeasantLocation, 
                 Map<String, Integer> initialGoldMines, 
                 Map<String, Integer> initialForests) {
        this.peasantLocation = initialPeasantLocation;
        this.holding = PeasantHolding.NOTHING;
        this.currentGoldTally = 0;
        this.currentWoodTally = 0;
        this.goldMineCapacities = new HashMap<>(initialGoldMines);
        this.forestCapacities = new HashMap<>(initialForests);
        
        
        this.peasantCount = 1;
        this.foodSupply = 3; 
    }
    
    
    private State(String newLocation, PeasantHolding newHolding, 
                  int newGoldTally, int newWoodTally,
                  Map<String, Integer> newGoldMines, Map<String, Integer> newForests,
                  int newPeasantCount, int newFoodSupply) { 
        this.peasantLocation = newLocation;
        this.holding = newHolding;
        this.currentGoldTally = newGoldTally;
        this.currentWoodTally = newWoodTally;
        this.goldMineCapacities = newGoldMines;
        this.forestCapacities = newForests;
        this.peasantCount = newPeasantCount;
        this.foodSupply = newFoodSupply;
    }

    
    public State applyBuildPeasant() {
        
        return new State(this.peasantLocation, this.holding,
                         this.currentGoldTally - 400, 
                         this.currentWoodTally,
                         this.goldMineCapacities, this.forestCapacities,
                         this.peasantCount + 1, 
                         this.foodSupply);
    }
    
    
    public State applyMove(String newLocation) {
        return new State(newLocation, this.holding, this.currentGoldTally, 
                         this.currentWoodTally, this.goldMineCapacities, this.forestCapacities,
                         this.peasantCount, this.foodSupply);
    }
    
    public State applyHarvestGold(String mineID) {
        Map<String, Integer> newGoldMines = new HashMap<>(this.goldMineCapacities);
        newGoldMines.put(mineID, newGoldMines.get(mineID) - 100);
        return new State(this.peasantLocation, PeasantHolding.GOLD,  
                         this.currentGoldTally, this.currentWoodTally, newGoldMines, this.forestCapacities,
                         this.peasantCount, this.foodSupply);
    }

    public State applyHarvestWood(String forestID) {
        Map<String, Integer> newForests = new HashMap<>(this.forestCapacities);
        newForests.put(forestID, newForests.get(forestID) - 100);
        return new State(this.peasantLocation, PeasantHolding.WOOD,  
                         this.currentGoldTally, this.currentWoodTally, this.goldMineCapacities, newForests,
                         this.peasantCount, this.foodSupply);
    }

    public State applyDeposit() {
        int newGold = this.currentGoldTally + (this.holding == PeasantHolding.GOLD ? 100 : 0);
        int newWood = this.currentWoodTally + (this.holding == PeasantHolding.WOOD ? 100 : 0);
        return new State(this.peasantLocation, PeasantHolding.NOTHING,
                         newGold, newWood, this.goldMineCapacities, this.forestCapacities,
                         this.peasantCount, this.foodSupply);
    }

    
    public int getCurrentGoldTally() { return currentGoldTally; }
    public int getCurrentWoodTally() { return currentWoodTally; }
    public String getPeasantLocation() { return peasantLocation; }
    public PeasantHolding getHolding() { return holding; }
    public int getGoldMineCapacity(String mineID) { return goldMineCapacities.getOrDefault(mineID, 0); }
    public int getForestCapacity(String forestID) { return forestCapacities.getOrDefault(forestID, 0); }
    public Map<String, Integer> getGoldMineCapacities() { return goldMineCapacities; }
    public Map<String, Integer> getForestCapacities() { return forestCapacities; }
    public int getPeasantCount() { return peasantCount; } 
    public int getFoodSupply() { return foodSupply; }   

    public boolean isGoal(int requiredGold, int requiredWood) {
        return this.currentGoldTally >= requiredGold && this.currentWoodTally >= requiredWood;
    }

    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        State state = (State) o;
        return currentGoldTally == state.currentGoldTally &&
               currentWoodTally == state.currentWoodTally &&
               peasantCount == state.peasantCount && 
               foodSupply == state.foodSupply &&    
               peasantLocation.equals(state.peasantLocation) &&
               holding == state.holding &&
               goldMineCapacities.equals(state.goldMineCapacities) &&
               forestCapacities.equals(state.forestCapacities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(peasantLocation, holding, currentGoldTally, currentWoodTally,
                            goldMineCapacities, forestCapacities, peasantCount, foodSupply); 
    }
    
    @Override
    public String toString() {
        return "State[Loc:" + peasantLocation + ", Holds:" + holding + 
               ", G:" + currentGoldTally + ", W:" + currentWoodTally + 
               ", Peasants:" + peasantCount + "]"; 
    }
}