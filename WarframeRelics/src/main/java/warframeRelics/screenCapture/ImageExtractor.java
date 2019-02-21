package warframeRelics.screenCapture;

import java.awt.image.BufferedImage;

public class ImageExtractor {
	private static final int RELIC_Y_POS = 429;
	private static final int RELIC_WIDTH = 416;
	private static final int RELIC_HEIGHT = 58;
	private static final int FIRST_RELIC_X_POS = 104;
	private static final int SECOND_RELIC_X_POS = 538;
	private static final int THIRD_RELIC_X_POS = 970;
	private static final int FOURTH_RELIC_X_POS = 1402;
	
	public BufferedImage[] extractRelics(BufferedImage image) {
		BufferedImage[] ret = new BufferedImage[4];
		ret[0] = image.getSubimage(FIRST_RELIC_X_POS, RELIC_Y_POS, RELIC_WIDTH, RELIC_HEIGHT);
		ret[1] = image.getSubimage(SECOND_RELIC_X_POS, RELIC_Y_POS, RELIC_WIDTH, RELIC_HEIGHT);
		ret[2] = image.getSubimage(THIRD_RELIC_X_POS, RELIC_Y_POS, RELIC_WIDTH, RELIC_HEIGHT);
		ret[3] = image.getSubimage(FOURTH_RELIC_X_POS, RELIC_Y_POS, RELIC_WIDTH, RELIC_HEIGHT);
		return ret;
	}
}
