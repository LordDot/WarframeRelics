package warframeRelics.gui.settings;

import com.google.gson.*;
import warframeRelics.screenCapture.ResolutionFile;
import warframeRelics.screenCapture.ScreenResolution;

import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class SettingsLoader {

    public static Settings loadSettings(Reader in, ResolutionFile resolutionFile) {
        JsonParser parser = new JsonParser();
        JsonObject root = parser.parse(in).getAsJsonObject();
        ScreenResolution resolution = resolutionFile.getFromString(root.get("resolution").getAsString());

        JsonArray arr = root.get("priceDisplayers").getAsJsonArray();
        List<String> priceDisplayers = new ArrayList<>();
        for(JsonElement e : arr) {
            priceDisplayers.add(e.getAsString());
        }

        int readRewardsHotkey = root.get("readRewardsHotkey").getAsInt();
        float onTopTime = root.get("onTopTime").getAsFloat();

        Settings ret = new Settings(resolution, priceDisplayers, readRewardsHotkey, onTopTime);
        return ret;
    }

    public static void writeSettings(Settings settings, Writer out){
        JsonObject root = new JsonObject();
        root.addProperty("resolution", settings.getResolution().name());
        JsonArray arr = new JsonArray();
        List<String> priceDisplayers = settings.getPriceDisplayers();
        for(String s : priceDisplayers) {
            arr.add(s);
        }
        root.add("priceDisplayers", arr);
        root.addProperty("readRewardsHotkey", settings.getReadRewardsHotkey());
        root.addProperty("onTopTime", settings.getOnTopTime());
        Gson gson = new Gson();
        gson.toJson(root, out);
    }
}
