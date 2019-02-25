package warframeRelics.screenCapture;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ResolutionFile {

	private Map<String, ScreenResolution> resolutions;
	
	public ResolutionFile(InputStream in) {
		resolutions = new HashMap<>();
		JsonParser parser = new JsonParser();
		JsonObject root = parser.parse(new InputStreamReader(in)).getAsJsonObject();
		for(String s : root.keySet()) {
			String[] split = s.split("x");
			int width = Integer.parseInt(split[0]);
			int height = Integer.parseInt(split[1]);
			resolutions.put(s,new ScreenResolution(s, width, height));
		}
	}
	
	public ScreenResolution getFromString(String s) {
		return resolutions.get(s);
	}
	
	public Collection<ScreenResolution> getResolutions(){
		return resolutions.values();
	}
}
