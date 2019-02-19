package gui;

import java.io.IOException;

import dataBase.SQLLiteDataBase;

public class Util {
	public static int stringDifference(String first, String second) {
		String shorter;
		String longer;
		if (first.length() < second.length()) {
			shorter = first.toLowerCase().trim();
			longer = second.toLowerCase().trim();
		} else {
			shorter = second.toLowerCase().trim();
			longer = first.toLowerCase().trim();
		}

		int diff = longer.length() - shorter.length();
		for (int i = 0; i < shorter.length(); i++) {
			if (shorter.charAt(i) != longer.charAt(i)) {
				diff++;
			}
		}
		return diff;
	}
	
	public static int convertFissureNameToInt(String tierName) throws IOException {
		int tier;
		if (tierName.equals("Lith")) {
			tier = SQLLiteDataBase.LITH;
		} else if (tierName.equals("Meso")) {
			tier = SQLLiteDataBase.MESO;
		} else if (tierName.equals("Neo")) {
			tier = SQLLiteDataBase.NEO;
		} else if (tierName.equals("Axi")) {
			tier = SQLLiteDataBase.AXI;
		} else {
			throw new IOException("Unknown relicType");
		}
		return tier;
	}
}
