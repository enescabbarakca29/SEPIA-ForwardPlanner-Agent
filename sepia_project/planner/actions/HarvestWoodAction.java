package sepia_project.planner.actions;

import sepia_project.planner.Action;
import sepia_project.planner.State;

public class HarvestWoodAction implements Action {
    private final String forestId;

    public HarvestWoodAction(String forestId) {
        this.forestId = forestId;
    }

    @Override
    public boolean isApplicable(State state) {
        
        return state.getPeasantLocation().equals(forestId) &&
               state.getHolding() == State.PeasantHolding.NOTHING &&
               state.getForestCapacity(forestId) > 0;
    }

    @Override
    public State apply(State state) {

        return state.applyHarvestWood(forestId);
    }

    @Override
    public double getCost() {
        return 1.0; 
    }

    @Override
    public String toString() {
        return "HARVEST_WOOD " + forestId;
    }
}
