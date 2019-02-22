package screenCaptureTest;

import java.awt.AWTException;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.junit.Test;

import static org.junit.Assert.*;

import warframeRelics.dataBase.SQLLiteDataBase;
import warframeRelics.screenCapture.BufferedImageProvider;
import warframeRelics.screenCapture.RelicReader;
import warframeRelics.screenCapture.ScreenResolution;

public class ImageTest {

	@Test
	public void test4Players() throws Exception {
		test(4, "fourPlayers", ImageTest.class.getResourceAsStream("fourPlayers/infos.txt"));
	}

	@Test
	public void test3Players() throws Exception {
		test(3, "threePlayers", ImageTest.class.getResourceAsStream("threePlayers/infos.txt"));
	}
	
	@Test
	public void test2Players() throws Exception {
		test(2, "twoPlayers",ImageTest.class.getResourceAsStream("twoPlayers/infos.txt"));
	}
	
	@Test
	public void test1Player() throws Exception {
		test(1, "onePlayer", ImageTest.class.getResourceAsStream("onePlayer/infos.txt"));
	}
	
	private void test(int players, String prefix, InputStream data) throws Exception {
		Map<String, String[]> images = getData(players, data);
		ImageProvider p = new ImageProvider(prefix);
		try (SQLLiteDataBase db = new SQLLiteDataBase("db.db");) {
			RelicReader r = new RelicReader(db, p, ScreenResolution.S1920x1080);
			for (String image : images.keySet()) {
				System.out.println(image);
				p.setPath(image);
				String[] readNames = r.readRelics();
				assertArrayEquals("At " + image, images.get(image), readNames);
			}
		}
	}

	private Map<String, String[]> getData(int players, InputStream data) {
		Map<String, String[]> ret = new HashMap<>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(data));) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] split = line.split(",");
				String key = split[0].trim();
				String[] values = new String[players];
				for (int i = 0; i < values.length; i++) {
					values[i] = split[i + 1].trim();
				}
				ret.put(key, values);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ret;
	}

	public class ImageProvider implements BufferedImageProvider {

		private String path;
		private String prefix;

		public ImageProvider(String prefix) {
			this.prefix = prefix;
		}

		public void setPath(String path) {
			this.path = path;
		}

		@Override
		public BufferedImage getImage() {
			try {
				return ImageIO.read(ImageTest.class.getResourceAsStream(prefix + "/" + path));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

	}
}
