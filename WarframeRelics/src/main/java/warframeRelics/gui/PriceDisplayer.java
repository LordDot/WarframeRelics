package warframeRelics.gui;

import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;

import warframeRelics.pricing.Pricer;

public class PriceDisplayer extends Panel{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1370426689489086638L;
	private Label[] labels;
	
	public PriceDisplayer() {
		super(new GridLayout(2, 3));
		labels = new Label[6];
		for(int i = 0; i < 6; i++) {
			labels[i] = new Label();
			add(labels[i]);
		}
	}
	
	public void setPrices(Pricer.Price p) {
		if(p == null) {
			for(int i = 0; i < labels.length; i++) {
				labels[i].setText("");
			}
			return;
		}
		setPrice(labels[0], p.getIngameBuy());
		setPrice(labels[1], p.getOnlineBuy());
		setPrice(labels[2], p.getOfflineBuy());
		setPrice(labels[3], p.getIngameSell());
		setPrice(labels[4], p.getOnlineSell());
		setPrice(labels[5], p.getOfflineSell());
	}
	
	private void setPrice(Label label, int value) {
		if(value == 0 || value == Integer.MAX_VALUE) {
			label.setText("");
		}else {
			label.setText("" + value);
		}
	}
}
