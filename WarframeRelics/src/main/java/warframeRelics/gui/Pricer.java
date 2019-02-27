package warframeRelics.gui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.text.Font;

public abstract class Pricer {
	public String getName() {
		return "";
	}
	
	public Node getHeader() {
		Label ret = new Label();
		ret.setAlignment(Pos.BOTTOM_CENTER);
		ret.setText(getName());
		ret.setFont(new Font("System", 19));
		return ret;
	}
	
	public double getColumnWidth() {
		return 50;
	}
	
	public abstract PriceDisplayer getPriceDisplayer();
}
