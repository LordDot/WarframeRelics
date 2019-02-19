package screenCapture;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class ImageExtractor {
	private static final int RELIC_Y_POS = 429;
	private static final int RELIC_WIDTH = 416;
	private static final int RELIC_HEIGHT = 58;
	private static final int FIRST_RELIC_X_POS = 104;
	private static final int SECOND_RELIC_X_POS = 538;
	private static final int THIRD_RELIC_X_POS = 970;
	private static final int FOURTH_RELIC_X_POS = 1402;
	
	private static final Color BRONZE = new Color(140,96,46,255);
	private static final Color SILVER = new Color(174,174,174,255);
	private static final Color GOLD = new Color(173,154,80,255);
	private static final Color BLACK = new Color(0,0,0,255);
	
	private static final float MAX_COLOR_DIST = 75;
	
	public BufferedImage[] extractRelics(BufferedImage image) {
		BufferedImage[] ret = new BufferedImage[4];
		ret[0] = image.getSubimage(FIRST_RELIC_X_POS, RELIC_Y_POS, RELIC_WIDTH, RELIC_HEIGHT);
		ret[1] = image.getSubimage(SECOND_RELIC_X_POS, RELIC_Y_POS, RELIC_WIDTH, RELIC_HEIGHT);
		ret[2] = image.getSubimage(THIRD_RELIC_X_POS, RELIC_Y_POS, RELIC_WIDTH, RELIC_HEIGHT);
		ret[3] = image.getSubimage(FOURTH_RELIC_X_POS, RELIC_Y_POS, RELIC_WIDTH, RELIC_HEIGHT);
//		for(int i = 0; i < ret.length; i++) {
//			for(int y = 0; y < ret[i].getHeight(); y++) {
//				for(int x = 0; x < ret[i].getWidth(); x++) {
//					System.out.println("x: " + x + ", y: " + y);
//					int colorInt = ret[i].getRGB(x, y);
//					Color color = new Color(colorInt);
//					Color newColor = processColor(color);
//					ret[i].setRGB(x, y, newColor.getRGB());
//				}
//			}
//		}
		return ret;
	}
	
	private Color processColor(Color c) {
		if(c.getGreen() > 220 && c.getBlue() > 220 && c.getRed() > 220) {
			return BLACK;
		}
		if(colorDist(c,BRONZE)< MAX_COLOR_DIST || colorDist(c, SILVER)<MAX_COLOR_DIST || colorDist(c, GOLD)< MAX_COLOR_DIST){
			return c;
		}
		return BLACK;
	}
	
	private float colorDist(Color first, Color second) {
		float redDiff = first.getRed() - second.getRed();
		float greenDiff = first.getGreen() - second.getGreen();
		float blueDiff = first.getBlue() - second.getBlue();
		return (float) Math.sqrt(redDiff*redDiff + greenDiff*greenDiff + blueDiff*blueDiff);
	}
}
