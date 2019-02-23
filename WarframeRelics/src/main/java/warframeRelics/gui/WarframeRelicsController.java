package warframeRelics.gui;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import net.sourceforge.tess4j.TesseractException;
import warframeRelics.dataBase.SQLLiteDataBase;
import warframeRelics.dataDownload.DataDownLoader;
import warframeRelics.pricing.Pricer;
import warframeRelics.pricing.WarframeMarket;
import warframeRelics.screenCapture.BufferedImageProvider;
import warframeRelics.screenCapture.FileImageProvider;
import warframeRelics.screenCapture.RelicReader;
import warframeRelics.screenCapture.ScreenBufferedImageProvider;
import warframeRelics.screenCapture.ScreenResolution;

public class WarframeRelicsController implements Initializable {
	private static final Logger log = Logger.getLogger(WarframeRelicsController.class.getName());

	private RelicReader relicReader;
	private SQLLiteDataBase database;
	private Pricer pricer;
	private int debugImageCounter;

	@FXML
	private GridPane table;
	@FXML
	private ProgressBar progressBar;
	@FXML
	private ChoiceBox<ScreenResolution> resolutionComboBox;
	private ScreenResolution resolution;
	
	private Label[] labels;
	private PriceDisplayer[] prices;


	public WarframeRelicsController(SQLLiteDataBase dataBase, String fromFile) {

		try {
			this.database = dataBase;
			BufferedImageProvider prov;
			if(fromFile==null){
				prov = new ScreenBufferedImageProvider(ScreenResolution.S1920x1080);
			}else {
				prov = new FileImageProvider(new FileInputStream(fromFile));
			}
			relicReader = new RelicReader(dataBase, prov, ScreenResolution.S1920x1080);

		} catch (AWTException | IOException e1) {
			e1.printStackTrace();
			log.severe(e1.toString());
		}
		pricer = new WarframeMarket();
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		labels = new Label[4];
		prices = new PriceDisplayer[4];
		for (int i = 0; i < 4; i++) {
			labels[i] = new Label();
			table.add(Util.stretch(labels[i]), 0, i);
			labels[i].setAlignment(Pos.CENTER_RIGHT);

			prices[i] = new PriceDisplayer();
			table.add(Util.stretch(prices[i]), 1, i);
		}
		
		resolutionComboBox.getItems().addAll(ScreenResolution.values());
		resolutionComboBox.setConverter(new StringConverter<ScreenResolution>() {
			
			@Override
			public String toString(ScreenResolution object) {
				return object.name().substring(1);
			}
			
			@Override
			public ScreenResolution fromString(String string) {
				return ScreenResolution.valueOf("S" + string);
			}
		});
		resolutionComboBox.setValue(ScreenResolution.S1920x1080);
	}

	public void setResolution() {
		resolution = resolutionComboBox.getValue();
		try {
			relicReader.setResolution(resolution);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void update() {
		new Thread(() -> {
			try {
				Platform.runLater(() -> progressBar.setProgress(-1.0));
				database.emptyTables();
				DataDownLoader dl = new DataDownLoader(database);
				Set<String> wordList = dl.downLoadPartData();
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
				dl.downloadMissionData();
			} catch (SQLException e1) {
				e1.printStackTrace();
				log.severe(e1.toString());
			} catch (IOException e2) {
				e2.printStackTrace();
				log.severe(e2.toString());
			} finally {
				Platform.runLater(() -> progressBar.setProgress(0));
			}
		}).start();
	}

	public void readRewards() {
		new Thread(() -> {
			Platform.runLater(() -> progressBar.setProgress(-1.0));
			try {
				String[] rewards = relicReader.readRelics();
				int i;
				for (i= 0; i < rewards.length; i++) {
					String labelText = rewards[i];
					if (database.getItemVaulted(rewards[i])) {
						labelText += " (v)";
					}
					int index = i;
					final String text = labelText;
					Platform.runLater(() -> labels[index].setText(text));

					final Pricer.Price p;
					if (rewards[i].equals("Forma Blueprint")) {
						p = null;
					} else {
						p = pricer.getPlat(rewards[i]);
					}
					Platform.runLater(() -> prices[index].setPrice(p));
				}
				for(; i < 4;i++) {
					int index = i;
					Platform.runLater(() -> labels[index].setText(""));
					Platform.runLater(() -> prices[index].setPrice(null));
				}
			} catch (TesseractException e1) {
				e1.printStackTrace();
				log.severe(e1.toString());
			} catch (SQLException e1) {
				e1.printStackTrace();
				log.severe(e1.toString());
			} catch (Exception e) {
				e.printStackTrace();
				log.severe(e.toString());
			} finally {
				Platform.runLater(() -> progressBar.setProgress(0));
			}
		}).start();
	}

	public void takeScreenshot() {
		new Thread(() -> {
			Platform.runLater(() -> progressBar.setProgress(-1.0));
			try {
				File f = new File("./debug/image" + debugImageCounter++ + ".png");
				f.mkdirs();
				f.createNewFile();
				log.info("writing debug image number" + debugImageCounter);
				ImageIO.write(new Robot().createScreenCapture(new Rectangle(0, 0, resolution.getWidth(), resolution.getHeight())), "png", f);
			} catch (IOException e) {
				e.printStackTrace();
				log.severe(e.toString());
			} catch (AWTException e) {
				e.printStackTrace();
				log.severe(e.toString());
			} finally {
				Platform.runLater(() -> progressBar.setProgress(0));
			}
		}).start();
	}

}
