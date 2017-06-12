package Base;

import NeuralNetwork.NeuralNetwork;
import org.apache.commons.cli.*;

import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * Contains the CLI argument parser (uses Apache-commons)
 * See <a href="https://commons.apache.org/proper/commons-cli/javadocs/api-release/index.html">Apache-commons-cli javadoc</a>
 */
public class Main {

    //get start time
    public static long start = System.currentTimeMillis();
    //Add some color to the output, unfortunately unsupported on windows
    private static String ANSI_RESET = "\u001B[0m";
    private static String ANSI_RED = "\u001B[31m";
    private static String ANSI_GREEN = "\u001B[32m";

    //default options:
    private static final String EPOCHS = "800";
    private static final String NEURONS = "10";

    public static void main(String[] args) throws Exception {

        //avoid printing ansi codes if not supported
        if (System.getProperty("os.name").contains("Windows")) {
            ANSI_RESET = "";
            ANSI_RED = "";
            ANSI_GREEN = "";
        }
//---------------------------------------------------------------------------CLI option parsing------------------------------------------------------------------------
        Options options = new Options();
        Option training_set = new Option("t", "trainset", true, "Path to the training data set  (csv) that is is be processed");
        Option validation_set = new Option("v", "valset", true, "Path to the validation data set (csv). Used to analyze the networks performance and avoid overfitting.");

        Option testing_set = new Option("e", "testset", true, "Pass a set that contains values not present in the training and validation sets to test network performance");
        training_set.setArgName("TRAINING SET");
        Option nbOfColors = new Option("c", "colors", true, "Number of colors to be recognized");
        Option write_mse_stats = new Option("s", "stats", false, "Write network error statistics to stats/mse_stats.csv");
        Option epoch_nb = new Option("x", "epochs", true, "specify a specific number of epochs to be executed (defaults to "+EPOCHS+")");
        Option hidden_neurons = new Option("n", "neurons", true, "specify a specific number of hidden neurons (defaults to "+NEURONS+")");
        Option c_source_out = new Option("o", "csource", true, "Path to the c source that will be created. (overwrites if already existing [!])");
        nbOfColors.setRequired(true);
        nbOfColors.setArgName("nbOfColors");
        training_set.setRequired(true);
        validation_set.setArgName("VAL SET PATH");
        testing_set.setArgName("TESTING SET PATH");
        epoch_nb.setArgName("NB OF EPOCHS");
        hidden_neurons.setArgName("NEURON NUMBER");
        c_source_out.setArgName("C OUT PATH");
        validation_set.setRequired(false);
        testing_set.setRequired(false);
        write_mse_stats.setRequired(false);
        epoch_nb.setRequired(false);
        hidden_neurons.setRequired(false);
        c_source_out.setRequired(true);
        options.addOption(training_set);
        options.addOption(validation_set);
        options.addOption(testing_set);
        options.addOption(write_mse_stats);
        options.addOption(epoch_nb);
        options.addOption(hidden_neurons);
        options.addOption(c_source_out);
        options.addOption(nbOfColors);
        CommandLineParser parser = new DefaultParser();
        HelpFormatter help = new HelpFormatter();
        help.setWidth(100);
        help.setNewLine("\n");
        CommandLine cmd = null;
        String header =
                "\nCreated for the ROB[SP17] course in an attempt to differentiate  more colors with higher accuracy when using the E-puck camera," +
                        "using a feed forward 4 layer ANN (Artificial Neural Network)\nVersion: 1.0\n" +
                        "\nUsage:\n\n";
        String footer = "\nExample usages:\njava -jar rgb_NN.jar -t training_set.csv -s -c 5 -o query.c\n" +
                "java -jar rgb_NN.jar -t training_set.csv -e testing_set.csv -v validation_set.csv -c 5 -o query.c\n" +
                "java -jar rgb_NN.jar -t training_set.csv -x 800 -n 15 -c 5 -o query.c\n" +
                "\nAuthor: Nicolas Dutly - nicolas.dutly[at]unifr.ch";

        try {
            cmd = parser.parse(options, args);

        } catch (ParseException | IndexOutOfBoundsException e) {
            System.out.println(e.getMessage());
            help.printHelp("java -jar rgb_NN.jar", header, options, footer, true);
            System.exit(0);
        }

        String c_out = cmd.getOptionValue("csource");
        String tr_set = cmd.getOptionValue("t");
        int nbOfNeurons = Integer.valueOf(cmd.getOptionValue("neurons") == null ? NEURONS : cmd.getOptionValue("neurons"));
        int nbOfColorsv = Integer.valueOf(cmd.getOptionValue("c"));
        int nbOfEpochs = Integer.valueOf(cmd.getOptionValue("epochs") == null ? EPOCHS : cmd.getOptionValue("epochs"));
        String valcsv = cmd.getOptionValue("valset");
        String testcsv = cmd.getOptionValue("testset");

//-------------------------------------------------------------------------------end of option parsing-------------------------------------------------------
        NeuralNetwork neuralNetwork = new NeuralNetwork(nbOfNeurons, nbOfColorsv);
        neuralNetwork.setWriteStats(cmd.hasOption("s"));
        System.out.printf("Parsing data set(s)...");
        neuralNetwork.parseDataSets(tr_set, valcsv, testcsv);
        System.out.printf("%50s", ANSI_GREEN + "[OK]\n" + ANSI_RESET);

        System.out.printf("Training data set... \n");
        neuralNetwork.init(nbOfEpochs);

        System.out.printf("Generating C source...");
        neuralNetwork.write_C_source(c_out);
        System.out.printf("%49s", ANSI_GREEN + "[OK]\n\n" + ANSI_RESET);
        String trcol = neuralNetwork.getFinalTrainingErr() > 5 ? ANSI_RED : ANSI_GREEN;
        String valcol = neuralNetwork.getFinalValidationErr() > 8 ? ANSI_RED : ANSI_GREEN;
        System.out.println("Final classification error on training set: "+trcol+neuralNetwork.getFinalTrainingErr()+ANSI_RESET+"%");
        if (valcsv != null) {
            System.out.println("Final classification error on validation set: "+valcol+neuralNetwork.getFinalValidationErr()+ANSI_RESET+"%");
        }
        System.out.println("Increase number of epochs (-x) and/or number of neurons (-n) to further reduce the error");
        /*short snippet to format elapsed time, found here:
        https://stackoverflow.com/questions/6710094/how-to-format-an-elapsed-time-interval-in-hhmmss-sss-format-in-java*/
        long millis = System.currentTimeMillis() - start;
        String elapsed = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
        System.out.println("\nElapsed time: " + elapsed);
    }
}