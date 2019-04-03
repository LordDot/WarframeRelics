package warframeRelics.gui.settings;

import com.google.gson.*;
import warframeRelics.gui.WarframeRelics;
import warframeRelics.screenCapture.ResolutionFile;
import warframeRelics.screenCapture.ScreenResolution;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class SettingsLoader {

    public static Settings loadSettings(Reader in, ResolutionFile resolutionFile) throws IOException {
        JsonParser parser = new JsonParser();
        JsonObject root = parser.parse(in).getAsJsonObject();

        String version = root.get("version").getAsString();
        if (version.equals("2.0.0.0")) {
            ScreenResolution resolution = resolutionFile.getFromString(root.get("resolution").getAsString());

            JsonArray arr = root.get("priceDisplayers").getAsJsonArray();
            List<String> priceDisplayers = new ArrayList<>();
            for (JsonElement e : arr) {
                priceDisplayers.add(e.getAsString());
            }

            int readRewardsHotkey = root.get("readRewardsHotkey").getAsInt();
            float onTopTime = root.get("onTopTime").getAsFloat();

            Settings ret = new Settings(resolution, priceDisplayers, readRewardsHotkey, onTopTime);
            return ret;
        } else {
            throw new IOException("Unknown Settings Version");
        }
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
        root.addProperty("version", WarframeRelics.VERSION);
        Gson gson = new Gson();
        gson.toJson(root, out);
    }
}
