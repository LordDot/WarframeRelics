package warframeRelics.gui.priceControls;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import warframeRelics.gui.Util;
import warframeRelics.pricing.WarframeMarket;

public class WarframeMarketStatisticsWrapper extends Pricer {
    private WarframeMarket pricer;

    public WarframeMarketStatisticsWrapper(String id) {
        super(id);
        this.pricer = new WarframeMarket();
    }

    @Override
    public String getName() {
        return "Warframe Market Statistics";
    }

    @Override
    public PriceDisplayer getPriceDisplayer() {
        return new WarframeMarketStatisticsDisplayer(pricer);
    }

    @Override
    public Node getHeader() {
        Label ret =  new Label("Warframe Market\n        Statistics");
        ret.setAlignment(Pos.BOTTOM_CENTER);
        ret.setFont(new Font("System", 19));
        return Util.stretch(ret);
    }

    @Override
    public double getColumnWidth() {
        return 75;
    }
}
