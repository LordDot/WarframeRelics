package warframeRelics.screenCapture;

public class ScreenResolution {
	public static final ScreenResolution S1920x1080 = new ScreenResolution("S1920x1080", 1920, 1080);
	public static final ScreenResolution S3840x2160 = new ScreenResolution("S3840x2160", 3840, 2160);

	private int width;
	private int height;
	private String name;

	private ScreenResolution(String name, int width, int height) {
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

	public static ScreenResolution[] values() {
		return new ScreenResolution[] { S1920x1080, S3840x2160 };
	}

	public static ScreenResolution valueOf(String string) {
		ScreenResolution[] vals = values();
		for(int i = 0; i < vals.length; i++) {
			if(vals[i].name().equals(string)) {
				return vals[i];
			}
		}
		throw new IllegalArgumentException();
	}
}
