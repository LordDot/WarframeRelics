package warframeRelics.gui.settings;

import com.jfoenix.controls.*;
import com.jfoenix.controls.JFXDialog.DialogTransition;
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
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import warframeRelics.dataBase.IDataBase;
import warframeRelics.dataDownload.DataDownLoader;
import warframeRelics.gui.priceControls.Pricer;
import warframeRelics.gui.priceControls.PricerFactory;
import warframeRelics.screenCapture.ResolutionFile;
import warframeRelics.screenCapture.ScreenResolution;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SettingsDialog implements Initializable, NativeKeyListener {

    private static final Logger log = Logger.getLogger(SettingsDialog.class.getName());

    private Stage stage;
    private Scene scene;
    private boolean accepted;
    private IDataBase database;
    private PricerFactory pricerFactory;

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
    private List<PricerDisplayer> pricerDisplayers;
    private int hotkey;
    private Settings settings;

    public SettingsDialog(Stage parentStage, IDataBase database, ResolutionFile resolutionFile, PricerFactory pricerFactory) throws IOException {
        this.database = database;
        this.pricerFactory = pricerFactory;
        stage = new Stage();
        accepted = false;
        pricerDisplayers = new ArrayList<>();

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

        for (Pricer p : pricerFactory.getAllPricers()) {
            PricerDisplayer dp = new PricerDisplayer();
            dp.setPricer(p);
            registerDragEvents(dp);
            pricerVBox.getChildren().add(dp);
            pricerDisplayers.add(dp);
        }
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
        setSelectedPricers(settings.getPriceDisplayers());
        setResolution(settings.getResolution());
        setReadRewardsHotKey(settings.getReadRewardsHotkey());
        setOnTopTime(settings.getOnTopTime());
    }

    public Settings getSettings() {
        List<String> selectedPricers = pricerDisplayers.stream().filter((pd) -> pd.isSelected()).map((pd) -> pd.getPricer().getId()).collect(Collectors.toList());
        settings.setPriceDisplayers(selectedPricers);
        settings.setResolution(resolutionComboBox.getValue());
        settings.setOnTopTime(timeOnTopSpinner.getValue().floatValue());
        return settings;
    }

    private void setSelectedPricers(List<String> pricerIds) {
        pricerVBox.getChildren().clear();
        pricerDisplayers.clear();
        List<Pricer> pricers = pricerFactory.getAllPricers();
        for (String id : pricerIds) {
            Pricer p = pricerFactory.get(id);
            pricers.remove(p);

            PricerDisplayer dp = new PricerDisplayer();
            dp.setPricer(p);
            dp.setSelected(true);
            registerDragEvents(dp);
            pricerVBox.getChildren().add(dp);
            pricerDisplayers.add(dp);
        }

        for (Pricer p : pricers) {
            PricerDisplayer dp = new PricerDisplayer();
            dp.setPricer(p);
            registerDragEvents(dp);
            pricerVBox.getChildren().add(dp);
            pricerDisplayers.add(dp);
        }
    }

    private void setResolution(ScreenResolution resolution) {
        resolutionComboBox.setValue(resolution);
        settings.setResolution(resolution);
    }

    private void setReadRewardsHotKey(int readRewardsHotkey) {
        hotkeyButton.setText(NativeKeyEvent.getKeyText(readRewardsHotkey));
        settings.setReadRewardsHotkey(readRewardsHotkey);
    }

    private void setOnTopTime(float onTopTime) {
        timeOnTopSpinner.getValueFactory().setValue((double) onTopTime);
        settings.setOnTopTime(onTopTime);
    }

    public boolean showAndWait() {
        stage.showAndWait();
        return accepted;
    }

    @FXML
    private void accept() {
        accepted = true;
        stage.close();
    }

    @FXML
    private void cancel() {
        accepted = false;
        stage.close();
    }

    @FXML
    private void updateData() {
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

    @Override
    public void nativeKeyTyped(NativeKeyEvent nativeEvent) {
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent nativeEvent) {
        GlobalScreen.removeNativeKeyListener(this);
        Platform.runLater(() -> setReadRewardsHotKey(nativeEvent.getKeyCode()));
        pressAKeyDialog.close();
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent nativeEvent) {
    }


    private void registerDragEvents(PricerDisplayer target) {
        target.addEventFilter(MouseEvent.DRAG_DETECTED, (event) -> {

            Dragboard dragboard = target.startDragAndDrop(TransferMode.ANY);

            ClipboardContent content = new ClipboardContent();
            content.putString(target.getPricer().getId());
            dragboard.setContent(content);

            event.consume();
        });
        target.setOnDragOver(event -> {

            if (!checkChilds(target, (Node) event.getGestureSource()) && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }

            event.consume();
        });
        target.setOnDragEntered(event -> {

            if (!checkChilds(target, (Node) event.getGestureSource()) && event.getDragboard().hasString()) {
                target.setOpacity(0.3);
            }
        });
        target.setOnDragExited(event -> {

            if (!checkChilds(target, (Node) event.getGestureSource()) && event.getDragboard().hasString()) {
                target.setOpacity(1);
            }
        });
        target.setOnDragDropped(event -> {

            Dragboard dragboard = event.getDragboard();
            boolean success = false;
            if (dragboard.hasString()) {
                ObservableList<Node> children = pricerVBox.getChildren();
                int index = children.indexOf(target);
                String draggedId = dragboard.getString();
                PricerDisplayer dragged = (PricerDisplayer) pricerVBox.getChildren().stream().filter(node -> node instanceof PricerDisplayer && ((PricerDisplayer) node).getPricer().getId() == draggedId).collect(Collectors.toList()).get(0);
                children.remove(dragged);
                children.add(index, dragged);
                pricerDisplayers.remove(dragged);
                pricerDisplayers.add(index, dragged);
                success = true;
            }

            event.setDropCompleted(success);

            event.consume();
        });
    }

    private boolean checkChilds(Pane parent, Node child) {
        if (child == parent) {
            return true;
        }
        for (Node n : parent.getChildren()) {
            if (n instanceof Pane && checkChilds((Pane) n, child)) {
                return true;
            }
        }
        return false;
    }
}
