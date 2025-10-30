package sepia_project;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.ActionType;
import edu.cwru.sepia.action.RawAction;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.environment.model.history.History.HistoryView;
import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;
import edu.cwru.sepia.environment.model.state.ResourceType;
import edu.cwru.sepia.environment.model.state.State.StateView;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;
import edu.cwru.sepia.util.Direction;
import sepia_project.planner.MapInfo;
import sepia_project.planner.Mission;
import sepia_project.planner.Planner;
import sepia_project.planner.actions.MoveAction;


public class MidasAgent extends Agent {
    private static final long serialVersionUID = 1L;
    private List<sepia_project.planner.Action> plan = null;
    private List<Integer> peasantIDs = new ArrayList<>();
    private Integer townhallID;
    private int peasantTemplateID;
    private List<Mission> activeMissions = new ArrayList<>();
    private Map<String, int[]> locationCoords = new HashMap<>();
    private int requiredGold;
    private int requiredWood;

    public MidasAgent(int playerNum, String[] args) { 
        super(playerNum); 
        if (args.length == 2) { 
            this.requiredGold = Integer.parseInt(args[0]); 
            this.requiredWood = Integer.parseInt(args[1]); 
        } else { 
            this.requiredGold = 1000; 
            this.requiredWood = 1000; 
        } 
    }
    
    @Override 
    public Map<Integer, Action> initialStep(StateView stateView, HistoryView historyView) { 
        findInitialUnitsAndTemplates(stateView); 
        MapInfo mapInfo = new MapInfo(stateView, playernum); 
        sepia_project.planner.State initialState = createInitialPlannerState(stateView); 
        System.out.println("Goal: " + requiredGold + " Gold, " + requiredWood + " Wood"); 
        Planner planner = new Planner(mapInfo); 
        plan = planner.plan(initialState, requiredGold, requiredWood); 
        if (plan != null) System.out.println("Optimal plan found (" + plan.size() + " steps)"); 
        else System.err.println("ERROR: No plan could be found!"); 
        return new HashMap<>(); 
    }
    
    
    @Override 
    public Map<Integer, Action> middleStep(StateView stateView, HistoryView historyView) { 
        Map<Integer, Action> actions = new HashMap<>(); 
        updatePeasantList(stateView); 
        removeCompletedMissions(stateView); 
        assignNewMissions(stateView); 
        handleGlobalActions(actions, stateView); 
        
        
        Map<Integer, Action> peasantActions = collectPlannedActions(stateView);
        
        Map<String, List<Integer>> plannedMoves = new HashMap<>();
        Set<Integer> peasantsToExecute = new HashSet<>();
        
        for (Map.Entry<Integer, Action> entry : peasantActions.entrySet()) {
            Integer peasantID = entry.getKey();
            Action action = entry.getValue();

            if (action instanceof RawAction && ((RawAction)action).getType() == ActionType.PRIMITIVE_MOVE) {
                int[] nextCoords = getNextMoveCoordinates(stateView.getUnit(peasantID), (RawAction)action);
                String targetKey = nextCoords[0] + "," + nextCoords[1];
                
                plannedMoves.computeIfAbsent(targetKey, k -> new ArrayList<>()).add(peasantID);
            }
        }
        
        
        for (List<Integer> conflictingPeasants : plannedMoves.values()) {
            if (conflictingPeasants.size() >= 1) {
                
                peasantsToExecute.add(conflictingPeasants.get(0));
            }
        }

        
        for (Integer peasantID : peasantIDs) {
            Action plannedAction = peasantActions.get(peasantID);
            
            if (plannedAction != null && (plannedAction instanceof RawAction) && ((RawAction)plannedAction).getType() == ActionType.PRIMITIVE_MOVE) {
                 if (peasantsToExecute.contains(peasantID)) {
                    actions.put(peasantID, plannedAction); 
                } 
            } else if (plannedAction != null) {
                
                actions.put(peasantID, plannedAction);
            }
        }
        
        return actions; 
    }
    
    @Override 
    public void terminalStep(StateView stateView, HistoryView historyView) { 
        System.out.println("Total Steps (Makespan): " + stateView.getTurnNumber()); 
    }

    
    private int[] getEffectiveResourceCounts(StateView stateView) { 
        int effectiveGold = stateView.getResourceAmount(playernum, ResourceType.GOLD); 
        int effectiveWood = stateView.getResourceAmount(playernum, ResourceType.WOOD); 
        for (Integer peasantID : peasantIDs) { 
            UnitView peasant = stateView.getUnit(peasantID); 
            if (peasant != null && peasant.getCargoAmount() > 0) { 
                if (peasant.getCargoType().equals(ResourceType.GOLD)) { 
                    effectiveGold += peasant.getCargoAmount(); 
                } else if (peasant.getCargoType().equals(ResourceType.WOOD)) { 
                    effectiveWood += peasant.getCargoAmount(); 
                } 
            } 
        } 
        return new int[]{effectiveGold, effectiveWood}; 
    }

    
    private void assignNewMissions(StateView stateView) { 
        Set<Integer> busyPeasants = getBusyPeasantIDs(); 
        int[] effectiveResources = getEffectiveResourceCounts(stateView); 
        for (Integer peasantID : peasantIDs) { 
            if (!busyPeasants.contains(peasantID)) { 
                List<sepia_project.planner.Action> missionSteps = extractNextAvailableMission(); 
                if (!missionSteps.isEmpty()) { 
                    boolean isGoldMission = false, isWoodMission = false; 
                    for (sepia_project.planner.Action step : missionSteps) { 
                        if (step.toString().contains("Gold")) { isGoldMission = true; break; } 
                        if (step.toString().contains("Wood")) { isWoodMission = true; break; } 
                    } 
                    boolean assignMission = true; 
                    if (isGoldMission && effectiveResources[0] >= requiredGold) { 
                        assignMission = false; 
                    } 
                    if (isWoodMission && effectiveResources[1] >= requiredWood) { 
                        assignMission = false; 
                    } 
                    if (assignMission) { 
                        Mission newMission = new Mission(peasantID, missionSteps); 
                        activeMissions.add(newMission); 
                    } 
                } 
            } 
        } 
    }

    
    private Map<Integer, Action> collectPlannedActions(StateView stateView) {
        Map<Integer, Action> plannedActions = new HashMap<>();
        
        for (Mission mission : activeMissions) {
            UnitView peasant = stateView.getUnit(mission.getPeasantID());
            
            if (peasant != null && peasant.getCurrentDurativeAction() == null && !mission.isComplete()) {
                sepia_project.planner.Action currentStep = mission.getCurrentStep();
                
                if (checkStepCompletion(peasant, currentStep)) {
                    mission.advance();
                    if (mission.isComplete()) continue;
                    currentStep = mission.getCurrentStep();
                }
                
                Action sepiaAction = createSepiaActionForTask(currentStep, stateView, peasant);
                if (sepiaAction != null) {
                    plannedActions.put(peasant.getID(), sepiaAction);
                }
            }
        }
        return plannedActions;
    }

    
    private Action createSepiaActionForTask(sepia_project.planner.Action task, StateView state, UnitView peasant) {
        String taskString = task.toString();

        if (task instanceof MoveAction) {
            String targetLocation = ((MoveAction) task).getToLocation();
            return getSmartMoveAction(peasant, locationCoords.get(targetLocation), state);
        } 
        
        else if (taskString.startsWith("HARVEST")) {
            String targetResource = taskString.split(" ")[1];
            int[] targetCoords = locationCoords.get(targetResource);

           
            if (isAdjacent(peasant.getXPosition(), peasant.getYPosition(), targetCoords[0], targetCoords[1])) {
                Direction dir = getDirection(peasant.getXPosition(), peasant.getYPosition(), targetCoords[0], targetCoords[1]);
                return Action.createPrimitiveGather(peasant.getID(), dir);
            } else {
                
                return getSmartMoveAction(peasant, targetCoords, state);
            }
        } 
        
        else if (taskString.startsWith("DEPOSIT")) {
            int[] townhallCoords = locationCoords.get("TownHall");

            
            if (isAdjacent(peasant.getXPosition(), peasant.getYPosition(), townhallCoords[0], townhallCoords[1])) {
                Direction dir = getDirection(peasant.getXPosition(), peasant.getYPosition(), townhallCoords[0], townhallCoords[1]);
                return Action.createPrimitiveDeposit(peasant.getID(), dir);
            } else {
                
                return getSmartMoveAction(peasant, townhallCoords, state);
            }
        }
        return null;
    }
    
    
    private int[] getNextMoveCoordinates(UnitView peasant, RawAction moveAction) {
        
        return new int[]{peasant.getXPosition(), peasant.getYPosition()}; 
    }

    private void removeCompletedMissions(StateView stateView) { List<Mission> completedMissions = new ArrayList<>(); for (Mission mission : activeMissions) { UnitView peasant = stateView.getUnit(mission.getPeasantID()); if (mission.isComplete() || peasant == null) { completedMissions.add(mission); } } activeMissions.removeAll(completedMissions); }
    private List<sepia_project.planner.Action> extractNextAvailableMission() { ListIterator<sepia_project.planner.Action> iterator = plan.listIterator(); while(iterator.hasNext()) { sepia_project.planner.Action potentialFirstStep = iterator.next(); if (potentialFirstStep instanceof MoveAction) { String target = getTargetFromAction(potentialFirstStep); if (target != null) { int startIndex = iterator.previousIndex(); int endIndex = findEndOfMission(startIndex); if (endIndex != -1) { List<sepia_project.planner.Action> missionSteps = new ArrayList<>(plan.subList(startIndex, endIndex + 1)); plan.subList(startIndex, endIndex + 1).clear(); return missionSteps; } } } } return new ArrayList<>(); }
    private Action getSmartMoveAction(UnitView peasant, int[] targetCoords, StateView state) { Direction idealDirection = getDirection(peasant.getXPosition(), peasant.getYPosition(), targetCoords[0], targetCoords[1]); if (idealDirection == null) { return null; } int nextX = peasant.getXPosition() + idealDirection.xComponent(); int nextY = peasant.getYPosition() + idealDirection.yComponent(); if (!isOccupied(nextX, nextY, state, peasant.getID())) { return Action.createPrimitiveMove(peasant.getID(), idealDirection); } for (Direction alternative : getAlternativeDirections(idealDirection)) { nextX = peasant.getXPosition() + alternative.xComponent(); nextY = peasant.getYPosition() + alternative.yComponent(); if (!isOccupied(nextX, nextY, state, peasant.getID())) { return Action.createPrimitiveMove(peasant.getID(), alternative); } } return null; }
    private int findEndOfMission(int startIndex) { for (int i = startIndex; i < plan.size(); i++) { if (plan.get(i).toString().startsWith("DEPOSIT")) return i; } return -1; }
    private String getTargetFromAction(sepia_project.planner.Action action) { String actionStr = action.toString(); if (actionStr.startsWith("MOVE") || actionStr.startsWith("HARVEST")) { String target = actionStr.split(" ")[1]; if (!target.equals("TownHall")) return target; } return null; }
    private void handleGlobalActions(Map<Integer, Action> actions, StateView stateView) { if (!plan.isEmpty() && plan.get(0).toString().startsWith("BUILD_PEASANT")) { UnitView townhall = stateView.getUnit(townhallID); if(townhall != null && townhall.getCurrentDurativeAction() == null) { actions.put(townhallID, Action.createPrimitiveProduction(townhallID, peasantTemplateID)); plan.remove(0); } } }
    private Set<Integer> getBusyPeasantIDs() { Set<Integer> busyPeasants = new HashSet<>(); for (Mission mission : activeMissions) busyPeasants.add(mission.getPeasantID()); return busyPeasants; }
    private boolean checkStepCompletion(UnitView peasant, sepia_project.planner.Action step) { if (step instanceof MoveAction) { String target = ((MoveAction) step).getToLocation(); return isAdjacent(peasant.getXPosition(), peasant.getYPosition(), locationCoords.get(target)[0], locationCoords.get(target)[1]); } return true; }
    private void updatePeasantList(StateView stateView) { List<Integer> currentPeasants = new ArrayList<>(); for (UnitView unit : stateView.getUnits(playernum)) { if (unit.getTemplateView().getID() == peasantTemplateID) currentPeasants.add(unit.getID()); } if (currentPeasants.size() > peasantIDs.size()) peasantIDs = currentPeasants; }
    private boolean isAdjacent(int x1, int y1, int x2, int y2) { return Math.abs(x1 - x2) <= 1 && Math.abs(y1 - y2) <= 1; }
    private boolean isOccupied(int x, int y, StateView state, int movingPeasantId) { if (state.isResourceAt(x, y)) return true; Integer unitIdAtTarget = state.unitAt(x, y); if (unitIdAtTarget != null) { if (peasantIDs.contains(unitIdAtTarget) && unitIdAtTarget != movingPeasantId) return true; if (unitIdAtTarget.equals(townhallID)) return true; } return false; }
    private Direction[] getAlternativeDirections(Direction ideal) { switch (ideal) { case NORTH: return new Direction[]{Direction.NORTHEAST, Direction.NORTHWEST}; case SOUTH: return new Direction[]{Direction.SOUTHEAST, Direction.SOUTHWEST}; case EAST: return new Direction[]{Direction.NORTHEAST, Direction.SOUTHEAST}; case WEST: return new Direction[]{Direction.NORTHWEST, Direction.SOUTHWEST}; case NORTHEAST: return new Direction[]{Direction.NORTH, Direction.EAST}; case NORTHWEST: return new Direction[]{Direction.NORTH, Direction.WEST}; case SOUTHEAST: return new Direction[]{Direction.SOUTH, Direction.EAST}; case SOUTHWEST: return new Direction[]{Direction.SOUTH, Direction.WEST}; default: return new Direction[]{}; } }
    private Direction getDirection(int unitX, int unitY, int targetX, int targetY) { int dx = targetX - unitX; int dy = targetY - unitY; if (dx == 0 && dy == 0) return null; return Direction.getDirection(Integer.compare(dx, 0), Integer.compare(dy, 0)); }
    private sepia_project.planner.State createInitialPlannerState(StateView stateView) { Map<String, Integer> goldMines = new HashMap<>(); Map<String, Integer> forests = new HashMap<>(); int goldMineCount = 1; int forestCount = 1; for (ResourceView resource : stateView.getAllResourceNodes()) { if (resource.getType() == ResourceNode.Type.GOLD_MINE) goldMines.put("GoldMine" + goldMineCount++, resource.getAmountRemaining()); else if (resource.getType() == ResourceNode.Type.TREE) forests.put("Forest" + forestCount++, resource.getAmountRemaining()); } return new sepia_project.planner.State("TownHall", goldMines, forests); }
    private void findInitialUnitsAndTemplates(StateView stateView) { for (UnitView unit : stateView.getUnits(playernum)) { String unitTypeName = unit.getTemplateView().getName(); if (unitTypeName.equals("Peasant")) { if (!peasantIDs.contains(unit.getID())) peasantIDs.add(unit.getID()); peasantTemplateID = unit.getTemplateView().getID(); } else if (unitTypeName.equals("TownHall")) { townhallID = unit.getID(); locationCoords.put("TownHall", new int[]{unit.getXPosition(), unit.getYPosition()}); } } int goldMineCount = 1; int forestCount = 1; for (ResourceView resource : stateView.getAllResourceNodes()) { String name; if (resource.getType() == ResourceNode.Type.GOLD_MINE) name = "GoldMine" + goldMineCount++; else name = "Forest" + forestCount++; locationCoords.put(name, new int[]{resource.getXPosition(), resource.getYPosition()}); } }
    @Override public void loadPlayerData(InputStream is) {}
    @Override public void savePlayerData(OutputStream os) {}
}