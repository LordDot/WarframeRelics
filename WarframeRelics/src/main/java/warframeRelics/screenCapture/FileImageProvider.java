package warframeRelics.screenCapture;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

public class FileImageProvider implements BufferedImageProvider{

	private BufferedImage image;
	
	public FileImageProvider(InputStream input) throws IOException {
		image = ImageIO.read(input);
		input.close();
	}
	
	@Override
	public BufferedImage getImage() {
		return image;
	}

	@Override
	public void setResolution(ScreenResolution resolution) {
		
	}

}
