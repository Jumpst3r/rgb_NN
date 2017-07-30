package Controller;

import Model.NeuralNetwork.NeuralNetwork;
import View.View;

import java.io.File;

/**
 * Created by ndutl on 18/06/2017.
 */
public class Controller {

    private static File training_set;
    private static File validation_set;
    private static File testing_set;
    private static boolean stats;
    private static int nbOfColors;
    private static int nbOfHiddenNeurons;
    private static int nbOfEpochs;


    public static void create_Neural_Net() {
        new Thread(()->{
            NeuralNetwork neuralNetwork = new NeuralNetwork(nbOfHiddenNeurons, nbOfColors);
            neuralNetwork.setWriteStats(stats);
            try {
                neuralNetwork.parseDataSets(training_set.getAbsolutePath(),validation_set.getAbsolutePath(),testing_set.getAbsolutePath());
            } catch (Exception e) {
                System.err.println("Invalid csv format");
            }
            try {
                neuralNetwork.init(nbOfEpochs);
            } catch (Exception e) {
                System.err.println("Failed loading training inputs");
            }
            neuralNetwork.write_C_source("out.c");

        }).run();
    }

    public static void updateProgress(double percentage) {
        View.getTrainingProgressBar().setProgress(percentage);
    }

    public static void setTraining_set(File file) {
        Controller.training_set = file;
    }

    public static void setValidation_set(File validation_set) {
        Controller.validation_set = validation_set;
    }

    public static void setTesting_set(File testing_set) {
        Controller.testing_set = testing_set;
    }

    public static void setStats(boolean stats) {
        Controller.stats = stats;
    }

    public static void setNbOfColors(int nbOfColors) {
        Controller.nbOfColors = nbOfColors;
    }

    public static void setNbOfHiddenNeurons(int nbOfHiddenNeurons) {
        Controller.nbOfHiddenNeurons = nbOfHiddenNeurons;
    }

    public static void setNbOfEpochs(int nbOfEpochs) {
        Controller.nbOfEpochs = nbOfEpochs;
    }
}
