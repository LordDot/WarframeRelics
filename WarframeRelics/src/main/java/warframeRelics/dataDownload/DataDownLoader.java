package warframeRelics.dataDownload;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import warframeRelics.dataBase.IDataBase;
import warframeRelics.dataBase.SQLLiteDataBase;
import warframeRelics.gui.Util;

public class DataDownLoader {
    private static Logger log = Logger.getLogger(DataDownLoader.class.getName());

    private static final String WARFRAME_DROP_DATA = "https://drops.warframestat.us/data/relics.json";
    private static final String[] WARFRAME_ITEMS = {"https://raw.githubusercontent.com/WFCD/warframe-items/development/data/json/Primary.json", "https://raw.githubusercontent.com/WFCD/warframe-items/development/data/json/Archwing.json", "https://raw.githubusercontent.com/WFCD/warframe-items/development/data/json/Melee.json", "https://raw.githubusercontent.com/WFCD/warframe-items/development/data/json/Secondary.json", "https://raw.githubusercontent.com/WFCD/warframe-items/development/data/json/Sentinels.json", "https://raw.githubusercontent.com/WFCD/warframe-items/development/data/json/Warframes.json"};

    private IDataBase database;

    public DataDownLoader(IDataBase database) {
        this.database = database;
    }

    public Set<String> downloadData() throws IOException, SQLException {
        Set<String> ret = new HashSet<>();
        List<String> displayNames = new ArrayList<>();


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
                    log.info(itemName);
                }
            }
        }

        log.info(displayNames.toString());

        for(int i = 0; i < WARFRAME_ITEMS.length; i++){

            URLConnection warframeItems = new URL(WARFRAME_ITEMS[i]).openConnection();

            JsonArray itemsList = null;

            try (InputStreamReader ipsr = new InputStreamReader(warframeItems.getInputStream());) {
                JsonParser parser = new JsonParser();
                itemsList = parser.parse(ipsr).getAsJsonArray();
            }

            for (JsonElement e : itemsList) {
                JsonObject equipment = e.getAsJsonObject();
                String equipmentName = equipment.get("name").getAsString();
                if(equipmentName.toLowerCase().contains("prime")){
                    JsonArray components = equipment.get("components").getAsJsonArray();
                    for(JsonElement componentElement : components){
                        JsonObject component = componentElement.getAsJsonObject();
                        String componentName = component.get("name").getAsString();
                        if(!componentName.toLowerCase().contains("cell") && !componentName.toLowerCase().contains("extract")){
                            if(!(equipmentName.startsWith("Ak") && componentName.toLowerCase().contains(equipmentName.split(" ")[0].substring(2)))){
                                String displayNameGuess = equipmentName + " " + componentName;
                                String displayName = null;
                                for(String s: displayNames){
                                    if(displayNameGuess.equals(s)){
                                        displayName = displayNameGuess;
                                        break;
                                    }else if(s.equals(displayNameGuess + " Blueprint")){
                                        displayName = displayNameGuess + " Blueprint";
                                        break;
                                    }
                                }
                                if(displayName == null){
                                    throw new RuntimeException("could not find displayname for item " + displayNameGuess);
                                }

                                String uniqueName = component.get("uniqueName").getAsString();
                                boolean vaulted = equipment.get("vaulted").getAsString().equals(true);
                                database.addItem(uniqueName, displayName, vaulted);
                            }
                        }
                    }
                }
            }
        }

        return ret;
    }
}
