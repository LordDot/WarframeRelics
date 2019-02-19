package gui;

import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;

import pricing.Pricer;

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
		labels[0].setText("" + p.getIngameBuy());
		labels[1].setText("" + p.getOnlineBuy());
		labels[2].setText("" + p.getOfflineBuy());
		labels[3].setText("" + p.getIngameSell());
		labels[4].setText("" + p.getOnlineSell());
		labels[5].setText("" + p.getOfflineSell());
	}
}
