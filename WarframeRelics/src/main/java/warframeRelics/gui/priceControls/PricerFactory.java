package warframeRelics.gui.priceControls;

import warframeRelics.dataBase.IDataBase;
import warframeRelics.pricing.WarframeMarket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PricerFactory {

    public static final String NAME = "name";
    public static final String DUCATS = "ducats";
    public static final String WARFRAME_MARKET_PRICES = "warframeMarketPrices";
    public static final String WARFRAME_MARKET_STATISTICS = "warframeMarketStatistics";

    private Map<String, Pricer> pricers;

    public PricerFactory(IDataBase database) {
        pricers = new HashMap<>();
        WarframeMarket warframeMarket = new WarframeMarket();
        pricers.put(NAME, new NamePricer(NAME, database));
        pricers.put(DUCATS, new DucatsPricer(DUCATS));
        pricers.put(WARFRAME_MARKET_PRICES, new WarframeMarketPriceWrapper(WARFRAME_MARKET_PRICES, warframeMarket));
        pricers.put(WARFRAME_MARKET_STATISTICS, new WarframeMarketStatisticsWrapper(WARFRAME_MARKET_STATISTICS, warframeMarket));
    }

    public List<Pricer> getAllPricers() {
        ArrayList<Pricer> ret = new ArrayList<>(2);
        ret.add(getNamePricer());
        ret.add(getDucatPricer());
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

    public Pricer getDucatPricer() {
        return pricers.get(DUCATS);
    }

    public Pricer get(String name) {
        return pricers.get(name);
    }
}
