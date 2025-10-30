package sepia_project.planner;

import java.util.List;


public class Mission {
    private int peasantID;
    private List<Action> steps;
    private int currentStepIndex;

    public Mission(int peasantID, List<Action> steps) {
        this.peasantID = peasantID;
        this.steps = steps;
        this.currentStepIndex = 0;
    }

    public int getPeasantID() {
        return peasantID;
    }

    public Action getCurrentStep() {
        if (isComplete()) {
            return null;
        }
        return steps.get(currentStepIndex);
    }
    
    public List<Action> getSteps() {
        return this.steps;
    }

    public void advance() {
        if (!isComplete()) {
            currentStepIndex++;
        }
    }

    public boolean isComplete() {
        return currentStepIndex >= steps.size();
    }
}