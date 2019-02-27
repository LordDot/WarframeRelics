package warframeRelics.gui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import warframeRelics.pricing.WarframeMarket;

public class WarframeMarketWrapper extends Pricer{

	private WarframeMarket market;
	
	public WarframeMarketWrapper() {
		this.market = new WarframeMarket();
	}
	
	
	
	@Override
	public String getName() {
		return "Warframe Market";
	}

	@Override
	public Node getHeader() {
		Label ret =  new Label("Warframe Market\n         Prices");
		ret.setAlignment(Pos.BOTTOM_CENTER);
		ret.setFont(new Font("System", 19));
		return Util.stretch(ret);
	}

	@Override
	public PriceDisplayer getPriceDisplayer() {
		return new WarframeMarketPriceDisplayer(market);
	}

	@Override
	public double getColumnWidth() {
		return 75;
	}

}
