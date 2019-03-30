package warframeRelics.gui;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Logger;

import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialog.DialogTransition;
import com.jfoenix.controls.JFXSpinner;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import warframeRelics.dataBase.IDataBase;
import warframeRelics.dataDownload.DataDownLoader;
import warframeRelics.gui.priceControls.Pricer;
import warframeRelics.screenCapture.ResolutionFile;
import warframeRelics.screenCapture.ScreenResolution;

public class SettingsDialog implements Initializable ,NativeKeyListener{

	private static final Logger log = Logger.getLogger(SettingsDialog.class.getName());

	private Stage stage;
	private Scene scene;
	private boolean accepted;
	private IDataBase database;

	@FXML
	private JFXComboBox<ScreenResolution> resolutionComboBox;
	@FXML
	private JFXCheckBox warframeMarketCheckBox;
	private JFXDialog loadingDialog;
	private JFXDialog pressAKeyDialog;
	@FXML
	private StackPane stackPane;
	@FXML
	private VBox pricerVBox;
	@FXML
	private JFXButton hotkeyButton;
	@FXML
	private Spinner<Double> timeOnTopSpinner;
	private Map<JFXCheckBox, Pricer> pricerCheckBoxes;
	private int hotkey;

	public SettingsDialog(Stage parentStage, IDataBase database, ResolutionFile resolutionFile) throws IOException {
		this.database = database;
		stage = new Stage();
		accepted = false;

		FXMLLoader loader = new FXMLLoader(getClass().getResource("SettingsDialog.fxml"));
		loader.setController(this);
		Parent root = loader.load();
		scene = new Scene(root);
		stage.setScene(scene);
		stage.sizeToScene();
		stage.setResizable(false);
		stage.initOwner(parentStage);
		stage.initModality(Modality.APPLICATION_MODAL);

		resolutionComboBox.getItems().addAll(resolutionFile.getResolutions());
		resolutionComboBox.setConverter(new StringConverter<ScreenResolution>() {
			@Override
			public ScreenResolution fromString(String arg0) {
				return resolutionFile.getFromString(arg0);
			}

			@Override
			public String toString(ScreenResolution arg0) {
				return arg0.name();
			}
		});
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		StackPane stackPane = new StackPane();
		stackPane.setPrefSize(175, 125);
		JFXSpinner spinner = new JFXSpinner();
		spinner.setMaxSize(75, 75);
		stackPane.getChildren().add(spinner);
		loadingDialog = new JFXDialog(this.stackPane, stackPane, DialogTransition.CENTER);
		loadingDialog.setOverlayClose(false);
		Label label = new Label("Press A Key");
		label.setFont(new Font("System", 16));
		label.setPrefSize(175, 125);
		label.setAlignment(Pos.CENTER);
		pressAKeyDialog = new JFXDialog(this.stackPane, label, DialogTransition.CENTER);
		pressAKeyDialog.setOverlayClose(false);
	}

	public void setPricers(Map<Pricer, Boolean> pricers) {
		pricerCheckBoxes = new HashMap<>();
		for(Pricer p : pricers.keySet()) {
			JFXCheckBox cb = new JFXCheckBox();
			cb.setText(p.getName());
			cb.setSelected(pricers.get(p));
			pricerVBox.getChildren().add(cb);
			pricerCheckBoxes.put(cb, p);
		}
		stage.sizeToScene();
	}
	
	public List<Pricer> getSelectedPricers(){
		List<Pricer> ret = new ArrayList<>();
		ObservableList<Node> childeren = pricerVBox.getChildren();
		for(int i = 1; i < childeren.size(); i++) {
			JFXCheckBox cb = (JFXCheckBox)childeren.get(i);
			if(cb.isSelected()) {
				ret.add(pricerCheckBoxes.get(cb));
			}
		}
		return ret;
	}
	
	public boolean showAndWait() {
		stage.showAndWait();
		return accepted;
	}

	public void accept() {
		accepted = true;
		stage.close();
	}

	public void cancel() {
		accepted = false;
		stage.close();
	}

	public ScreenResolution getResolution() {
		return resolutionComboBox.getValue();
	}

	public void setResolution(ScreenResolution resolution) {
		resolutionComboBox.setValue(resolution);
	}

	public void updateData() {
		loadingDialog.show(stackPane);
		new Thread(() -> {
			try {
				database.emptyTables();
				database.setFastMode(true);
				DataDownLoader dl = new DataDownLoader(database);
				Set<String> wordList = null;
				try {
					wordList = dl.downloadData();
				} finally {
					database.setFastMode(false);
				}
				File f = new File("tessdata/eng.user-words");
				if (f.exists()) {
					f.delete();
				}
				f.getParentFile().mkdirs();
				f.createNewFile();
				try (FileWriter out = new FileWriter(f);) {
					for (String s : wordList) {
						out.append(s);
						out.append("\n");
					}
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
				log.severe(e1.toString());
			} catch (IOException e2) {
				e2.printStackTrace();
				log.severe(e2.toString());
			} finally {
				Platform.runLater(() -> loadingDialog.close());
			}
		}).start();
	}

	@FXML
	private void changeHotKey() {
		GlobalScreen.addNativeKeyListener(this);
		pressAKeyDialog.show(stackPane);
	}
	
	public void setOnTopTime(float onTopTime) {
		timeOnTopSpinner.getValueFactory().setValue((double)onTopTime);
	}

	public void setReadRewardsHotKey(int readRewardsHotkey) {
		hotkeyButton.setText(NativeKeyEvent.getKeyText(readRewardsHotkey));
		hotkey = readRewardsHotkey;
	}

	public float getOnTopTime() {
		return timeOnTopSpinner.getValue().floatValue();
	}

	public int getRewardsHotKey() {
		return hotkey;
	}

	@Override
	public void nativeKeyTyped(NativeKeyEvent nativeEvent) {
	}

	@Override
	public void nativeKeyPressed(NativeKeyEvent nativeEvent) {
		GlobalScreen.removeNativeKeyListener(this);
		Platform.runLater(()->setReadRewardsHotKey(nativeEvent.getKeyCode()));
		pressAKeyDialog.close();
	}

	@Override
	public void nativeKeyReleased(NativeKeyEvent nativeEvent) {
	}

}
