package sepia_project;

import sepia_project.planner.Action;
import sepia_project.planner.MapInfo; 
import sepia_project.planner.Planner;
import sepia_project.planner.State;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.AbstractMap.SimpleEntry;


public class PlannerTest {

    
    static class MockMapInfo extends MapInfo {
        private final Map<Map.Entry<String, String>, Integer> distanceMap = new HashMap<>();

        public MockMapInfo() {
            
            super(null, 0); 

            
            addDistance("TownHall", "GoldMine1", 5);
            addDistance("TownHall", "GoldMine2", 12);
            addDistance("TownHall", "GoldMine3", 25);
            addDistance("TownHall", "Forest1", 8);
            addDistance("TownHall", "Forest2", 10);
            addDistance("TownHall", "Forest3", 15);
        }

        private void addDistance(String loc1, String loc2, int dist) {
            distanceMap.put(new SimpleEntry<>(loc1, loc2), dist);
            distanceMap.put(new SimpleEntry<>(loc2, loc1), dist);
        }

        @Override
        public int getDistance(String from, String to) {
            if (from.equals(to)) return 0;
            return distanceMap.getOrDefault(new SimpleEntry<>(from, to), 15);
        }
        
        @Override
        public Map<String, int[]> getLocations() {
             Map<String, int[]> fakeLocations = new HashMap<>();
             fakeLocations.put("TownHall", new int[]{0,0});
             fakeLocations.put("GoldMine1", new int[]{0,0});
             fakeLocations.put("GoldMine2", new int[]{0,0});
             fakeLocations.put("GoldMine3", new int[]{0,0});
             fakeLocations.put("Forest1", new int[]{0,0});
             fakeLocations.put("Forest2", new int[]{0,0});
             fakeLocations.put("Forest3", new int[]{0,0});
             return fakeLocations;
        }
    }


    public static void main(String[] args) {
        // 1. Başlangıç Durumunu Tanımla
        Map<String, Integer> goldMines = new HashMap<>();
        goldMines.put("GoldMine1", 100);
        goldMines.put("GoldMine2", 500);
        goldMines.put("GoldMine3", 5000);

        Map<String, Integer> forests = new HashMap<>();
        forests.put("Forest1", 400);
        forests.put("Forest2", 400);
        forests.put("Forest3", 400);
        
        State initialState = new State("TownHall", goldMines, forests);
        System.out.println("Başlangıç Durumu: " + initialState);

        // 2. Hedef Durumu Tanımla
        int goalGold = 1000;
        int goalWood = 1000;
        System.out.println("Hedef: " + goalGold + " Altın, " + goalWood + " Odun");
        System.out.println("------------------------------------");

        // 3. Planlayıcıyı sahte harita bilgisiyle oluştur ve çalıştır
        MockMapInfo mockMapInfo = new MockMapInfo();
        Planner planner = new Planner(mockMapInfo); // HATA DÜZELTİLDİ
        
        System.out.println("Planlayıcı başlatılıyor...");
        long startTime = System.currentTimeMillis();
        List<Action> plan = planner.plan(initialState, goalGold, goalWood);
        long endTime = System.currentTimeMillis();
        System.out.println("Planlama " + (endTime - startTime) + " milisaniyede tamamlandı.");
        System.out.println("------------------------------------");

        // 4. Sonucu Yazdır
        if (plan != null && !plan.isEmpty()) {
            System.out.println("PLAN BULUNDU (" + plan.size() + " adım):");
            int step = 1;
            for (Action action : plan) {
                System.out.println(step + ". " + action);
                step++;
            }
        } else {
            System.err.println("HATA: Bir plan bulunamadı!");
        }
    }
}