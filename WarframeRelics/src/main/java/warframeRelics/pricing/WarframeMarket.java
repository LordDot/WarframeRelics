package warframeRelics.pricing;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class WarframeMarket{

	private static Logger log = Logger.getLogger(WarframeMarket.class.getName());

	private static final int OFFLINE = 0;
	private static final int ONLINE = 1;
	private static final int INGAME = 2;

	private static final int SELL = 0;
	private static final int BUY = 1;
	
	public Price getPlat(String itemName) throws MalformedURLException, IOException {
		String targetItem = removeBlueprint(itemName.toLowerCase()).replace(" ", "_");

		targetItem = targetItem.replace("_band", "_collar_band").replaceAll("_buckle", "_collar_buckle").replace("kubrow_", "");
		targetItem = targetItem.replace("&", "and");
		
		String url = "https://api.warframe.market/v1/items/" + targetItem + "/orders";
		log.info("Searched for " + itemName + " with url " + url);

		Reader reader = new InputStreamReader(new URL(url).openStream());
		JsonParser parser = new JsonParser();
		JsonArray orders = parser.parse(reader).getAsJsonObject().get("payload").getAsJsonObject().get("orders")
				.getAsJsonArray();

		int[][] prices = new int[3][2];
		for (int i = 0; i < 3; i++) {
			prices[i][SELL] = Integer.MAX_VALUE;
		}

		for (JsonElement e : orders) {
			JsonObject order = e.getAsJsonObject();
			if (order.get("visible").getAsBoolean()) {
				int status;

				String statusString = order.get("user").getAsJsonObject().get("status").getAsString();
				if (statusString.equals("offline")) {
					status = OFFLINE;
				} else if (statusString.equals("online")) {
					status = ONLINE;
				} else if (statusString.equals("ingame")) {
					status = INGAME;
				} else {
					throw new IOException("Unknown status");
				}

				int price = order.get("platinum").getAsInt();
				String orderType = order.get("order_type").getAsString();
				if (orderType.equals("buy")) {
					if (prices[status][BUY] < price) {
						prices[status][BUY] = price;
					}
				} else if (orderType.equals("sell")) {
					if (prices[status][SELL] > price) {
						prices[status][SELL] = price;
					}
				} else {
					throw new IOException("Unknown orderType");
				}
			}
		}
		return new Price(prices);
	}

	private String removeBlueprint(String name) {
		if (!(name.contains("helios") || name.contains("carrier") || name.contains("wyrm"))) {
			if (name.contains("systems") || name.contains("chassis") || name.contains("neuroptics") || name.contains("harness") || name.contains("wings")) {
				name = name.substring(0, name.length() - 10);
			}
		}
		return name;
	}
	
	public class Price {
		private IntegerProperty offlineSell;
		private IntegerProperty onlineSell;
		private IntegerProperty ingameSell;
		private IntegerProperty offlineBuy;
		private IntegerProperty onlineBuy;
		private IntegerProperty ingameBuy;

		public Price() {
			offlineSell = new SimpleIntegerProperty();
			onlineSell = new SimpleIntegerProperty();
			ingameSell = new SimpleIntegerProperty();
			offlineBuy = new SimpleIntegerProperty();
			onlineBuy = new SimpleIntegerProperty();
			ingameBuy = new SimpleIntegerProperty();
		}
		
		public Price(int[][] prices) {
			this();
			if(!(prices.length == 3 && prices[0].length == 2)) {
				throw new IllegalArgumentException();
			}
			offlineBuy.set(prices[OFFLINE][BUY]);
			onlineBuy.set(prices[ONLINE][BUY]);
			ingameBuy.set(prices[INGAME][BUY]);
			offlineSell.set(prices[OFFLINE][SELL]);
			onlineSell.set(prices[ONLINE][SELL]);
			ingameSell.set(prices[INGAME][SELL]);
		}
		

		public int getOfflineSell() {
			return offlineSell.get();
		}

		public int getOnlineSell() {
			return onlineSell.get();
		}

		public int getIngameSell() {
			return ingameSell.get();
		}

		public int getOfflineBuy() {
			return offlineBuy.get();
		}

		public int getOnlineBuy() {
			return onlineBuy.get();
		}

		public int getIngameBuy() {
			return ingameBuy.get();
		}
		
		@Override
		public String toString() {
			return "Price [offlineSell=" + offlineSell.get() + ", onlineSell=" + onlineSell.get() + ", ingameSell=" + ingameSell.get()
					+ ", offlineBuy=" + offlineBuy.get() + ", onlineBuy=" + onlineBuy.get() + ", ingameBuy=" + ingameBuy.get() + "]";
		}
	}
}
