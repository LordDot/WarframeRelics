package warframeRelics.screenCapture;

public class ScreenResolution {

	private int width;
	private int height;
	private String name;

	public ScreenResolution(String name, int width, int height) {
		this.width = width;
		this.height = height;
		this.name = name;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public String name() {
		return name;
	}
}
