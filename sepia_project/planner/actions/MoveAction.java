package sepia_project.planner.actions;

import sepia_project.planner.Action;
import sepia_project.planner.State;

public class MoveAction implements Action {
    private final String toLocation;

    public MoveAction(String toLocation) {
        this.toLocation = toLocation;
    }

    public String getToLocation() {
        return toLocation;
    }

    @Override
    public boolean isApplicable(State state) {
        return !state.getPeasantLocation().equals(toLocation);
    }

    @Override
    public State apply(State state) {
        return state.applyMove(toLocation);
    }

    @Override
    public double getCost() {

        return 1.0;
    }

    @Override
    public String toString() {
        return "MOVE " + toLocation;
    }
}