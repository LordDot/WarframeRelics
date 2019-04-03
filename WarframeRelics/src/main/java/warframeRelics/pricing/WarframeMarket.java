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

public class WarframeMarket {

    private static Logger log = Logger.getLogger(WarframeMarket.class.getName());

    private static final String API_URL = "https://api.warframe.market/v1/items/";
    private static final String ORDER_SUFFIX = "/orders";
    private static final String STATISTICS_SUFFIX = "/statistics";

    private static final int OFFLINE = 0;
    private static final int ONLINE = 1;
    private static final int INGAME = 2;

    private static final int SELL = 0;
    private static final int BUY = 1;

    public OrderInformation getOrders(String itemName) throws IOException {
        String targetItem = cleanItemName(itemName);

        String url = API_URL + targetItem + ORDER_SUFFIX;
        log.info("Searched for " + itemName + " with url " + url);

        JsonArray orders;
        try(Reader reader = new InputStreamReader(new URL(url).openStream());) {
            JsonParser parser = new JsonParser();
            orders = parser.parse(reader).getAsJsonObject().get("payload").getAsJsonObject().get("orders")
                    .getAsJsonArray();
        }

        int[][] prices = new int[3][2];
        for (int i = 0; i < 3; i++) {
            prices[i][SELL] = Integer.MAX_VALUE;
        }

        for (JsonElement e : orders) {
            JsonObject order = e.getAsJsonObject();
            if (order.get("visible").getAsBoolean()) {
                int status;

                String statusString = order.get("user").getAsJsonObject().get("status").getAsString();
                 if ("offline".equals(statusString)) {
                    status = OFFLINE;
                } else if ("online".equals(statusString)) {
                    status = ONLINE;
                } else if ("ingame".equals(statusString)) {
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
        return new OrderInformation(prices);
    }

    public Statistics getStatistics(String itemName) throws IOException {
        String url =  API_URL + cleanItemName(itemName) + STATISTICS_SUFFIX;

        JsonObject info;
        try(Reader reader = new InputStreamReader(new URL(url).openStream());){
            info = new JsonParser().parse(reader).getAsJsonObject();
        }
        JsonArray statistics = info.get("payload").getAsJsonObject().get("statistics_closed").getAsJsonObject().get("48hours").getAsJsonArray();

        int totalAmount = 0;
        int totalPrice = 0;
        for(JsonElement timePieceElement: statistics){
            JsonObject timePiece = timePieceElement.getAsJsonObject();
            int amount = timePiece.get("volume").getAsInt();
            int price = timePiece.get("avg_price").getAsInt();
            totalAmount += amount;
            totalPrice += amount * price;
        }

        return new Statistics(totalAmount, totalPrice / (float) totalAmount);
    }

    private String cleanItemName(String name) {
        name = name.toLowerCase();

        if (!(name.contains("helios") || name.contains("carrier") || name.contains("wyrm"))) {
            if (name.contains("systems") || name.contains("chassis") || name.contains("neuroptics") || name.contains("harness") || name.contains("wings")) {
                name = name.substring(0, name.length() - 10);
            }
        }

        name = name.replace(" ", "_");

        name = name.replace("_band", "_collar_band").replaceAll("_buckle", "_collar_buckle").replace("kubrow_", "");
        name = name.replace("&", "and");

        return name;
    }

    public class Statistics{
        private int amountMoved;
        private float averagePrice;

        public Statistics(int amountMoved, float averagePrice) {
            this.amountMoved = amountMoved;
            this.averagePrice = averagePrice;
        }

        public int getAmountMoved() {
            return amountMoved;
        }

        public float getAveragePrice() {
            return averagePrice;
        }
    }

    public class OrderInformation {
        private int offlineSell;
        private int onlineSell;
        private int ingameSell;
        private int offlineBuy;
        private int onlineBuy;
        private int ingameBuy;

        public OrderInformation() {

        }

        public OrderInformation(int[][] prices) {
            this();
            if (!(prices.length == 3 && prices[0].length == 2)) {
                throw new IllegalArgumentException();
            }
            offlineBuy = (prices[OFFLINE][BUY]);
            onlineBuy = (prices[ONLINE][BUY]);
            ingameBuy = (prices[INGAME][BUY]);
            offlineSell = (prices[OFFLINE][SELL]);
            onlineSell = (prices[ONLINE][SELL]);
            ingameSell = (prices[INGAME][SELL]);
        }


        public int getOfflineSell() {
            return offlineSell;
        }

        public int getOnlineSell() {
            return onlineSell;
        }

        public int getIngameSell() {
            return ingameSell;
        }

        public int getOfflineBuy() {
            return offlineBuy;
        }

        public int getOnlineBuy() {
            return onlineBuy;
        }

        public int getIngameBuy() {
            return ingameBuy;
        }

        @Override
        public String toString() {
            return "OrderInformation{" +
                    "offlineSell=" + offlineSell +
                    ", onlineSell=" + onlineSell +
                    ", ingameSell=" + ingameSell +
                    ", offlineBuy=" + offlineBuy +
                    ", onlineBuy=" + onlineBuy +
                    ", ingameBuy=" + ingameBuy +
                    '}';
        }
    }
}
