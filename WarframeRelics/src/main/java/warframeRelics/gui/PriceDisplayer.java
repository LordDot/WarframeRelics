package warframeRelics.gui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import warframeRelics.pricing.Pricer;

public class PriceDisplayer extends GridPane {
	private Label[][] labels;

	public PriceDisplayer() {
		labels = new Label[2][3];
		
		ColumnConstraints c = new ColumnConstraints();
		c.setPercentWidth(50);
		
		for (int i = 0; i < labels[0].length; i++) {
			getColumnConstraints().add(c);
		}
		RowConstraints r = new RowConstraints();
		r.setPercentHeight(50);
		for (int i = 0; i < 2; i++) {
			getRowConstraints().add(r);
		}
		for (int i = 0; i < labels.length; i++) {
			for (int j = 0; j < 3; j++) {
				labels[i][j] = new Label();
				labels[i][j].setAlignment(Pos.CENTER);
				add(Util.stretch(labels[i][j]), j, i);
			}
		}
	}

	public void setPrices(Pricer.Price p) {
		if (p == null) {
			for (int i = 0; i < labels.length; i++) {
				for (int j = 0; j < labels[0].length; j++) {
					labels[i][j].setText("");
				}
			}
			return;
		}
		setPrice(labels[0][0], p.getIngameBuy());
		setPrice(labels[0][1], p.getOnlineBuy());
		setPrice(labels[0][2], p.getOfflineBuy());
		setPrice(labels[1][0], p.getIngameSell());
		setPrice(labels[1][1], p.getOnlineSell());
		setPrice(labels[1][2], p.getOfflineSell());
	}

	private void setPrice(Label label, int value) {
		if (value == 0 || value == Integer.MAX_VALUE) {
			label.setText("");
		} else {
			label.setText("" + value);
		}
	}
}
