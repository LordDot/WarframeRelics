package warframeRelics.gui.priceControls;

import javafx.scene.layout.AnchorPane;
import warframeRelics.beans.PrimeItem;

public abstract class PriceDisplayer extends AnchorPane{
	public abstract void setPrice(PrimeItem item);
}
