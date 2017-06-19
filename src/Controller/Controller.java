package Controller;

import java.io.File;

/**
 * Created by ndutl on 18/06/2017.
 */
public class Controller {

    private static File training_set;
    private static File validation_set;
    private static File testing_set;

    public static void create_Neural_Net() {

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
}
