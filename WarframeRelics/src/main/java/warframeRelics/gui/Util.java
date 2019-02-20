package warframeRelics.gui;

import java.io.IOException;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import warframeRelics.dataBase.SQLLiteDataBase;

public class Util {
	public static int stringDifference(String first, String second) {
		first = first.trim();
		second = second.trim();
		String shorter;
		String longer;
		if (first.length() < second.length()) {
			shorter = first.toLowerCase();
			longer = second.toLowerCase();
		} else {
			shorter = second.toLowerCase();
			longer = first.toLowerCase();
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
	
	public static Parent stretch(Node node) {
		AnchorPane ret = new AnchorPane();	
		AnchorPane.setTopAnchor(node, 0d);
		AnchorPane.setBottomAnchor(node, 0d);
		AnchorPane.setRightAnchor(node, 0d);
		AnchorPane.setLeftAnchor(node, 0d);
		ret.getChildren().add(node);
		return ret;
	}
}
