package warframeRelics.dataDownload;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import warframeRelics.dataBase.IDataBase;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

public class DataDownLoader {
    private static Logger log = Logger.getLogger(DataDownLoader.class.getName());

    private static final String WARFRAME_DROP_DATA = "https://drops.warframestat.us/data/relics.json";
    private static final String[] WARFRAME_ITEMS = {"https://raw.githubusercontent.com/WFCD/warframe-items/development/data/json/Primary.json", "https://raw.githubusercontent.com/WFCD/warframe-items/development/data/json/Archwing.json", "https://raw.githubusercontent.com/WFCD/warframe-items/development/data/json/Melee.json", "https://raw.githubusercontent.com/WFCD/warframe-items/development/data/json/Secondary.json", "https://raw.githubusercontent.com/WFCD/warframe-items/development/data/json/Sentinels.json", "https://raw.githubusercontent.com/WFCD/warframe-items/development/data/json/Warframes.json"};
    private static final String WARFRAME_ITEMS_ALL = "https://raw.githubusercontent.com/WFCD/warframe-items/development/data/json/All.json";

    private IDataBase database;

    public DataDownLoader(IDataBase database) {
        this.database = database;
    }

    public Set<String> downloadData() throws IOException, SQLException {
        Set<String> ret = new HashSet<>();
        List<String> displayNames = new ArrayList<>();

        log.info("pulling data form warframe-drop-data");
        URLConnection warframeDropData = new URL(WARFRAME_DROP_DATA).openConnection();
        warframeDropData.addRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
        JsonArray dropDataList = null;

        try (InputStreamReader ipsr = new InputStreamReader(warframeDropData.getInputStream());) {
            JsonParser parser = new JsonParser();
            dropDataList = parser.parse(ipsr).getAsJsonObject().get("relics").getAsJsonArray();
        }

        for(JsonElement e : dropDataList){
            JsonObject o = e.getAsJsonObject();
            if(o.get("state").getAsString().equals("Intact")){
                JsonArray rewards = o.get("rewards").getAsJsonArray();
                for(JsonElement rewardelement : rewards){
                    JsonObject reward = rewardelement.getAsJsonObject();
                    String itemName = reward.get("itemName").getAsString();
                    displayNames.add(itemName);
                    ret.addAll(Arrays.asList(itemName.split(" ")));
                }
            }
        }


        log.info("pulling data from warframe-items");
        for(int i = 0; i < WARFRAME_ITEMS.length; i++){

            URLConnection warframeItems = new URL(WARFRAME_ITEMS[i]).openConnection();

            JsonArray itemsList = null;

            try (InputStreamReader ipsr = new InputStreamReader(warframeItems.getInputStream());) {
                JsonParser parser = new JsonParser();
                itemsList = parser.parse(ipsr).getAsJsonArray();
            }

            for (JsonElement e : itemsList) {
                JsonObject equipment = e.getAsJsonObject();
                handleEquipmentItem(displayNames, equipment);
            }
        }

        URLConnection warframeItems = new URL(WARFRAME_ITEMS_ALL).openConnection();

        JsonArray objects = null;
        try (InputStreamReader ipsr = new InputStreamReader(warframeItems.getInputStream());) {
            JsonParser parser = new JsonParser();
            objects = parser.parse(ipsr).getAsJsonArray();
        }
        for(JsonElement e: objects){
            JsonObject o = e.getAsJsonObject();
            if(o.has("name") && o.get("name").getAsString().equals("Kavasa Prime Kubrow Collar")){
                JsonArray components = o.get("components").getAsJsonArray();
                for(JsonElement componentElement: components){
                    JsonObject component = componentElement.getAsJsonObject();
                    String name = component.get("name").getAsString();
                    String nameGuess;
                    if(name.equals("Blueprint")){
                        nameGuess = "Kavasa Prime Kubrow Collar Blueprint";
                    }else{
                        nameGuess = name;
                    }
                    String displayName = null;
                    for(String s: displayNames){
                        if(nameGuess.equals(s)){
                            displayName = nameGuess;
                            break;
                        }
                    }
                    if(displayName == null){
                        throw new RuntimeException("could not find displayname for item " + nameGuess);
                    }
                    String uniqueName = component.get("uniqueName").getAsString();
                    boolean vaulted = true;
                    if (component.has("vaulted")) {
                        vaulted = o.get("vaulted").getAsString().equals(true);
                    }
                    int ducats = 45;
                    if (name.equals("Kavasa Prime Buckle")) {
                        ducats = 65;
                    }
                    database.addItem(uniqueName, displayName, vaulted, ducats);
                }
            }
        }

        return ret;
    }

    private void handleEquipmentItem(List<String> displayNames, JsonObject equipment) throws SQLException {
        String equipmentName = equipment.get("name").getAsString();
        log.info("Handling " + equipmentName);
        if(equipmentName.toLowerCase().contains("prime")){
            if(equipment.has("components")) {
                JsonArray components = equipment.get("components").getAsJsonArray();
                for (JsonElement componentElement : components) {
                    JsonObject component = componentElement.getAsJsonObject();
                    String componentName = component.get("name").getAsString();
                    if (!componentName.toLowerCase().contains("cell") && !componentName.toLowerCase().contains("extract")) {
                        if (!(equipmentName.startsWith("Ak") && componentName.toLowerCase().contains(equipmentName.split(" ")[0].substring(2)))) {
                            String displayNameGuess = equipmentName + " " + componentName;
                            String displayName = null;
                            for (String s : displayNames) {
                                if (displayNameGuess.equals(s)) {
                                    displayName = displayNameGuess;
                                    break;
                                } else if (s.equals(displayNameGuess + " Blueprint")) {
                                    displayName = displayNameGuess + " Blueprint";
                                    break;
                                }
                            }
                            if (displayName == null) {
                                throw new RuntimeException("could not find displayname for item " + displayNameGuess);
                            }

                            String uniqueName = component.get("uniqueName").getAsString();
                            boolean vaulted = true;
                            if (component.has("vaulted")) {
                                vaulted = equipment.get("vaulted").getAsString().equals(true);
                            }
                            int ducats = component.get("ducats").getAsInt();
                            database.addItem(uniqueName, displayName, vaulted, ducats);
                        }
                    }
                }
            }
        }
    }
}
