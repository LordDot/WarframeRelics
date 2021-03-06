package warframeRelics.gui.priceControls;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import warframeRelics.gui.Util;
import warframeRelics.pricing.WarframeMarket;

public class WarframeMarketPriceWrapper extends Pricer{

	private WarframeMarket market;

	WarframeMarketPriceWrapper(String id) {
		super(id);
		this.market = new WarframeMarket();
	}
	
	
	
	@Override
	public String getName() {
		return "Warframe Market Prices";
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
