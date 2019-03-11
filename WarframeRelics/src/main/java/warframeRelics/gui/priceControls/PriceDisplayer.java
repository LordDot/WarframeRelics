package warframeRelics.gui.priceControls;

import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

public abstract class PriceDisplayer extends AnchorPane{
	public abstract void setPrice(String itemName);
}
