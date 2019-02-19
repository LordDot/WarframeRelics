package pricing;

import java.io.IOException;
import java.net.MalformedURLException;

public abstract class Pricer {

	protected static final int OFFLINE = 0;
	protected static final int ONLINE = 1;
	protected static final int INGAME = 2;

	protected static final int SELL = 0;
	protected static final int BUY = 1;

	public abstract Price getPlat(String itemName) throws MalformedURLException, IOException;

	public class Price {
		private int offlineSell;
		private int onlineSell;
		private int ingameSell;
		private int offlineBuy;
		private int onlineBuy;
		private int ingameBuy;

		public Price(int[][] prices) {
			if(!(prices.length == 3 && prices[0].length == 2)) {
				throw new IllegalArgumentException();
			}
			offlineBuy = prices[OFFLINE][BUY];
			onlineBuy = prices[ONLINE][BUY];
			ingameBuy = prices[INGAME][BUY];
			offlineSell = prices[OFFLINE][SELL];
			onlineSell = prices[ONLINE][SELL];
			ingameSell = prices[INGAME][SELL];
		}
		
		public Price(int offlineSell, int onlineSell, int ingameSell, int offlineBuy, int onlineBuy, int ingameBuy) {
			super();
			this.offlineSell = offlineSell;
			this.onlineSell = onlineSell;
			this.ingameSell = ingameSell;
			this.offlineBuy = offlineBuy;
			this.onlineBuy = onlineBuy;
			this.ingameBuy = ingameBuy;
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
			return "Price [offlineSell=" + offlineSell + ", onlineSell=" + onlineSell + ", ingameSell=" + ingameSell
					+ ", offlineBuy=" + offlineBuy + ", onlineBuy=" + onlineBuy + ", ingameBuy=" + ingameBuy + "]";
		}
	}
}
