package pricerTest.warframeMarketTest;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.junit.Test;

import warframeRelics.dataBase.SQLLiteDataBase;
import warframeRelics.pricing.WarframeMarket;

public class WarframeMarketTest {

	//@Test
	public void testItemNameCoversion() throws SQLException, IOException {
		List<String> names = null;
		try(SQLLiteDataBase db = new SQLLiteDataBase("db.db");){
			names = db.getAllItems();
		}
		WarframeMarket wm = new WarframeMarket();
		for(String name : names) {
			if(!name.equals("Forma Blueprint"))
			try {
				wm.getPlat(name);
			} catch (IOException e) {
				throw new IOException("For name " + name, e);
			}
		}
	}
}
