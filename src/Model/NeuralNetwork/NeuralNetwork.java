package Model.NeuralNetwork;

import Controller.Controller;
import Jama.Matrix;
import Model.NeuralNetwork.Layers.HiddenLayer;
import Model.NeuralNetwork.Layers.InputLayer;
import Model.NeuralNetwork.Layers.OutputLayer;
import javafx.application.Platform;

import java.io.*;
import java.lang.management.PlatformLoggingMXBean;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import java.util.regex.Pattern;


/**
 * Represents a neural network containing one input layer with 3 inputs,
 * two hidden layer with a variable number of neurons and one output layer
 * containing a variable amount of output neurons (depending on how many colors are to be detected).
 * <p>
 * <p>This project was created for the ROB[SP17] course in an attempt to better differentiate colors
 * when using the E-puck camera.</p>
 * <p>
 * <p><b>Note:</b>The  external libraries used are <a href="http://math.nist.gov/javanumerics/jama/">JAMA</a>,
 * which provides basic linear algebra functions and <a href="https://commons.apache.org/proper/commons-cli/">apache-commons-cli</a> for the CLI argument parser.
 * In this project, JAMA is mostly used to store information
 * in matrices, which makes the calculations more readable.</p>
 * <p>
 * Network type: FFANN  (Feed forward artificial neural network)<br>
 * BP Algorithm: SGD (Stochastic gradient descent)<br>
 * Learning type: Supervised<br>
 * </p>
 * <p>
 * @author Nicolas Dutly
 * @version 1.0
 */


public class NeuralNetwork {
    /**
     * Represents the networks bias value
     * (Every neuron has an additional bias input
     * with the value of {@code BIAS})
     */
    public static final int BIAS = -1;
    /**
     * Represents the writer used to print
     * network error statistics.
     */
    private BufferedWriter err_writer = null;
    /**
     * Represents the final training classification error
     * at the end of the program run
     */
    private double finalTrainingErr;
    /**
     * Represents the final validation classification error
     * at the end of the program run
     */
    private double finalValidationErr;
    /**
     * Represents the networks input layer
     */
    private InputLayer inputLayer;
    /**
     * Represents the networks first hidden layer
     */
    private HiddenLayer hiddenLayer1;
    /**
     * Represents the networks second hidden layer
     */
    private HiddenLayer hiddenLayer2;
    /**
     * Represents the networks output layer
     */
    private OutputLayer outputLayer;
    /**
     * Represents the training data sets.
     * {@link #parseDataSets(String, String, String)}
     */
    private Matrix trainingInput, trainingOutput;
    /**
     * Represents matrices containing the validation data.
     */
    private Matrix validationInput, validationOutput;
    /**
     * Represents matrices containing the testing data.
     */
    private Matrix testingInput, testingOutput;

    /**
     * Describes how many colors are to be classified (nb of output neurons)
     */
    private int nbOfColors;

    /**
     * Write network error statistics to stats/err_writer.csv
     * if set to true (-s flag)
     */
    private boolean writeStats;

    /**
     * Contains color definition in the testing set
     */
    private String[] colorNames = null;

    /**
     * Contains the current training progress percentage
     */
    private double percentage = 0;
    /**
     * Create a neural network with the following topology:
     * Input Layer: 3 input nodes (R/G/B) + bias
     * 2 hidden Layer with a variable amount of neurons (-n flag)
     * Output Layer: variable amount of neurons (-c flag)
     *
     * @param nbOfHiddenNeurons specifies the number of hidden layer neurons
     * @param nbOfColors        specifies how many colors are to be recognized
     */
    public NeuralNetwork(int nbOfHiddenNeurons, int nbOfColors) {
        this.nbOfColors = nbOfColors;
        //note the input layer bias is created in the InputLayer class
        this.inputLayer = new InputLayer(3);
        this.hiddenLayer1 = new HiddenLayer(nbOfHiddenNeurons);
        this.hiddenLayer2 = new HiddenLayer(nbOfHiddenNeurons);

        this.hiddenLayer1.setPrevLayer(inputLayer);
        this.hiddenLayer1.setNextLayer(this.hiddenLayer2);
        this.hiddenLayer2.setPrevLayer(hiddenLayer1);

        this.outputLayer = new OutputLayer(nbOfColors);
        this.outputLayer.setPrevLayer(hiddenLayer2);

        this.hiddenLayer2.setNextLayer(this.outputLayer);

    }

    /**
     * Queries the neural network using following parameters
     *
     * @param normalized indicates whether the passed values are already normalized
     * @param red        the red rgb value [0-255]
     * @param green      the green rgb value [0-255]
     * @param blue       the blue rgb value [0-255]
     * @return the output array where the first element is the red probability,
     * green the second and blue the third. (Note that array positions might change
     * if the training color order is swapped {@link #parseDataSets(String, String, String)})
     */
    public double[] query(boolean normalized, double red, double green, double blue) {
        //normalize between [-1,1]
        if (!normalized) {
            red = 2 * (red / 255) - 1;
            green = 2 * (green / 255) - 1;
            blue = 2 * (blue / 255) - 1;
        }
        inputLayer.setInputs(new double[]{red, green, blue, BIAS});
        hiddenLayer1.process();
        hiddenLayer2.process();
        outputLayer.process();
        return outputLayer.getOutputVector();
    }

    /**
     * Trains, validates and tests the neural network.
     *
     * @param nbOfEpochs number of cycles that the training matrix is
     *                   to be fed through the network.
     * @throws Exception if the training input matrix is null.
     * @see #train()
     * @see #validate()
     * @see #test()
     * @see #write_statistics(int, int)
     */
    public void init(int nbOfEpochs) throws Exception {
        if (this.trainingInput == null) {
            throw new Exception("Error: Training data was no parsed. Was parseTrainingSet() called?");
        }
        try {
            for (int i = 0; i < nbOfEpochs; i++) {
                printProgress(nbOfEpochs, i);
                train();
                validate();
                write_statistics(i, nbOfEpochs);

            }
        } catch (IOException e) {
            System.err.println("An error occurred while writing stats to file:");
            System.err.println(e.getMessage());
        } finally {
            if (writeStats) {
                err_writer.close();
            }
        }
        test();
    }

    /**
     * Print the current training progress percentage
     *
     * @param nbOfEpochs   total number of epochs
     * @param currentEpoch elapsed epochs
     */
    private void printProgress(int nbOfEpochs, int currentEpoch) {
        this.percentage = (100. / (double) nbOfEpochs) * currentEpoch;
        Platform.runLater(()->Controller.updateProgress(this.percentage));
        System.out.printf("\r[%.2f%%]", this.percentage);
    }

    /**
     * Tests the network performance under realistic circumstances.
     * <p>
     * Testing takes place at the very end of the program, only if a testing set is provided. A training set consists
     * of data not present in training and validation sets. The data present in the testing set is passed through the
     * network. The result gives an indication of the overall network performance.
     */
    private void test() {
        if (testingInput == null) return;
        System.out.printf("\n============================================BEGIN TESTING===================================\n");
        System.out.printf("\nProbability vector order: %s\n\n", Arrays.toString(colorNames));
        for (int row2 = 0; row2 < testingInput.getRowDimension(); row2++) {
            String color = "";
            try {
                /*To lower amount of CLI options needed, these values must be adapted if one wishes
                to train other colors.*/
                for (int i = 0; i < testingOutput.getColumnDimension(); i++) {
                    if (testingOutput.get(row2, i) == 1) {
                        color = colorNames[i];
                    }
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                System.err.println("An error occurred during testing. Adapt color definitions in source file");
            }
            double[] results = query(true, testingInput.get(row2, 0), testingInput.get(row2, 1), testingInput.get(row2, 2));
            System.out.printf("Color should be %s, output vector is:\t\t%s\n", color, Arrays.toString(results));
        }
        System.out.printf("=====================================END TESTING==================================\n");
    }

    /**
     * Writes network error statistics to stats/err_writer.csv
     * (percentage of false classifications)
     * @param currentEpoch the current epoch
     * @param nbOfEpochs   total number of epochs
     * @throws IOException on IO exception when accessing stats/error_stats.csv
     */
    private void write_statistics(int currentEpoch, int nbOfEpochs) throws IOException {
        if (!writeStats) return;
        //validation classification error
        double validation_error = outputLayer.get_class_err(false);
        //training classification error
        double training_error = outputLayer.get_class_err(true);
        //write statistics to file
        File errFile;
        if (err_writer == null) {
            File statsFolder = new File("stats");
            statsFolder.mkdir();
            errFile = new File("stats/error_stats.csv");
            err_writer = new BufferedWriter(new FileWriter(errFile));
            if (validationInput != null) {
                err_writer.write("Epoch,Training Error,Validation Error\n");
            } else {
                err_writer.write("Epoch,Training Error\n");
            }
        }
        err_writer.write(String.format("%d,%f", currentEpoch, training_error));
        if (validationInput != null) err_writer.write(String.format(",%f\n", validation_error));
        else err_writer.write("\n");
        if (currentEpoch == nbOfEpochs - 1) {
            finalTrainingErr = training_error;
            finalValidationErr = validation_error;
        }
    }

    /**
     * Validates the network output by using a validation set
     * <p>
     * Validation only takes place if a validation set is provided. A validation set consists of data not present
     * in the training set. When used in conjunction with the (-s) flag, allows to compare the training and validation
     * error. If the number of epochs is to high or if the amount of neurons in the hidden layers is to high, overfitting
     * will occur (the validation error will increase while the training error decreases). This loss of generalisation can
     * be avoided by observing the network statistics and reducing the number of epochs and/or hidden layer neurons.
     * <p>
     * Note that no backpropagation takes place.
     *
     * @see #init(int)
     */
    private void validate() {
        if (validationInput != null) {
            //order does not matter on validation
            for (int row3 = 0; row3 < validationInput.getRowDimension(); row3++) {
                inputLayer.setInputs(new double[]{validationInput.get(row3, 0), validationInput.get(row3, 1), validationInput.get(row3, 2), BIAS /*bias*/});
                outputLayer.setTrainingOutput(validationOutput.getArray()[row3]);
                hiddenLayer1.process();
                hiddenLayer2.process();
                outputLayer.process();
                outputLayer.calc_class_err(false);
            }
        }
    }

    /**
     * Feeds a the training set through the neural network,
     * then back-propagates the error and adjusts the weights.
     * Called once during every epoch.
     * @see #init(int)
     */
    private void train() {

        Random rnd = new Random();
        int row;
        //randomize line order to improve training
        ArrayList<Integer> processedLines = new ArrayList<>();
        row = rnd.nextInt(trainingInput.getRowDimension());

        for (int n = 0; n < trainingInput.getRowDimension(); n++) {

            //don't train the same row twice during one epoch
            while (processedLines.contains(row)) row = rnd.nextInt(trainingInput.getRowDimension());
            processedLines.add(row);
            //set the networks training input and training outputs
            inputLayer.setInputs(new double[]{trainingInput.get(row, 0), trainingInput.get(row, 1), trainingInput.get(row, 2), BIAS});
            outputLayer.setTrainingOutput(trainingOutput.getArray()[row]);

            //forward phase
            hiddenLayer1.process();
            hiddenLayer2.process();
            outputLayer.process();

            //classification error
            outputLayer.calc_class_err(true);

            //Back propagation
            outputLayer.calculate_delta();
            hiddenLayer2.calculate_delta();
            hiddenLayer1.calculate_delta();

            outputLayer.adjustLayerWeights();
            hiddenLayer2.adjustLayerWeights();
            hiddenLayer1.adjustLayerWeights();
        }
        processedLines.clear();
    }

    /**
     * Parses a csv file, which is in following format: <br >
     * {@code r,g,b;x1,x2,x3,...,xn}<br>
     * With one of the {@code xi} set to one. Where <br>
     * {@code r}: red value [0-255]<br>
     * {@code g}: green value [0-255]<br>
     * {@code b}: blue value [0-255]<br>
     * {@code x1}: set to 1 if color is red<br>
     * {@code x2}: set to 1 if color is green<br>
     * {@code x3}: set to 1 if color is blue<br>
     * (Note: the order of the {@code xi} can be interchanged, this will result
     * in a change of the output vector {@link #write_C_source(String)})
     * The csv values are then inserted into the following Matrices<br>
     * <b>Note:</b>The first line of the testing set *must* contain the names of the colors
     * to that are to be recognized, in the correct order and separated by commas (see testing
     * set example)
     * {@link #trainingInput}
     * {@link #trainingOutput}
     * {@link #validationInput}
     * {@link #validationOutput}
     * {@link #testingInput}
     * {@link #testingOutput}
     *
     * @param trainingSet csv file path of the training set
     * @param validationSet csv file path of the validation set (optional)
     * @param testingSet csv file path of the testing set (optional)
     * @throws IOException if an IO exception occurs
     * @throws Exception   if the csv file has an incorrect format.
     */
    public void parseDataSets(String trainingSet, String validationSet, String testingSet) throws Exception {

        ArrayList<String> files = new ArrayList<>();
        files.add(trainingSet);

        //optional
        if (validationSet == null) files.add("");
        else files.add(validationSet);
        if (testingSet == null) files.add("");
        else files.add(testingSet);

        double[][] inputArray = null;
        double[][] outputArray = null;
        int nbOfLines = 0;

        for (int k = 0; k < files.size(); k++) {
            if (files.get(k).equals("")) continue;
            File dataSet = new File(files.get(k));
            BufferedReader bufferedReader = null;
            LineNumberReader lineNumberReader;
            try {
                lineNumberReader = new LineNumberReader(new FileReader(dataSet));
                //get the number of lines
                lineNumberReader.skip(Long.MAX_VALUE);
                nbOfLines = lineNumberReader.getLineNumber() + 1;
                bufferedReader = new BufferedReader(new FileReader(dataSet));
            } catch (FileNotFoundException e) {
                System.err.printf("error: %s%n", e.getMessage());
                System.exit(-1);
            }
            try {
                String currentLine;
                String[] tmpInputLines = new String[nbOfLines];
                String[] tmpOutputLines = new String[nbOfLines];
                inputArray = new double[nbOfLines][3];
                outputArray = new double[nbOfLines][this.nbOfColors];
                int i = 0;
                boolean headerParsed = false;
                while ((currentLine = bufferedReader.readLine()) != null) {
                    if (k == 2 && !headerParsed) {
                        headerParsed = true;
                        colorNames = currentLine.split(",");
                        currentLine = bufferedReader.readLine();
                        for (String colorName : colorNames) {
                            if (Pattern.compile("[0-9]+").matcher(colorName).find()) {
                                System.err.println("\nInvalid test color header. The test file must" +
                                        "contain a header line containing color names separated by commas.");
                                System.exit(-1);
                            }
                        }
                    }
                    while (currentLine.equals("")) {
                        currentLine = bufferedReader.readLine();
                    }
                    tmpInputLines[i] = currentLine.split(";")[0];
                    tmpOutputLines[i] = currentLine.split(";")[1];
                    String[] colorVals = tmpInputLines[i].split(",");
                    String[] outputVals = tmpOutputLines[i].split(",");
                    //get rgb values and normalize
                    double red = Double.valueOf(colorVals[0]);
                    double rednorm = 2 * (red / 255.) - 1;
                    double green = Double.valueOf(colorVals[1]);
                    double greennorm = 2 * (green / 255.) - 1;
                    double blue = Double.valueOf(colorVals[2]);
                    double bluenorm = 2 * (blue / 255.) - 1;
                    inputArray[i] = new double[]{rednorm, greennorm, bluenorm};

                    for (int j = 0; j < outputVals.length; j++) {
                        outputArray[i][j] = Double.valueOf(outputVals[j]);
                    }
                    i++;
                }
            } catch (IOException e) {
                System.err.printf("IO error: %s%n", e.getMessage());
            } catch (ArrayIndexOutOfBoundsException e) {
                System.err.println("error: passed csv file has bad format\n");
                System.exit(0);
            } finally {
                bufferedReader.close();
            }
            switch (k) {
                case 0:
                    this.trainingInput = new Matrix(inputArray);
                    this.trainingOutput = new Matrix(outputArray);
                    break;
                case 1:
                    this.validationInput = new Matrix(inputArray);
                    this.validationOutput = new Matrix(outputArray);
                    break;
                case 2:
                    this.testingInput = new Matrix(inputArray);
                    this.testingOutput = new Matrix(outputArray);
                    break;
                default:
                    System.err.println("Error while parsing data sets");
                    System.exit(0);
            }
        }


    }
//------------------------------------------C source writer---------------------------------------------------------

    /**
     * Prints out the query formula for the neural network in a c file that can then be compiled
     * with the other project files and can be run independently from the java program.
     *
     * @param filename the c source file to write to.
     */
    public void write_C_source(String filename) {
        String pattern = "dd.MM.yyyy 'at' HH:mm:ss";
        String timeStamp = new SimpleDateFormat(pattern).format(new Date());
        double[][] hlWeightMatrix = new double[hiddenLayer1.getNbOfNeurons() - 1][inputLayer.getNbOfNeurons()];
        //---------------------------------------Generate hidden Layer 1 weight matrix----------------------------------------
        String hlwMatrix1 = String.format("double hl1WeightMatrix1[%d][%d]", hiddenLayer1.getNbOfNeurons() - 1, inputLayer.getNbOfNeurons());
        StringBuilder sb = new StringBuilder(hlwMatrix1);
        sb.append(" = {");
        for (int i = 0; i < hiddenLayer1.getNbOfNeurons() - 1; i++) {
            sb.append("{");
            for (int j = 0; j < inputLayer.getNbOfNeurons(); j++) {
                hlWeightMatrix[i][j] = hiddenLayer1.getNeurons()[i].getSynapse_weights().get(j, 0);
                sb.append(hlWeightMatrix[i][j]);
                sb.append(",");
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append("},");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append("};\n");
        hlwMatrix1 = sb.toString();
        //---------------------------------------Generate hidden Layer 2 weight matrix----------------------------------------
        double[][] hl2WeightMatrix = new double[hiddenLayer2.getNbOfNeurons() - 1][hiddenLayer1.getNbOfNeurons()];
        String hlwMatrix2 = String.format("double hl2WeightMatrix1[%d][%d]", hiddenLayer2.getNbOfNeurons() - 1, hiddenLayer1.getNbOfNeurons());
        StringBuilder sbhl2 = new StringBuilder(hlwMatrix2);
        sbhl2.append(" = {");
        for (int i = 0; i < hiddenLayer2.getNbOfNeurons() - 1; i++) {
            sbhl2.append("{");
            for (int j = 0; j < hiddenLayer1.getNbOfNeurons(); j++) {
                hl2WeightMatrix[i][j] = hiddenLayer2.getNeurons()[i].getSynapse_weights().get(j, 0);
                sbhl2.append(hl2WeightMatrix[i][j]);
                sbhl2.append(",");
            }
            sbhl2.deleteCharAt(sbhl2.length() - 1);
            sbhl2.append("},");
        }
        sbhl2.deleteCharAt(sbhl2.length() - 1);
        sbhl2.append("};\n");
        hlwMatrix2 = sbhl2.toString();
        //---------------------------------------Generate output Layer weight matrix----------------------------------------
        double[][] outWeightMatrix = new double[outputLayer.getNbOfNeurons()][hiddenLayer1.getNbOfNeurons()];
        String outwMatrix = String.format("double outWeightMatrix1[%d][%d]", outputLayer.getNbOfNeurons(), hiddenLayer1.getNbOfNeurons());
        StringBuilder sb2 = new StringBuilder(outwMatrix);
        sb2.append(" = {");
        for (int i = 0; i < outputLayer.getNbOfNeurons(); i++) {
            sb2.append("{");
            for (int j = 0; j < hiddenLayer1.getNbOfNeurons(); j++) {
                outWeightMatrix[i][j] = outputLayer.getNeurons()[i].getSynapse_weights().get(j, 0);
                sb2.append(outWeightMatrix[i][j]);
                sb2.append(",");
            }
            sb2.deleteCharAt(sb2.length() - 1);
            sb2.append("},");
        }
        sb2.deleteCharAt(sb2.length() - 1);
        sb2.append("};\n");
        outwMatrix = sb2.toString();
        //----------------------------------generate C file-----------------------------------------------------------------
        File cFile = new File(filename);
        BufferedWriter cSourceWritter = null;
        try {
            cSourceWritter = new BufferedWriter(new FileWriter(cFile));
            String cSource =
                    "/*------------------------------------------------------------------------------------------------" +
                            "\nThis file was automatically generated by " + this.getClass().getSimpleName() + "\n" +
                            "Gen time: " + timeStamp + ". Use -lm flag when compiling to get access to the exp() function from math.h\n" +
                            "This function returns an array of doubles indicating the probability of each color (same order as the)\n" +
                            "training data set (eg: r,g,b,v,c)\n" +
                            "--------------------------------------------------------------------------------------------------*/\n\n\n" +
                            "#include <math.h>\n" +
                            "double hiddenOut1[" + hiddenLayer1.getNbOfNeurons() + "];\n" +
                            "double hiddenOut2[" + hiddenLayer2.getNbOfNeurons() + "];\n" +
                            "double out["+outputLayer.getNbOfNeurons()+"];\n" +
                            hlwMatrix1 +
                            hlwMatrix2 +
                            outwMatrix +
                            "double* query(double r, double g, double b);" +
                            "\n" +
                            "double* query(double r, double g, double b){\n" +
                            "    r = 2 * (r / 255.) - 1;\n" +
                            "    g = 2 * (g / 255.) - 1;\n" +
                            "    b = 2 * (b / 255.) - 1;\n" +
                            "\n" +
                            "    double sum = 0;\n" +
                            "    int i = 0;\n" +
                            "    int j = 0;\n" +
                            "    int n = 0;\n" +
                            "    double inputs[4] = {r,g,b," + BIAS + "};\n" +
                            "\n" +
                            "    for(i = 0; i < " + hiddenLayer1.getNbOfNeurons() + " - 1; i++){\n" +
                            "        sum = 0;\n" +
                            "        for(j = 0; j < " + inputLayer.getNbOfNeurons() + "; j++){\n" +
                            "            sum += inputs[j] * hl1WeightMatrix1[i][j];\n" +
                            "        }\n" +
                            "        hiddenOut1[i] = 1. / (1 + exp(-sum));\n" +
                            "    }\n" +
                            "\n" +
                            "    hiddenOut1[" + hiddenLayer1.getNbOfNeurons() + " - 1] = " + BIAS + ";\n" +
                            "    for(i = 0; i < " + hiddenLayer2.getNbOfNeurons() + "-1;i++){\n" +
                            "        sum = 0;\n" +
                            "        for(j = 0; j < " + hiddenLayer1.getNbOfNeurons() + "; j++){\n" +
                            "            sum += hiddenOut1[j] * hl2WeightMatrix1[i][j];\n" +
                            "        }\n" +
                            "        hiddenOut2[i] = 1. / (1 + exp(-sum));\n" +
                            "    }hiddenOut2[" + hiddenLayer2.getNbOfNeurons() + "-1] = -1 ;\n" +
                            "    double weightedInput[" + outputLayer.getNbOfNeurons() + "];\n" +
                            "    double softmaxsum = 0;\n" +
                            "    for(i = 0; i < " + outputLayer.getNbOfNeurons() + "; i++){\n" +
                            "        sum = 0;\n" +
                            "        for(j = 0; j < " + hiddenLayer2.getNbOfNeurons() + "; j++){\n" +
                            "            sum += hiddenOut2[j] * outWeightMatrix1[i][j];\n" +
                            "        }\n" +
                            "        weightedInput[i] = sum;\n" +
                            "        softmaxsum += exp(weightedInput[i]);\n" +
                            "    }\n" +
                            "    for(n = 0; n < " + outputLayer.getNbOfNeurons() + "; n++){\n" +
                            "        out[n] = exp(weightedInput[n]) / softmaxsum;\n" +
                            "        out[n] = roundf(out[n] * 100) / 100;\n" +
                            "    }\n" +
                            "    return out;\n" +
                            "}";

            cSourceWritter.write(cSource);
            cSourceWritter.close();

        } catch (IOException e) {
            System.out.println("IO error occurred while trying to write C source: " + e.getMessage());
        } finally {
            if (cSourceWritter != null) {
                try {
                    cSourceWritter.close();
                } catch (IOException e) {
                    System.out.println("Error closing C source: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Setter for {@link #writeStats}
     *
     * @param writeStats set to true if error stats are to be written to
     *                   stats/err_writer.csv
     */
    public void setWriteStats(boolean writeStats) {
        this.writeStats = writeStats;
    }

    /**
     * @return final training classification (% of missed classifications over the entire training set)
     */
    public double getFinalTrainingErr() {
        return finalTrainingErr;
    }

    /**
     * @return final training classification (% of missed classifications over the entire validation set)
     */
    public double getFinalValidationErr() {
        return finalValidationErr;
    }

    /**
     * @return the current training percentage
     */
    public double getPercentage() {
        return percentage;
    }
}
