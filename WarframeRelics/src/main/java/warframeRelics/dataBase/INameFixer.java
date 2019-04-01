package warframeRelics.dataBase;

import warframeRelics.beans.PrimeItem;

public interface INameFixer {
	public PrimeItem getNearestItemName(String name) throws RuntimeException;
}
