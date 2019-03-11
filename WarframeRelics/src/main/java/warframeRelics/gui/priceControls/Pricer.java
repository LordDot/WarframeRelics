package warframeRelics.gui.priceControls;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import warframeRelics.gui.Util;

public abstract class Pricer {
	public abstract String getName();
	
	public Node getHeader() {
		Label ret = new Label();
		ret.setAlignment(Pos.BOTTOM_CENTER);
		ret.setText(getName());
		ret.setFont(new Font("System", 19));
		return Util.stretch(ret);
	}
	
	public double getColumnWidth() {
		return 50;
	}
	
	public abstract PriceDisplayer getPriceDisplayer();
}
