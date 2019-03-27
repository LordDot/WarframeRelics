package warframeRelics.gui;

import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.jnativehook.keyboard.NativeKeyEvent;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import warframeRelics.gui.priceControls.PricerFactory;

public class SettingsFile {
	private String resolution;
	private List<String> priceDisplayers;
	private int readRewardsHotkey;
	private float onTopTime;
	
	public SettingsFile(Reader in) {
		JsonParser parser = new JsonParser();
		JsonObject root = parser.parse(in).getAsJsonObject();
		resolution = root.get("resolution").getAsString();
		JsonArray arr = root.get("priceDisplayers").getAsJsonArray();
		priceDisplayers = new ArrayList<>();
		for(JsonElement e : arr) {
			priceDisplayers.add(e.getAsString());
		}
		
		readRewardsHotkey = root.get("readRewardsHotkey").getAsInt();
		onTopTime = root.get("onTopTime").getAsFloat();
	}
	
	public SettingsFile() {
		resolution = "1920x1080";
		priceDisplayers = new ArrayList<>();
		priceDisplayers.add(PricerFactory.NAME);
		priceDisplayers.add(PricerFactory.WARFRAME_MARKET);
		readRewardsHotkey = NativeKeyEvent.VC_K;
		onTopTime = 5;
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
		root.addProperty("readRewardsHotkey", readRewardsHotkey);
		root.addProperty("onTopTime", onTopTime);
		Gson gson = new Gson();
		gson.toJson(root, out);
	}

	public int getReadRewardsHotkey() {
		return readRewardsHotkey;
	}
	
	public float getOnTopTime() {
		return onTopTime;
	}

	public void setReadRewardsHotkey(int readRewardsHotkey) {
		this.readRewardsHotkey = readRewardsHotkey;
	}

	public void setOnTopTime(float onTopTime) {
		this.onTopTime = onTopTime;
	}
	
	
}
