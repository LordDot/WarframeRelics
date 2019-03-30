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
		
		int[][] distances = new int[first.length() + 1][second.length() + 1];
		for(int i = 1; i < distances.length; i++) {
			distances[i][0] = i;
		}
		
		for(int i = 1; i < distances[0].length; i++) {
			distances[0][i] = i;
		}
		
		char[] firstString = first.toCharArray();
		char[] secondString = second.toCharArray();
		for(int j = 1; j < distances[0].length; j++) {
			for(int i = 1; i < distances.length; i++) {
				int cost = 0;
				if(firstString[i-1] != secondString[j-1]) {
					cost = 1;
				}
				distances[i][j] = Math.min(distances[i-1][j] + 1, Math.min(distances[i][j-1] + 1, distances[i-1][j-1]+cost));
			}
		}
		return distances[first.length()][second.length()];
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
