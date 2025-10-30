package sepia_project.planner;

public interface Action {
    boolean isApplicable(State state);

    State apply(State state);

    double getCost();
    
    String toString();
}
