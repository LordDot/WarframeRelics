package warframeRelics.gui.priceControls;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import warframeRelics.beans.PrimeItem;
import warframeRelics.pricing.WarframeMarket;

import java.io.IOException;

public class WarframeMarketStatisticsDisplayer extends PriceDisplayer {

    private WarframeMarket market;
    @FXML
    private Label amountLabel;
    @FXML
    private Label priceLabel;

    public WarframeMarketStatisticsDisplayer(WarframeMarket market) {
        this.market = market;

        FXMLLoader loader = new FXMLLoader(getClass().getResource("WarframeMarketStatisticsDisplayer.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setPrice(PrimeItem item) {
        if(item != null && !item.equals("Forma Blueprint")) {
            WarframeMarket.Statistics statistics;
            try {
                statistics = market.getStatistics(item.getDisplayName());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            Platform.runLater(() -> {
                amountLabel.setText("" + statistics.getAmountMoved());
                priceLabel.setText("" + statistics.getAveragePrice());
            });
            setVisible(true);
        }else{
            setVisible(false);
        }
    }
}
