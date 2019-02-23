package warframeRelics.screenCapture;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;

public class ScreenBufferedImageProvider implements BufferedImageProvider{

	private Robot robot;
	private ScreenResolution resolution;
	
	public ScreenBufferedImageProvider(ScreenResolution resolution) throws AWTException {
		robot = new Robot();
		this.resolution = resolution;
	}
	
	@Override
	public BufferedImage getImage() {
		return robot.createScreenCapture(new Rectangle(0, 0, resolution.getWidth(), resolution.getHeight()));
	}

	@Override
	public void setResolution(ScreenResolution resolution) {
		this.resolution = resolution;
	}

}
