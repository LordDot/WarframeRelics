package warframeRelics.screenCapture;

import java.awt.AWTException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Logger;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import warframeRelics.dataBase.INameFixer;
import warframeRelics.gui.Util;

public class RelicReader {

	private static final Logger log = Logger.getLogger(RelicReader.class.getName());

	private ImageExtractor imageExtractor;
	private BufferedImageProvider bip;
	private ITesseract tess;
	private INameFixer dataBase;

	public RelicReader(INameFixer nameFixer, BufferedImageProvider bip, ScreenResolution resolution) throws AWTException, IOException {
		this.bip = bip;
		this.dataBase = nameFixer;
		imageExtractor = new ImageExtractor(resolution);

		tess = new Tesseract();
		tess.setDatapath("tessdata");
		tess.setTessVariable("tessedit_char_whitelist", "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
		tess.setTessVariable("load_system_dawg", "F");
		tess.setTessVariable("load_freq_dawg", "F");
		tess.setTessVariable("user_words_suffix", "user-words");
	}
	
	public void setResolution(ScreenResolution resolution) throws IOException {
		imageExtractor.setResolution(resolution);
	}

	
	public String[] readRelics() throws Exception {
		// TODO Prime is always in first line
		BufferedImage[] images = imageExtractor.extractRelics(bip.getImage());

		for (int i = 0; i < images.length; i++) {
			for (int j = 0; j < images[i].getHeight(); j++) {
				for (int k = 0; k < images[i].getWidth(); k++) {
					int color = images[i].getRGB(k, j);
					int r = (color & (255 << 16)) >> 16;
					int g = (color & (255 << 8)) >> 8;
					int b = color & 255;
					int average = r + g +b /3;
					if(average < 160) {
						images[i].setRGB(k, j, 255 << 24);
					}
				}
			}
		}

		String[] ret = new String[images.length];
		for (int i = 0; i < images.length; i++) {
			String read = tess.doOCR(images[i]);
			log.info("read " + read);
			String[] split = read.split("\n");
			if (Util.stringDifference(split[split.length - 1], "BLUEPRINT") < 3) {
				ret[i] = split[split.length - 2] + split[split.length - 1];
			} else {
				ret[i] = split[split.length - 1];
			}
			ret[i] = dataBase.getNearestItemName(ret[i]);
		}
		return ret;
	}
}
