package View;

import Controller.Handlers.StartButtonHandler;
import javafx.scene.control.Button;

/**
 * Created by ndutl on 19/06/2017.
 */
public class StartButton extends Button {
    public StartButton() {
        super("start training");
        this.setOnAction(StartButtonHandler.startTraining());
    }
}
