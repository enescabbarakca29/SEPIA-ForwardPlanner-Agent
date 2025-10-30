package sepia_project.planner.actions;

import sepia_project.planner.Action;
import sepia_project.planner.State;

public class BuildPeasantAction implements Action {

    @Override
    public boolean isApplicable(State state) {
        
        return state.getCurrentGoldTally() >= 400 &&
               state.getPeasantCount() < state.getFoodSupply() &&
               state.getPeasantLocation().equals("TownHall");
    }

    @Override
    public State apply(State state) {
        return state.applyBuildPeasant();
    }

    @Override
    public double getCost() {
        return 1.0;
    }

    @Override
    public String toString() {
        return "BUILD_PEASANT";
    }
}
