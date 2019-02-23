package warframeRelics.screenCapture;

import java.awt.image.BufferedImage;


public interface BufferedImageProvider {
	public BufferedImage getImage();
	public void setResolution(ScreenResolution resolution);
}
