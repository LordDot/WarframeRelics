package warframeRelics.gui.priceControls;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import warframeRelics.dataBase.IDataBase;
import warframeRelics.pricing.WarframeMarket;

public class PricerFactory {

    public static final String NAME = "name";
    public static final String WARFRAME_MARKET_PRICES = "warframeMarketPrices";
    public static final String WARFRAME_MARKET_STATISTICS = "warframeMarketStatistics";

    private Map<String, Pricer> pricers;

    public PricerFactory(IDataBase database) {
        pricers = new HashMap<>();
        WarframeMarket warframeMarket = new WarframeMarket();
        pricers.put(NAME, new NamePricer(NAME, database));
        pricers.put(WARFRAME_MARKET_PRICES, new WarframeMarketPriceWrapper(WARFRAME_MARKET_PRICES, warframeMarket));
        pricers.put(WARFRAME_MARKET_STATISTICS, new WarframeMarketStatisticsWrapper(WARFRAME_MARKET_STATISTICS, warframeMarket));
    }

    public Collection<Pricer> getAllPricers() {
        ArrayList<Pricer> ret = new ArrayList<>(2);
        ret.add(getNamePricer());
        ret.add(getWarframeMarketPricePricer());
        ret.add(getWarframeMarketStatisticsPricer());
        return ret;
    }

    public Pricer getNamePricer() {
        return pricers.get(NAME);
    }

    public Pricer getWarframeMarketPricePricer() {
        return pricers.get(WARFRAME_MARKET_PRICES);
    }

    public Pricer getWarframeMarketStatisticsPricer() {
        return pricers.get(WARFRAME_MARKET_STATISTICS);
    }

    public Pricer get(String name) {
        return pricers.get(name);
    }
}
