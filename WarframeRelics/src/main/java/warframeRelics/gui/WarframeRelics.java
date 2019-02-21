package warframeRelics.gui;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.imageio.ImageIO;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Stage;
import net.sourceforge.tess4j.TesseractException;
import warframeRelics.dataBase.SQLLiteDataBase;
import warframeRelics.dataDownload.DataDownLoader;
import warframeRelics.pricing.Pricer;
import warframeRelics.pricing.WarframeMarket;
import warframeRelics.screenCapture.RelicReader;
import warframeRelics.screenCapture.ScreenBufferedImageProvider;

public class WarframeRelics extends Application {
	public static String VERSION = "1.1.4.0";

	private static final Logger log = Logger.getLogger(WarframeRelics.class.getName());

	private Scene mainScene;
	private BorderPane borderPane;
	private GridPane table;
	private Label[] labels;
	private Button readButton;
	private Button updateButton;
	private Button debugButton;

	private PriceDisplayer[] prices;
	private ProgressBar progressBar;

	private RelicReader relicReader;
	private SQLLiteDataBase database;
	private Pricer pricer;
	private int debugImageCounter;

	@Override
	public void start(Stage stage) throws Exception {
		stage.setTitle("Warframe Relics " + VERSION);
		stage.setOnCloseRequest(e -> {
			if (database != null) {
				try {
					database.close();
				} catch (SQLException e1) {
					e1.printStackTrace();
					log.severe(e.toString());
				}
			}
		});

		borderPane = new BorderPane();

		table = new GridPane();
		ColumnConstraints c = new ColumnConstraints();
		c.setPercentWidth(50);
		for (int i = 0; i < 2; i++) {
			table.getColumnConstraints().add(c);
		}
		RowConstraints r = new RowConstraints();
		r.setPercentHeight(50);
		for (int i = 0; i < 6; i++) {
			table.getRowConstraints().add(r);
		}

		table.setVgap(5);
		table.setHgap(5);
		table.setPadding(new Insets(5));

		labels = new Label[4];
		prices = new PriceDisplayer[4];
		for (int i = 0; i < 4; i++) {
			labels[i] = new Label("test");
			table.add(Util.stretch(labels[i]), 0, i);
			labels[i].setAlignment(Pos.CENTER_RIGHT);

			prices[i] = new PriceDisplayer();
			table.add(Util.stretch(prices[i]), 1, i);
		}

		updateButton = new Button("Update Data");
		updateButton.setOnAction(e -> {

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
					f.createNewFile();
					try (FileWriter out = new FileWriter(f);) {
						for (String s : wordList) {
							out.append(s);
							out.append("\n");
						}
					}
					dl.downloadMissionData();
					relicReader = new RelicReader(database, new ScreenBufferedImageProvider());
				} catch (SQLException e1) {
					e1.printStackTrace();
					log.severe(e1.toString());
				} catch (IOException e2) {
					e2.printStackTrace();
					log.severe(e2.toString());
				} catch (AWTException e3) {
					e3.printStackTrace();
					log.severe(e3.toString());
				} finally {
					Platform.runLater(() -> progressBar.setProgress(0));
				}
			}).start();
		});
		table.add(Util.stretch(updateButton), 0, 4);

		readButton = new Button();
		readButton.setText("Read Rewards");
		readButton.setOnAction(e -> {
			new Thread(new Runnable() {
				@Override
				public void run() {
					Platform.runLater(() -> progressBar.setProgress(-1.0));
					try {
						String[] rewards = relicReader.readRelics();
						for (int i = 0; i < 4; i++) {
							String labelText = rewards[i];
							if (database.getItemVaulted(rewards[i])) {
								labelText += " (v)";
							}
							Integer index = new Integer(i);
							final String text = labelText;
							Platform.runLater(() -> labels[index.intValue()].setText(text));

							final Pricer.Price p;
							if (rewards[i].equals("Forma Blueprint")) {
								p= null;
							}else {
								p =pricer.getPlat(rewards[i]);
							}
							Platform.runLater(() -> prices[index.intValue()].setPrices(p));
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
				}
			}).start();
		});
		table.add(Util.stretch(readButton), 0, 5, 2, 1);

		debugButton = new Button();
		debugButton.setText("Debug info");
		debugButton.setOnAction(e -> {
			new Thread(new Runnable() {
				@Override
				public void run() {
					Platform.runLater(() -> progressBar.setProgress(-1.0));
					try {
						File f = new File("./debug/image" + debugImageCounter++ + ".png");
						f.mkdirs();
						f.createNewFile();
						log.info("writing debug image number" + debugImageCounter);
						ImageIO.write(new Robot().createScreenCapture(new Rectangle(0, 0, 1920, 1080)), "png", f);
					} catch (IOException e) {
						e.printStackTrace();
						log.severe(e.toString());
					} catch (AWTException e) {
						e.printStackTrace();
						log.severe(e.toString());
					} finally {
						Platform.runLater(() -> progressBar.setProgress(0));
					}
				}
			}).start();
		});
		table.add(Util.stretch(debugButton), 1, 4);

		borderPane.setCenter(Util.stretch(table));

		progressBar = new ProgressBar(0);
		borderPane.setBottom(Util.stretch(progressBar));

		mainScene = new Scene(Util.stretch(borderPane), 400, 300);
		stage.setScene(mainScene);

		stage.show();

		try {
			database = new SQLLiteDataBase("./db.db");
		} catch (SQLException e) {
			e.printStackTrace();
			log.severe(e.toString());
		}

		try {
			relicReader = new RelicReader(database, new ScreenBufferedImageProvider());
		} catch (AWTException e1) {
			e1.printStackTrace();
			log.severe(e1.toString());
		}
		pricer = new WarframeMarket();
	}

	public static void main(String args[]) throws SecurityException, IOException {

		Logger rootLogger = Logger.getLogger("warframeRelics");

		// rootLogger.setLevel(Level.FINEST);

		FileHandler handler = new FileHandler("log.txt");
		handler.setFormatter(new SimpleFormatter());
		rootLogger.addHandler(handler);

		launch(args);
	}
}
