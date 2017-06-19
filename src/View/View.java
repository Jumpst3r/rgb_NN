package View;

import Controller.Base.Main;
import javafx.scene.control.Label;

/**
 * Created by ndutl on 19/06/2017.
 */
public class View {

        private static NeuronTextField neuronTextField = new NeuronTextField();
        private static EpochTextField epochTextField = new EpochTextField();
        private static ColorsTextField colorsTextField = new ColorsTextField();
        private static StatCheckBox statCheckBox = new StatCheckBox();
        private static StartButton startButton = new StartButton();
        private static TrainingProgressBar trainingProgressBar = new TrainingProgressBar();


    public static void addUIElements() {
        Main.root.add(new Label("Select training set"),0,0);
        Main.root.add(new FileSelectorButton("Training set"),1,0);

        Main.root.add(new Label("Select validation set"),0,1);
        Main.root.add(new FileSelectorButton("Validation set"),1,1);

        Main.root.add(new Label("Select testing set"),0,2);
        Main.root.add(new FileSelectorButton("Testing set"),1,2);

        Main.root.add(new Label("Number of hidden neurons"), 0, 3);
        Main.root.add(neuronTextField,1,3);

        Main.root.add(new Label("Number of epochs"), 0, 4);
        Main.root.add(epochTextField,1,4);

        Main.root.add(new Label("Number of colors trained"), 0, 5);
        Main.root.add(colorsTextField,1,5);

        Main.root.add(new Label("Write statistics?"), 0, 6);
        Main.root.add(statCheckBox,1,6);

        Main.root.add(startButton, 0, 7);
        Main.root.add(trainingProgressBar,1, 7);
    }

    public static int getNeuronText() {
        return Integer.valueOf(neuronTextField.getText());
    }

    public static int getEpochText() {
        return Integer.valueOf(epochTextField.getText());
    }

    public static boolean getStatsVal() {
        return statCheckBox.isPressed();
    }

    public static int getColorText() {
        return Integer.valueOf(colorsTextField.getText());
    }

    public static TrainingProgressBar getTrainingProgressBar() {
        return trainingProgressBar;
    }
}
