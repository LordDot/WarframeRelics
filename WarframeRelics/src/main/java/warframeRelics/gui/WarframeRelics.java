package warframeRelics.gui;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javafx.application.Application;
import javafx.application.Application.Parameters;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Callback;
import warframeRelics.dataBase.SQLLiteDataBase;


public class WarframeRelics extends Application {

	public static final String VERSION = "1.4.2.0";
	public static final String TESSDATA_PATH = "./tessdata";
	public static final String DB_PATH = "./db.db";
	public static final String LOG_PATH = "./log.txt";

	private static final Logger log = Logger.getLogger(WarframeRelics.class.getName());

	private Scene mainScene;

	private SQLLiteDataBase database;

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

		try {
			database = new SQLLiteDataBase("DB_PATH");
		} catch (SQLException e) {
			e.printStackTrace();
			log.severe(e.toString());
		}

		List<String> params = getParameters().getRaw();
		String imageFile = (params.size() == 0)?null:params.get(0) ;
		System.out.println(imageFile);

		FXMLLoader loader = new FXMLLoader(getClass().getResource("WarframeRelics.fxml"));
		loader.setControllerFactory((Class<?> param) -> {
			return new WarframeRelicsController(stage, database,"warframeRelics/screenCapture/relicsPositions.json", imageFile);
		});
		Parent root = loader.load();
		mainScene = new Scene(root, 600, 410);
		stage.setScene(mainScene);
		stage.show();
	}

	public static void main(String args[]) throws SecurityException, IOException {

		Logger rootLogger = Logger.getLogger("warframeRelics");

		// rootLogger.setLevel(Level.FINEST);

		FileHandler handler = new FileHandler(LOG_PATH);
		handler.setFormatter(new SimpleFormatter());
		rootLogger.addHandler(handler);

		new FileExtractor("warframeRelics/copyFiles.txt").extractFiles();
		
		launch(args);
	}

}
