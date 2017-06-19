package Controller.Handlers;

import Controller.Controller;
import View.View;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

/**
 * Created by ndutl on 19/06/2017.
 */
public class StartButtonHandler extends ActionEvent {
    public static EventHandler<ActionEvent> startTraining() {
        return e->{
            Controller.setNbOfHiddenNeurons(View.getNeuronText());
            Controller.setNbOfEpochs(View.getEpochText());
            Controller.setNbOfColors(View.getColorText());
            Controller.setStats(View.getStatsVal());
            Controller.create_Neural_Net();
        };
    }
}
