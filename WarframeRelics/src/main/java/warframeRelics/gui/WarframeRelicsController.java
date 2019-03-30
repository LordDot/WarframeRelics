package warframeRelics.gui;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
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
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import net.sourceforge.tess4j.TesseractException;
import warframeRelics.beans.PrimeItem;
import warframeRelics.dataBase.SQLLiteDataBase;
import warframeRelics.dataDownload.DataDownLoader;
import warframeRelics.gui.priceControls.NamePricer;
import warframeRelics.gui.priceControls.PriceDisplayer;
import warframeRelics.gui.priceControls.Pricer;
import warframeRelics.gui.priceControls.PricerFactory;
import warframeRelics.gui.priceControls.WarframeMarketWrapper;
import warframeRelics.pricing.WarframeMarket;
import warframeRelics.pricing.WarframeMarket.Price;
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
    private SettingsFile settingsFile;
    private String settingsPath;
    private ScreenResolution resolution;
    private int readRewardsHotkey;
    private float onTopTime;

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

    private Map<Pricer, Boolean> pricers;

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

        if (new File(settingsFile).exists()) {
            try (Reader in = new FileReader(settingsFile)) {
                this.settingsFile = new SettingsFile(in);
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
                this.settingsFile = new SettingsFile();
            }
        } else {
            this.settingsFile = new SettingsFile();
        }

        try {
            BufferedImageProvider prov;
            resolution = this.resolutionFile.getFromString(this.settingsFile.getResolution());
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

        readRewardsHotkey = this.settingsFile.getReadRewardsHotkey();
        onTopTime = this.settingsFile.getOnTopTime();

        prices = new ArrayList<>();
        pricers = new LinkedHashMap<>();
        pricers.put(pricerFactory.getNamePricer(), false);
        pricers.put(pricerFactory.getWarframeMarketPricer(), false);
        GlobalScreen.addNativeKeyListener(this);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        List<Pricer> pricers = new ArrayList<>();
        for (String p : settingsFile.getPriceDisplayers())
            pricers.add(pricerFactory.get(p));
        setPriceDisplayers(pricers);
    }

    public void setPriceDisplayers(List<Pricer> pricers) {
        removePriceDisplayers();
        for (int i = 0; i < pricers.size(); i++) {
            Pricer p = pricers.get(i);
            this.pricers.put(p, true);

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
        this.pricers.forEach((p, b) -> pricers.put(p, false));
    }

    public void setResolution(ScreenResolution resolution) {
        this.resolution = resolution;
        try {
            relicReader.setResolution(resolution);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                setWorking(false);
            }
        }).start();
    }

    public void openSettings() {
        SettingsDialog settings;
        try {
            settings = new SettingsDialog(mainStage, database, resolutionFile);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        settings.setResolution(resolution);
        settings.setPricers(pricers);
        settings.setOnTopTime(onTopTime);
        settings.setReadRewardsHotKey(readRewardsHotkey);

        readRewardsHotkey = -1;

        if (settings.showAndWait()) {
            ScreenResolution res = settings.getResolution();
            setResolution(res);
            settingsFile.setResolution(res.name());

            List<Pricer> priceDisplayers = settings.getSelectedPricers();
            setPriceDisplayers(priceDisplayers);
            List<String> pricerNames = new ArrayList<>();
            for (Pricer p : priceDisplayers) {
                pricerNames.add(pricerFactory.getName(p));
            }
            settingsFile.setPriceDisplayers(pricerNames);

            onTopTime = settings.getOnTopTime();
            settingsFile.setOnTopTime(onTopTime);

            readRewardsHotkey = settings.getRewardsHotKey();
            settingsFile.setReadRewardsHotkey(readRewardsHotkey);

            try (Writer out = new FileWriter(settingsPath)) {
                settingsFile.writeTo(out);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
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
            if (nativeEvent.getKeyCode() == readRewardsHotkey) {
                readRewards(() -> {
                    Platform.runLater(() -> mainStage.setAlwaysOnTop(true));
                    try {
                        Thread.sleep((long) (onTopTime * 1000));
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
