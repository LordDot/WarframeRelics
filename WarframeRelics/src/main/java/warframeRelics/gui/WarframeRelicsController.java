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
import java.util.ArrayList;
import java.util.List;
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
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import net.sourceforge.tess4j.TesseractException;
import warframeRelics.dataBase.SQLLiteDataBase;
import warframeRelics.dataDownload.DataDownLoader;
import warframeRelics.pricing.WarframeMarket;
import warframeRelics.pricing.WarframeMarket.Price;
import warframeRelics.screenCapture.BufferedImageProvider;
import warframeRelics.screenCapture.FileImageProvider;
import warframeRelics.screenCapture.RelicReader;
import warframeRelics.screenCapture.ResolutionFile;
import warframeRelics.screenCapture.ScreenBufferedImageProvider;
import warframeRelics.screenCapture.ScreenResolution;

public class WarframeRelicsController implements Initializable {
	private static final Logger log = Logger.getLogger(WarframeRelicsController.class.getName());

	private RelicReader relicReader;
	private SQLLiteDataBase database;
	private int debugImageCounter;

	@FXML
	private GridPane table;
	@FXML
	private ProgressBar progressBar;
	@FXML
	private ChoiceBox<ScreenResolution> resolutionComboBox;
	private ScreenResolution resolution;
	private ResolutionFile resolutionFile;
	
	private Label[] nameLabels;
	private List<PriceDisplayer[]> prices;

	private List<Pricer> pricers;

	public WarframeRelicsController(SQLLiteDataBase dataBase, String resolutionFile, String fromFile) {
		this.database = dataBase;
		this.resolutionFile = new ResolutionFile(getClass().getClassLoader().getResourceAsStream(resolutionFile));
		
		try {
			BufferedImageProvider prov;
			if (fromFile == null) {
				prov = new ScreenBufferedImageProvider(this.resolutionFile.getFromString("1920x1080"));
			} else {
				prov = new FileImageProvider(new FileInputStream(fromFile));
			}
			relicReader = new RelicReader(dataBase, prov, this.resolutionFile.getFromString("1920x1080"));

		} catch (AWTException | IOException e1) {
			e1.printStackTrace();
			log.severe(e1.toString());
		}

		prices = new ArrayList<>();
		pricers = new ArrayList<>();
		pricers.add(new WarframeMarketWrapper());
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		nameLabels = new Label[4];
		for (int i = 0; i < 4; i++) {
			nameLabels[i] = new Label();
			table.add(Util.stretch(nameLabels[i]), 0, i + 1);
			nameLabels[i].setAlignment(Pos.CENTER_RIGHT);

		}

		prices = new ArrayList<>();
		for (int i = 0; i < pricers.size(); i++) {
			Pricer p = pricers.get(i);

			ColumnConstraints c = new ColumnConstraints();
			c.setPercentWidth(p.getColumnWidth());
			table.getColumnConstraints().add(c);

			table.add(p.getHeader(), i + 1, 0);

			PriceDisplayer[] priceDisplayers = new PriceDisplayer[4];
			for (int j = 0; j < 4; j++) {
				priceDisplayers[j] = p.getPriceDisplayer();
				table.add(priceDisplayers[j], i + 1, j + 1);
			}
			prices.add(priceDisplayers);
		}

		resolutionComboBox.getItems().addAll(this.resolutionFile.getResolutions());
		resolutionComboBox.setConverter(new StringConverter<ScreenResolution>() {

			@Override
			public String toString(ScreenResolution object) {
				return object.name();
			}

			@Override
			public ScreenResolution fromString(String string) {
				return resolutionFile.getFromString(string);
			}
		});
		resolutionComboBox.setValue(resolutionFile.getFromString("1920x1080"));
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
				database.setFastMode(true);
				DataDownLoader dl = new DataDownLoader(database);
				Set<String> wordList = null;
				try {
					wordList = dl.downLoadPartData();
				}finally {
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
				try {
					dl.downloadMissionData();
				}finally {
					database.setFastMode(false);
				}
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
				for (i = 0; i < rewards.length; i++) {
					String labelText = rewards[i];
					if (database.getItemVaulted(rewards[i])) {
						labelText += " (v)";
					}
					int index = i;
					final String text = labelText;
					Platform.runLater(() -> nameLabels[index].setText(text));
					for (PriceDisplayer[] pd : prices) {
						pd[index].setPrice(rewards[index]);
					}
				}
				for (; i < 4; i++) {
					int index = i;
					Platform.runLater(() -> nameLabels[index].setText(""));
					for (PriceDisplayer[] pd : prices) {
						pd[index].setPrice(null);
					}
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
				ImageIO.write(new Robot().createScreenCapture(
						new Rectangle(0, 0, resolution.getWidth(), resolution.getHeight())), "png", f);
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
