package warframeRelics.gui.priceControls;

import java.sql.SQLException;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import warframeRelics.beans.PrimeItem;
import warframeRelics.dataBase.IDataBase;

public class NamePricer extends Pricer {

    private IDataBase database;

    NamePricer(IDataBase database) {
        this.database = database;
    }

    @Override
    public PriceDisplayer getPriceDisplayer() {
        return new LabelPriceDisplayer(database);
    }

    @Override
    public String getName() {
        return "Name";
    }


    @Override
    public double getColumnWidth() {
        return 100;
    }


    private class LabelPriceDisplayer extends PriceDisplayer {
        private IDataBase database;
        private Label label;

        public LabelPriceDisplayer(IDataBase database) {
            this.database = database;
            label = new Label();
            label.setAlignment(Pos.CENTER_RIGHT);
            AnchorPane.setBottomAnchor(label, 0d);
            AnchorPane.setTopAnchor(label, 0d);
            AnchorPane.setRightAnchor(label, 0d);
            AnchorPane.setLeftAnchor(label, 0d);
            getChildren().add(label);
        }

        @Override
        public void setPrice(PrimeItem item) {
            String text;
            if (item == null) {
                text = "";
            } else {
                text = item.getDisplayName();
                if (item.isVaulted()) {
                    text += " (v)";
                }
            }
            String labelText = text;
            Platform.runLater(() -> label.setText(labelText));
        }

    }
}
