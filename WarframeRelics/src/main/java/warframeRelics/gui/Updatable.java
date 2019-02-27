package warframeRelics.gui;

import com.jfoenix.controls.JFXSpinner;

import javafx.scene.Node;
import javafx.scene.layout.StackPane;

public class Updatable<T extends Node> extends StackPane{

	private JFXSpinner spinner;
	private T node;
	private boolean updating;
	
	public Updatable(T node){
		this.node = node;
		getChildren().add(node);
		spinner = new JFXSpinner();
		spinner.setMaxSize(40, 40);
		getChildren().add(spinner);
		setUpdating(false);
	}
	
	public T getNode() {
		return node;
	}

	public boolean isUpdating() {
		return updating;
	}

	public void setUpdating(boolean updating) {
		this.updating = updating;
		node.setDisable(updating);
		spinner.setVisible(updating);
	}
}
