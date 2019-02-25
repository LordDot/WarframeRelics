package warframeRelics.gui;

import javafx.scene.Node;

public abstract class Pricer {
	public abstract Node getHeader();
	public abstract PriceDisplayer getPriceDisplayer();
	public abstract double getColumnWidth();
}
