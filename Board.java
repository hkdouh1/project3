// and this file is for managing the game board and the list of players

import java.util.ArrayList;

public class Board {
    private final ArrayList<property> properties;
    private final ArrayList<player> players;

    public Board()
    {
        properties = new ArrayList<>();
        players = new ArrayList<>();
        setupProperties();
    }

    private void setupProperties()
    {
        properties.add(new property("GO", 0, 0));
        properties.add(new property("Mediterranean Avenue", 60, 10));
        properties.add(new property("Community Chest", 0, 0));
        properties.add(new property("Baltic Avenue", 60, 20));
        properties.add(new property("Income Tax", 0, 0));
        properties.add(new property("Reading Railroad", 200, 25));
        properties.add(new property("Oriental Avenue", 100, 30));
        properties.add(new property("Chance", 0, 0));
        properties.add(new property("Vermont Avenue", 100, 30));
        properties.add(new property("Connecticut Avenue", 120, 40));
        properties.add(new property("Jail/Just Visiting", 0, 0));
        properties.add(new property("St. Charles Place", 140, 50));
        properties.add(new property("Electric Company", 150, 20));
        properties.add(new property("States Avenue", 140, 50));
        properties.add(new property("Virginia Avenue", 160, 60));
        properties.add(new property("Pennsylvania Railroad", 200, 25));
        properties.add(new property("St. James Place", 180, 70));
        properties.add(new property("Community Chest", 0, 0));
        properties.add(new property("Tennessee Avenue", 180, 70));
        properties.add(new property("New York Avenue", 200, 80));
        properties.add(new property("Free Parking", 0, 0));
        properties.add(new property("Kentucky Avenue", 220, 90));
        properties.add(new property("Chance", 0, 0));
        properties.add(new property("Indiana Avenue", 220, 90));
        properties.add(new property("Illinois Avenue", 240, 100));
        properties.add(new property("B&O Railroad", 200, 25));
        properties.add(new property("Atlantic Avenue", 260, 110));
        properties.add(new property("Ventnor Avenue", 260, 110));
        properties.add(new property("Water Works", 150, 20));
        properties.add(new property("Marvin Gardens", 280, 120));
        properties.add(new property("Go To Jail", 0, 0));
        properties.add(new property("Pacific Avenue", 300, 130));
        properties.add(new property("North Carolina Avenue", 300, 130));
        properties.add(new property("Community Chest", 0, 0));
        properties.add(new property("Pennsylvania Avenue", 320, 150));
        properties.add(new property("Short Line", 200, 25));
        properties.add(new property("Chance", 0, 0));
        properties.add(new property("Park Place", 350, 175));
        properties.add(new property("Luxury Tax", 0, 0));
        properties.add(new property("Boardwalk", 400, 200));

        System.out.println("DEBUG: Board initialized with " + properties.size() + " spaces");
    }

    public void addPlayer(player player)
    {
        players.add(player);
    }

    public ArrayList<property> getProperties()
    {
        return properties;
    }

    public ArrayList<player> getPlayers()
    {
        return players;
    }

    public property getPropertyAt(int location)
    {
        if (location >= 0 && location < properties.size()) {
            return properties.get(location);
        }
        System.out.println("WARNING: Invalid location " + location + ", returning GO");
        return properties.get(0);
    }

    public boolean ownsFullSet(player plr, String setName) {
        ArrayList<String> colorGroup = new ArrayList<>();

        switch (setName) {
            case "Brown":
                colorGroup.add("Mediterranean Avenue");
                colorGroup.add("Baltic Avenue");
                break;
            case "Light Blue":
                colorGroup.add("Oriental Avenue");
                colorGroup.add("Vermont Avenue");
                colorGroup.add("Connecticut Avenue");
                break;
            case "Pink":
                colorGroup.add("St. Charles Place");
                colorGroup.add("States Avenue");
                colorGroup.add("Virginia Avenue");
                break;
            case "Orange":
                colorGroup.add("St. James Place");
                colorGroup.add("Tennessee Avenue");
                colorGroup.add("New York Avenue");
                break;
            case "Red":
                colorGroup.add("Kentucky Avenue");
                colorGroup.add("Indiana Avenue");
                colorGroup.add("Illinois Avenue");
                break;
            case "Yellow":
                colorGroup.add("Atlantic Avenue");
                colorGroup.add("Ventnor Avenue");
                colorGroup.add("Marvin Gardens");
                break;
            case "Green":
                colorGroup.add("Pacific Avenue");
                colorGroup.add("North Carolina Avenue");
                colorGroup.add("Pennsylvania Avenue");
                break;
            case "Dark Blue":
                colorGroup.add("Park Place");
                colorGroup.add("Boardwalk");
                break;
            case "Railroad":
                colorGroup.add("Reading Railroad");
                colorGroup.add("Pennsylvania Railroad");
                colorGroup.add("B&O Railroad");
                colorGroup.add("Short Line");
                break;
            case "Utility":
                colorGroup.add("Electric Company");
                colorGroup.add("Water Works");
                break;
            default:
                return false;
        }

        for (String name : colorGroup) {
            boolean has = false;
            for (property prop : properties) {
                if (prop.getName().equals(name) && prop.getOwner().equals(plr.getName())) {
                    has = true;
                    break;
                }
            }
            if (!has) return false;
        }
        return true;
    }

    public ArrayList<property> getPropertiesByOwner(String owner) {
        ArrayList<property> list = new ArrayList<>();
        for (property p : properties)
            if (p.getOwner().equals(owner)) list.add(p);
        return list;
    }
}