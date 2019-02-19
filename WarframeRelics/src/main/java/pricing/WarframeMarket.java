package pricing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.function.Consumer;
import java.util.logging.Logger;

import org.omg.PortableInterceptor.IORInterceptor_3_0Holder;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class WarframeMarket extends Pricer {

	private static Logger log = Logger.getLogger(WarframeMarket.class.getName());

	@Override
	public Price getPlat(String itemName) throws MalformedURLException, IOException {
		String targetItem = removeBlueprint(itemName.toLowerCase()).replace(" ", "_");

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
			if (name.contains("systems") || name.contains("chassis") || name.contains("neuroptics")) {
				name = name.substring(0, name.length() - 10);
			}
		}
		return name;
	}
}
