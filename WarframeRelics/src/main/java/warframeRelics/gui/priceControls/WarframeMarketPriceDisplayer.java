package warframeRelics.gui.priceControls;

import java.io.IOException;

import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import warframeRelics.beans.PrimeItem;
import warframeRelics.pricing.WarframeMarket;
import warframeRelics.pricing.WarframeMarket.OrderInformation;

public class WarframeMarketPriceDisplayer extends PriceDisplayer {
	private Property<WarframeMarket.OrderInformation> priceProperty;

	@FXML
	private Label ingameBuy;
	@FXML
	private Label onlineBuy;
	@FXML
	private Label offlineBuy;
	@FXML
	private Label ingameSell;
	@FXML
	private Label onlineSell;
	@FXML
	private Label offlineSell;

	private WarframeMarket pricer;

	public WarframeMarketPriceDisplayer(WarframeMarket pricer) {
		this.pricer = pricer;
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("WarframeMarketPriceDisplayer.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try {
			fxmlLoader.load();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		priceProperty = new SimpleObjectProperty<>();
		priceProperty.addListener(new ChangeListener<OrderInformation>() {

			@Override
			public void changed(ObservableValue<? extends OrderInformation> observable, OrderInformation oldValue, OrderInformation newValue) {
				if (newValue == null) {
					Platform.runLater(() -> {
						ingameBuy.setText("");
						onlineBuy.setText("");
						offlineBuy.setText("");
						ingameSell.setText("");
						onlineSell.setText("");
						offlineSell.setText("");
					});
				} else {
					setPrice(ingameBuy, newValue.getIngameBuy());
					setPrice(onlineBuy, newValue.getOnlineBuy());
					setPrice(offlineBuy, newValue.getOfflineBuy());
					setPrice(ingameSell, newValue.getIngameSell());
					setPrice(onlineSell, newValue.getOnlineSell());
					setPrice(offlineSell, newValue.getOfflineSell());
				}
			}
		});
	}

	public void setPrice(OrderInformation p) {
		priceProperty.setValue(p);
	}

	public OrderInformation getPrice() {
		return priceProperty.getValue();
	}

	public Property<WarframeMarket.OrderInformation> pricePoperty() {
		return priceProperty;
	}

	private void setPrice(Label label, int value) {
		String text;
		if (!(value == 0 || value == Integer.MAX_VALUE)) {
			text = "" + value;
		} else {
			text = "";
		}
		Platform.runLater(() -> label.setText(text));
	}

	@Override
	public void setPrice(PrimeItem item) {
		try {
			if (item != null && !item.equals("Forma Blueprint")) {
				setPrice(pricer.getOrders(item.getDisplayName()));
			} else {
				setPrice((OrderInformation) null);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
