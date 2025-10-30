package sepia_project.planner;


public class Node implements Comparable<Node> {
    private final State state;
    private final Node parent;   
    private final Action action; 
    
    private final double g; 
    private final double h; 
    private final double f; 

    public Node(State state, Node parent, Action action, double g, double h) {
        this.state = state;
        this.parent = parent;
        this.action = action;
        this.g = g;
        this.h = h;
        this.f = g + h;
    }

    public State getState() { return state; }
    public Node getParent() { return parent; }
    public Action getAction() { return action; }
    public double getG() { return g; }
    public double getF() { return f; }

    @Override
    public int compareTo(Node other) {
       
        return Double.compare(this.f, other.f);
    }
}
