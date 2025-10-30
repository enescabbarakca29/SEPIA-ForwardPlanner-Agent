package sepia_project.planner.actions;

import sepia_project.planner.Action;
import sepia_project.planner.State;

public class DepositAction implements Action {

    public DepositAction() {

    }

    @Override
    public boolean isApplicable(State state) {
        
        return state.getPeasantLocation().equals("TownHall") &&
               state.getHolding() != State.PeasantHolding.NOTHING;
    }

    @Override
    public State apply(State state) {
        
        return state.applyDeposit();
    }

    @Override
    public double getCost() {
        return 1.0; 
    }

    @Override
    public String toString() {
        return "DEPOSIT";
    }
}
