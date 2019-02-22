package warframeRelics.gui;

import java.io.IOException;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;
import warframeRelics.pricing.Pricer;
import warframeRelics.pricing.Pricer.Price;

public class PriceDisplayer extends GridPane {
	private Property<Price> priceProperty;
	
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
	
	public PriceDisplayer() {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PriceDisplayer.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);
		
		try {
			fxmlLoader.load();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		priceProperty = new SimpleObjectProperty<>();
		priceProperty.addListener(new ChangeListener<Price>() {

			@Override
			public void changed(ObservableValue<? extends Price> observable, Price oldValue, Price newValue) {
				setPrice(ingameBuy,newValue.getIngameBuy());
				setPrice(onlineBuy,newValue.getOnlineBuy());
				setPrice(offlineBuy,newValue.getOfflineBuy());
				setPrice(ingameSell,newValue.getIngameSell());
				setPrice(onlineSell,newValue.getOnlineSell());
				setPrice(offlineSell, newValue.getOfflineSell());
			}
		});
	}

	public void setPrice(Pricer.Price p) {
		priceProperty.setValue(p);
	}
	
	public Price getPrice() {
		return priceProperty.getValue();
	}
	
	public Property<Price> pricePoperty(){
		return priceProperty;
	}

	private void setPrice(Label label, int value) {
		if (value == 0 || value == Integer.MAX_VALUE) {
			label.setText("");
		} else {
			label.setText("" + value);
		}
	}
}
