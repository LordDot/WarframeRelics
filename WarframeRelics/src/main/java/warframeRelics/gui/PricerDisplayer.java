package warframeRelics.gui;

import com.jfoenix.controls.JFXCheckBox;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import warframeRelics.gui.priceControls.Pricer;

import java.io.IOException;

public class PricerDisplayer extends AnchorPane {

    private Pricer pricer;

    @FXML
    private JFXCheckBox checkBox;

    public PricerDisplayer(){
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PricerDisplayer.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setPricer(Pricer pricer){
        this.pricer = pricer;
        checkBox.setText(pricer.getName());
    }

    public Pricer getPricer(){
        return pricer;
    }

    public void setSelected(boolean selected){
        checkBox.setSelected(selected);
    }

    public boolean isSelected(){
        return checkBox.isSelected();
    }
}
