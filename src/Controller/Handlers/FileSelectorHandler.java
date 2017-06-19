package Controller.Handlers;

import Controller.Base.Main;
import Controller.Controller;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.FileChooser;

/**
 * Created by ndutl on 18/06/2017.
 */
public class FileSelectorHandler {

    public static EventHandler<ActionEvent> selectFile(String name) {
        return e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Browse for " + name);
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("csv files","*.csv"));
            switch (name) {
                case "Training set":
                    Controller.setTraining_set(chooser.showOpenDialog(Main.getRootStage()));
                    break;
                case "Validation set":
                    Controller.setValidation_set(chooser.showOpenDialog(Main.getRootStage()));
                    break;
                case "Testing set":
                    Controller.setTesting_set(chooser.showOpenDialog(Main.getRootStage()));
                    break;
                default:
                    System.err.println("Invalid name in switch case in view");
            }
        };
    }
}
