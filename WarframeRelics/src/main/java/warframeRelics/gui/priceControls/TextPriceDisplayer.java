package warframeRelics.gui.priceControls;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import warframeRelics.beans.PrimeItem;

import java.util.function.Function;

public class TextPriceDisplayer extends PriceDisplayer {
    private Function<PrimeItem, String> converter;
    private Label label;

    public TextPriceDisplayer(Function<PrimeItem, String> converter) {
        this.converter = converter;

        label = new Label();
        label.setAlignment(Pos.CENTER);
        AnchorPane.setBottomAnchor(label, 0d);
        AnchorPane.setTopAnchor(label, 0d);
        AnchorPane.setRightAnchor(label, 0d);
        AnchorPane.setLeftAnchor(label, 0d);
        getChildren().add(label);
    }

    @Override
    public void setPrice(PrimeItem item) {
        String text = "";
        if (item != null) {
            text = converter.apply(item);
        }
        String labelText = text;
        Platform.runLater(() -> label.setText(labelText));
    }

}
