package warframeRelics.gui;

import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

public abstract class PriceDisplayer extends AnchorPane{
	protected abstract void setPrice(String itemName);
}
