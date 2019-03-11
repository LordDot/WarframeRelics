package warframeRelics.gui;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import warframeRelics.gui.priceControls.PricerFactory;

public class SettingsFile {
	private String resolution;
	private List<String> priceDisplayers;
	
	public SettingsFile(Reader in) {
		JsonParser parser = new JsonParser();
		JsonObject root = parser.parse(in).getAsJsonObject();
		resolution = root.get("resolution").getAsString();
		JsonArray arr = root.get("priceDisplayers").getAsJsonArray();
		priceDisplayers = new ArrayList<>();
		for(JsonElement e : arr) {
			priceDisplayers.add(e.getAsString());
		}
	}
	
	public SettingsFile() {
		resolution = "1920x1080";
		priceDisplayers = new ArrayList<>();
		priceDisplayers.add(PricerFactory.NAME);
		priceDisplayers.add(PricerFactory.WARFRAME_MARKET);
	}

	public String getResolution() {
		return resolution;
	}

	public void setResolution(String resolution) {
		this.resolution = resolution;
	}

	public List<String> getPriceDisplayers() {
		return priceDisplayers;
	}

	public void setPriceDisplayers(List<String> priceDisplayers) {
		this.priceDisplayers = priceDisplayers;
	}

	public void writeTo(Writer out) {
		JsonObject root = new JsonObject();
		root.addProperty("resolution", resolution);
		JsonArray arr = new JsonArray();
		for(String s : priceDisplayers) {
			arr.add(s);
		}
		root.add("priceDisplayers", arr);
		Gson gson = new Gson();
		gson.toJson(root, out);
	}
	
}
