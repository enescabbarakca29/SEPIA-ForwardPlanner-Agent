package sepia_project.planner;

import sepia_project.planner.actions.*;
import java.util.*;


 
public class Planner {
    private final MapInfo mapInfo;

    public Planner(MapInfo mapInfo) {
        this.mapInfo = mapInfo;
    }

    
    private List<Action> generateSensibleActions(State state) {
        List<Action> actions = new ArrayList<>();
        if (state.getHolding() != State.PeasantHolding.NOTHING) {
            if (state.getPeasantLocation().equals("TownHall")) {
                actions.add(new DepositAction());
            } else {
                actions.add(new MoveAction("TownHall"));
            }
        } else {
            String currentLoc = state.getPeasantLocation();
            if (currentLoc.startsWith("GoldMine") && state.getGoldMineCapacity(currentLoc) > 0) {
                actions.add(new HarvestGoldAction(currentLoc));
            } else if (currentLoc.startsWith("Forest") && state.getForestCapacity(currentLoc) > 0) {
                actions.add(new HarvestWoodAction(currentLoc));
            }
            
            
            if (currentLoc.equals("TownHall") && state.getCurrentGoldTally() >= 400 && state.getPeasantCount() < state.getFoodSupply()) {
                actions.add(new BuildPeasantAction());
            }

            for (Map.Entry<String, Integer> mine : state.getGoldMineCapacities().entrySet()) {
                if (mine.getValue() > 0) {
                    actions.add(new MoveAction(mine.getKey()));
                }
            }
            for (Map.Entry<String, Integer> forest : state.getForestCapacities().entrySet()) {
                if (forest.getValue() > 0) {
                    actions.add(new MoveAction(forest.getKey()));
                }
            }
        }
        Set<Action> uniqueActions = new HashSet<>(actions);
        return new ArrayList<>(uniqueActions);
    }

   
    private double heuristic(State state, int goalGold, int goalWood) {
        
        int neededGold = Math.max(0, goalGold - state.getCurrentGoldTally());
        int neededWood = Math.max(0, goalWood - state.getCurrentWoodTally());
        if (state.getHolding() == State.PeasantHolding.GOLD) neededGold -= 100;
        if (state.getHolding() == State.PeasantHolding.WOOD) neededWood -= 100;

        
        double goldTrips = Math.ceil(Math.max(0, neededGold) / 100.0);
        double woodTrips = Math.ceil(Math.max(0, neededWood) / 100.0);
        double totalTrips = goldTrips + woodTrips;

        
        double avgTripCost = 12.0; 
        double totalWorkInTime = totalTrips * avgTripCost; 

        
        int peasants = state.getPeasantCount();
        double estimatedTime = (peasants > 0) ? (totalWorkInTime / peasants) : Double.POSITIVE_INFINITY;

       
        if (state.getHolding() != State.PeasantHolding.NOTHING) {
            estimatedTime += mapInfo.getDistance(state.getPeasantLocation(), "TownHall");
        }
        
        return estimatedTime;
    }


   
    public List<Action> plan(State initialState, int goalGold, int goalWood) {
        PriorityQueue<Node> openList = new PriorityQueue<>();
        Map<State, Double> closedList = new HashMap<>();
        openList.add(new Node(initialState, null, null, 0, heuristic(initialState, goalGold, goalWood)));
        closedList.put(initialState, 0.0);
        int expandedNodes = 0;

        while (!openList.isEmpty()) {
            Node currentNode = openList.poll();
            State currentState = currentNode.getState();
            expandedNodes++;

            if (currentState.isGoal(goalGold, goalWood)) {
                System.out.println("Hedefe ulaşıldı! Toplam genişletilen düğüm: " + expandedNodes);
                return reconstructPlan(currentNode);
            }

            for (Action action : generateSensibleActions(currentState)) {
                if (action.isApplicable(currentState)) {
                    State nextState = action.apply(currentState);
                    double actionCost;

                    
                    if (action instanceof BuildPeasantAction) {
                        actionCost = 0.0;
                    } else if (action instanceof MoveAction) {
                        actionCost = mapInfo.getDistance(currentState.getPeasantLocation(), ((MoveAction) action).getToLocation());
                    } else {
                        actionCost = 1.0; 
                    }
                    
                    
                    double parallelActionCost = (currentState.getPeasantCount() > 0) ? (actionCost / currentState.getPeasantCount()) : actionCost;
                    double newG = currentNode.getG() + parallelActionCost;

                    if (closedList.containsKey(nextState) && closedList.get(nextState) <= newG) {
                        continue;
                    }

                    closedList.put(nextState, newG);
                    
                    double newH = heuristic(nextState, goalGold, goalWood);
                    openList.add(new Node(nextState, currentNode, action, newG, newH));
                }
            }
        }
        System.err.println("Planlayıcı bir çözüm bulamadı! Toplam genişletilen düğüm: " + expandedNodes);
        return null;
    }
    
    private List<Action> reconstructPlan(Node node) {
        LinkedList<Action> plan = new LinkedList<>();
        Node current = node;
        while (current.getParent() != null) {
            plan.addFirst(current.getAction());
            current = current.getParent();
        }
        return plan;
    }
}