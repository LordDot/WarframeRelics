package warframeRelics.gui;

import com.jfoenix.controls.JFXDialog;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MessageBox extends JFXDialog implements Initializable {

    @FXML
    private Label headingLabel;
    @FXML
    private Label bodyLabel;

    private String heading;
    private String body;

    public MessageBox(String heading, String body) {
        this.heading = heading;
        this.body = body;

        FXMLLoader loader = new FXMLLoader(getClass().getResource("MessageBox.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        headingLabel.setText(heading);
        bodyLabel.setText(body);
        setTransitionType(DialogTransition.CENTER);
    }
}
