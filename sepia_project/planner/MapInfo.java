package sepia_project.planner;

import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;
import edu.cwru.sepia.environment.model.state.State.StateView;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;
import java.util.HashMap;
import java.util.Map;


public class MapInfo {

    private final Map<String, int[]> locations = new HashMap<>();

    
    public MapInfo(StateView stateView, int playerNum) {
        
        for (UnitView unit : stateView.getUnits(playerNum)) {
            if (unit.getTemplateView().getName().equals("TownHall")) {
                locations.put("TownHall", new int[]{unit.getXPosition(), unit.getYPosition()});
                break; 
            }
        }

        
        int goldMineCount = 1;
        int forestCount = 1;
        for (ResourceView resource : stateView.getAllResourceNodes()) {
            if (resource.getType() == ResourceNode.Type.GOLD_MINE) {
                String name = "GoldMine" + goldMineCount++;
                locations.put(name, new int[]{resource.getXPosition(), resource.getYPosition()});
            } else if (resource.getType() == ResourceNode.Type.TREE) {
                String name = "Forest" + forestCount++;
                locations.put(name, new int[]{resource.getXPosition(), resource.getYPosition()});
            }
        }
    }

    
    public int getDistance(String from, String to) {
        if (!locations.containsKey(from) || !locations.containsKey(to)) {
            return 15; 
        int[] fromCoords = locations.get(from);
        int[] toCoords = locations.get(to);
        
        return Math.max(Math.abs(fromCoords[0] - toCoords[0]), Math.abs(fromCoords[1] - toCoords[1]));
    }

    
    public Map<String, int[]> getLocations() {
        return locations;
    }
}