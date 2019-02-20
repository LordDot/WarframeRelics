package warframeRelics.screenCapture;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;

import org.apache.log4j.Logger;

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
	}

	public String[] readRelics() throws Exception {
		//TODO Prime is always in first line
		BufferedImage[] images = imageExtractor.extractRelics(bip.getImage());
		String[] ret = new String[images.length];
		log.info("read " + ret[0] + ", " + ret[1] + ", " + ret[2] + ", " + ret[3]);
		for (int i = 0; i < images.length; i++) {
			String[] split = tess.doOCR(images[i]).split("\n");
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
