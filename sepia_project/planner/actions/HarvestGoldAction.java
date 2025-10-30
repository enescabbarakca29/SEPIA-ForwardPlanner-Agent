package sepia_project.planner.actions;

import sepia_project.planner.Action;
import sepia_project.planner.State;

public class HarvestGoldAction implements Action {
    private final String mineId;

    public HarvestGoldAction(String mineId) {
        this.mineId = mineId;
    }

    @Override
    public boolean isApplicable(State state) {
        
        return state.getPeasantLocation().equals(mineId) &&
               state.getHolding() == State.PeasantHolding.NOTHING &&
               state.getGoldMineCapacity(mineId) > 0;
    }

    @Override
    public State apply(State state) {
        
        return state.applyHarvestGold(mineId);
    }

    @Override
    public double getCost() {
        return 1.0; 
    }

    @Override
    public String toString() {
        return "HARVEST_GOLD " + mineId;
    }
}
