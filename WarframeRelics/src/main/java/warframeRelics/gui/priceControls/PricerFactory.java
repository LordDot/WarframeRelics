package warframeRelics.gui.priceControls;

import java.util.HashMap;
import java.util.Map;

import warframeRelics.dataBase.IDataBase;

public class PricerFactory {

	public static final String NAME = "name";
	public static final String WARFRAME_MARKET = "warframeMarket";

	private Map<String, Pricer> pricers;

	public PricerFactory(IDataBase database) {
		pricers = new HashMap<>();
		pricers.put(NAME, new NamePricer(database));
		pricers.put(WARFRAME_MARKET, new WarframeMarketWrapper());
	}

	public Pricer getNamePricer() {
		return pricers.get(NAME);
	}

	public Pricer getWarframeMarketPricer() {
		return pricers.get(WARFRAME_MARKET);
	}

	public Pricer get(String name) {
		return pricers.get(name);
	}
}
