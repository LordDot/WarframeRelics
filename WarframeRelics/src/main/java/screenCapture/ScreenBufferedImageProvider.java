package screenCapture;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;

public class ScreenBufferedImageProvider implements BufferedImageProvider{

	private Robot robot;
	
	public ScreenBufferedImageProvider() throws AWTException {
		robot = new Robot();
	}
	
	@Override
	public BufferedImage getImage() {
		return robot.createScreenCapture(new Rectangle(0, 0, 1920, 1080));
	}

}
