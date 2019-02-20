package warframeRelics.screenCapture;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import warframeRelics.dataBase.IDataBase;
import warframeRelics.dataBase.INameFixer;
import warframeRelics.gui.Util;

public class RelicReader {

	private static final Logger log = Logger.getLogger(RelicReader.class.getName());
	
	private ImageExtractor imageExtractor;
	private BufferedImageProvider bip;
	private ITesseract tess;
	private INameFixer dataBase;

	public RelicReader(INameFixer dataBase, BufferedImageProvider bip) throws AWTException {
		this.dataBase = dataBase;
		this.bip = bip;
		imageExtractor = new ImageExtractor();

		tess = new Tesseract();
		tess.setDatapath("tessdata");
		tess.setTessVariable("tessedit_char_whitelist", "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
		tess.setTessVariable("load_system_dawg", "F");
		tess.setTessVariable("load_freq_dawg", "F");
		tess.setTessVariable("user_words_suffix", "user-words");
	}

	public String[] readRelics() throws Exception {
		//TODO Prime is always in first line
		BufferedImage[] images = imageExtractor.extractRelics(bip.getImage());
		String[] ret = new String[images.length];
		for (int i = 0; i < images.length; i++) {
			String read = tess.doOCR(images[i]);
			log.info("read " + read);
			String[] split = read .split("\n");
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
