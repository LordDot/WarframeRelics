package warframeRelics.screenCapture;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

public class ImageExtractor {
	
	private static final Logger log = Logger.getLogger(ImageExtractor.class.getName());
	
	private Map<Integer, Rectangle[]> coordinates;
	private Rectangle names;
	
	public ImageExtractor(ScreenResolution resolution) throws IOException {
		setResolution(resolution);
	}
	
	public void setResolution(ScreenResolution resolution) throws IOException {
		JsonParser parser = new JsonParser();
		InputStream stream = ImageExtractor.class.getResourceAsStream("relicsPositions.json");
		JsonObject root = parser.parse(new InputStreamReader(stream)).getAsJsonObject();
		stream.close();
		JsonObject info = root.get(resolution.name()).getAsJsonObject().get("full").getAsJsonObject();
		JsonObject names = info.get("names").getAsJsonObject();
		int namesX = names.get("x").getAsInt();
		int namesY = names.get("y").getAsInt();
		int namesWidth = names.get("width").getAsInt();
		int namesHeight = names.get("height").getAsInt();
		this.names = new Rectangle(namesX, namesY, namesWidth, namesHeight);
		
		coordinates = new HashMap<>();
		for(int i = 1; i < 5; i++) {
			JsonArray coordsArray = info.get(i + "Players").getAsJsonArray();
			Rectangle[] rects = new Rectangle[i];
			for(int j = 0 ; j < i; j++) {
				JsonObject coords = coordsArray.get(j).getAsJsonObject();
				int x = coords.get("x").getAsInt();
				int y = coords.get("y").getAsInt();
				int width = coords.get("width").getAsInt();
				int height = coords.get("height").getAsInt();
				rects[j] = new Rectangle(x, y, width, height);
			}
			coordinates.put(i, rects);
		}
	}
	
	public BufferedImage[] extractRelics(BufferedImage image) throws TesseractException {
		return extractRelics(image, getNubmerOfPlayers(image));
	}
	
	private int getNubmerOfPlayers(BufferedImage image) throws TesseractException {
		BufferedImage players = getSubImage(image, names);
		
		for (int j = 0; j < players.getHeight(); j++) {
			for (int k = 0; k < players.getWidth(); k++) {
				int color = players.getRGB(k, j);
				int r = (color & (255 << 16)) >> 16;
				int g = (color & (255 << 8)) >> 8;
				int b = color & 255;
				if(r < 100 && g < 100 && b < 100) {
					players.setRGB(k, j, 255 << 24);
				}else {
					players.setRGB(k,j,Integer.MAX_VALUE);
				}
			}
		}
		
		Tesseract tess = new Tesseract();
		tess.setDatapath("tessdata");
		tess.setTessVariable("load_system_dawg", "F");
		tess.setTessVariable("load_freq_dawg", "F");
		tess.setTessVariable("preserve_interword_spaces", "1");
		tess.setTessVariable("tessedit_char_whitelist", "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_-.");
		String read = tess.doOCR(players);
		int numberOfPlayers = read.split("(.?[_\\-. ]){5,}").length;
		log.info("Found " + numberOfPlayers + " players from " + read);
		return numberOfPlayers;
	}
	
	private BufferedImage[] extractRelics(BufferedImage image, int players) {
		Rectangle[] coordinates = this.coordinates.get(players);
		BufferedImage[] ret = new BufferedImage[players];
		for(int i = 0; i < players; i++) {
			ret[i] = getSubImage(image, coordinates[i]);
		}
		return ret;
	}
	
	private BufferedImage getSubImage(BufferedImage image, Rectangle area) {
		return image.getSubimage(area.x, area.y, area.width, area.height);
	}
}
