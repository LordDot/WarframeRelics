package warframeRelics.pricing;

import java.io.IOException;
import java.net.MalformedURLException;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public abstract class Pricer {

	protected static final int OFFLINE = 0;
	protected static final int ONLINE = 1;
	protected static final int INGAME = 2;

	protected static final int SELL = 0;
	protected static final int BUY = 1;

	public abstract Price getPlat(String itemName) throws MalformedURLException, IOException;

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
