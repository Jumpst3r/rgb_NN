package View;

import Controller.Controller;
import Controller.Base.Main;
import Controller.Handlers.FileSelectorHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

/**
 * Created by ndutl on 18/06/2017.
 */
public class FileSelectorButton extends Button {

    private String name;

    public FileSelectorButton(String name){
        super("open");
        this.name = name;
        this.setOnAction(FileSelectorHandler.selectFile(this.name));
        this.setPrefWidth(150);
    }
}
