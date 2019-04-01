package warframeRelics.gui;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.jfoenix.controls.JFXButton;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import net.sourceforge.tess4j.TesseractException;
import warframeRelics.beans.PrimeItem;
import warframeRelics.dataBase.SQLLiteDataBase;
import warframeRelics.gui.priceControls.PriceDisplayer;
import warframeRelics.gui.priceControls.Pricer;
import warframeRelics.gui.priceControls.PricerFactory;
import warframeRelics.gui.settings.Settings;
import warframeRelics.gui.settings.SettingsDialog;
import warframeRelics.gui.settings.SettingsLoader;
import warframeRelics.screenCapture.BufferedImageProvider;
import warframeRelics.screenCapture.FileImageProvider;
import warframeRelics.screenCapture.RelicReader;
import warframeRelics.screenCapture.ResolutionFile;
import warframeRelics.screenCapture.ScreenBufferedImageProvider;
import warframeRelics.screenCapture.ScreenResolution;

public class WarframeRelicsController implements Initializable, NativeKeyListener {
    private static final Logger log = Logger.getLogger(WarframeRelicsController.class.getName());

    private RelicReader relicReader;
    private SQLLiteDataBase database;
    private int debugImageCounter;
    private ResolutionFile resolutionFile;
    private Settings settings;
    private String settingsPath;

    private PricerFactory pricerFactory;

    private boolean working;

    @FXML
    private GridPane table;
    @FXML
    private Stage mainStage;
    @FXML
    private JFXButton readRewardsButton;
    @FXML
    private JFXButton screenshotButton;
    @FXML
    private JFXButton settingsButton;

    // private Label[] nameLabels;
    private List<Updatable<PriceDisplayer>[]> prices;

    public WarframeRelicsController(Stage stage, SQLLiteDataBase dataBase, String resolutionFile, String settingsFile,
                                    String fromFile) {
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException e2) {
            e2.printStackTrace();
        }
        this.mainStage = stage;
        this.database = dataBase;
        settingsPath = settingsFile;
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resolutionFile);) {
            this.resolutionFile = new ResolutionFile(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        pricerFactory = new PricerFactory(dataBase);

        boolean newSettings = false;
        if (new File(settingsFile).exists()) {
            try (Reader in = new FileReader(settingsFile)) {
                this.settings = SettingsLoader.loadSettings(in, this.resolutionFile);
            } catch (IOException | NullPointerException e) {
                newSettings = true;
                e.printStackTrace();
            }
        } else {
            newSettings = true;
        }

        if (newSettings) {
            List<String> defaultPricers = new ArrayList<>(2);
            defaultPricers.add(PricerFactory.NAME);
            defaultPricers.add(PricerFactory.WARFRAME_MARKET);
            this.settings = new Settings(this.resolutionFile.getFromString("1920x1080"), defaultPricers, -1, 5.0f);
        }

        try {
            BufferedImageProvider prov;
            ScreenResolution resolution = this.settings.getResolution();
            if (fromFile == null) {
                prov = new ScreenBufferedImageProvider(resolution);
            } else {
                prov = new FileImageProvider(new FileInputStream(fromFile));
            }
            relicReader = new RelicReader(dataBase, prov, resolution);

        } catch (AWTException | IOException e1) {
            e1.printStackTrace();
            log.severe(e1.toString());
        }

        prices = new ArrayList<>();
        GlobalScreen.addNativeKeyListener(this);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setPriceDisplayers(settings.getPriceDisplayers());
    }

    public void setPriceDisplayers(List<String> pricers) {
        removePriceDisplayers();
        for (int i = 0; i < pricers.size(); i++) {
            Pricer p = pricerFactory.get(pricers.get(i));

            ColumnConstraints c = new ColumnConstraints();
            c.setPercentWidth(p.getColumnWidth());
            table.getColumnConstraints().add(c);
            table.add(p.getHeader(), i, 0);

            @SuppressWarnings("unchecked")
            Updatable<PriceDisplayer>[] priceDisplayers = (Updatable<PriceDisplayer>[]) new Updatable[4];
            for (int j = 0; j < 4; j++) {
                priceDisplayers[j] = new Updatable<>(p.getPriceDisplayer());
                table.add(priceDisplayers[j], i, j + 1);
            }
            prices.add(priceDisplayers);
        }
        mainStage.sizeToScene();
    }

    public void removePriceDisplayers() {
        table.getChildren().clear();
        table.getColumnConstraints().clear();
        prices.clear();
    }

    public void readRewardsCallback() {
        readRewards(() -> {
        });
    }

    public void readRewards(Runnable afterwards) {
        setWorking(true);
        for (Updatable<PriceDisplayer>[] ua : prices) {
            for (int i = 0; i < ua.length; i++) {
                ua[i].setUpdating(true);
            }
        }
        new Thread(() -> {
            try {
                PrimeItem[] rewards = relicReader.readRelics();
                int i;
                for (i = 0; i < rewards.length; i++) {
                    int index = i;
                    for (Updatable<PriceDisplayer>[] pd : prices) {
                        new Thread(() -> {
                            pd[index].getNode().setPrice(rewards[index]);
                            Platform.runLater(() -> pd[index].setUpdating(false));
                        }).start();
                    }
                }
                for (; i < 4; i++) {
                    int index = i;
                    for (Updatable<PriceDisplayer>[] pd : prices) {
                        pd[index].getNode().setPrice(null);
                        Platform.runLater(() -> pd[index].setUpdating(false));
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
                setWorking(false);
                afterwards.run();
            }
        }).start();
    }

    public void takeScreenshot() {
        setWorking(true);
        new Thread(() -> {
            try {
                File f = new File("./debug/image" + debugImageCounter + ".png");
                f.mkdirs();
                f.createNewFile();
                log.info("writing debug image number" + debugImageCounter++);
                ImageIO.write(new Robot().createScreenCapture(
                        new Rectangle(0, 0, settings.getResolution().getWidth(), settings.getResolution().getHeight())), "png", f);
            } catch (IOException e) {
                e.printStackTrace();
                log.severe(e.toString());
            } catch (AWTException e) {
                e.printStackTrace();
                log.severe(e.toString());
            } finally {
                setWorking(false);
            }
        }).start();
    }

    public void openSettings() {
        setWorking(true);
        SettingsDialog settings;
        try {
            settings = new SettingsDialog(mainStage, database, resolutionFile, pricerFactory);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        settings.setSettings(this.settings);

        if (settings.showAndWait()) {
            Settings returnedSettings = settings.getSettings();
            this.settings = returnedSettings;

            try {
                relicReader.setResolution(this.settings.getResolution());
            } catch (IOException e) {
                e.printStackTrace();
            }

            List<String> priceDisplayers = this.settings.getPriceDisplayers();
            setPriceDisplayers(priceDisplayers);

            try (Writer out = new FileWriter(settingsPath)) {
                SettingsLoader.writeSettings(this.settings, out);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        setWorking(false);
    }

    private void setWorking(boolean working) {
        this.working = working;
        Platform.runLater(() -> {
            readRewardsButton.setDisable(working);
            screenshotButton.setDisable(working);
            settingsButton.setDisable(working);
        });
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent nativeEvent) {

    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent nativeEvent) {

    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent nativeEvent) {
        if (!working) {
            if (nativeEvent.getKeyCode() == settings.getReadRewardsHotkey()) {
                readRewards(() -> {
                    Platform.runLater(() -> mainStage.setAlwaysOnTop(true));
                    try {
                        Thread.sleep((long) (settings.getOnTopTime() * 1000));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Platform.runLater(() -> {
                        mainStage.setAlwaysOnTop(false);
                        mainStage.toBack();
                    });
                });
            }
        }
    }
}
