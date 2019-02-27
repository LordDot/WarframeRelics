package warframeRelics.gui;

import java.sql.SQLException;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import warframeRelics.dataBase.IDataBase;

public class NamePricer extends Pricer{

	private IDataBase database;
	
	public NamePricer(IDataBase database) {
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



	private class LabelPriceDisplayer extends PriceDisplayer{
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
		public void setPrice(String itemName) {
			String text;
			if(itemName == null) {
				text = "";
			}else {
				text = itemName;
				try {
					if(database.getItemVaulted(itemName)) {
						text += " (v)";
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			String labelText = text;
			Platform.runLater(()->label.setText(labelText));
		}
		
	}
}
