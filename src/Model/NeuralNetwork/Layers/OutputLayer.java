package Model.NeuralNetwork.Layers;

import Jama.Matrix;
import Model.NeuralNetwork.Neuron.Neuron;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the networks output layer
 *
 * @author Nicolas Dutly
 */
public class OutputLayer extends Layer {

    /**
     * Represents the training output.
     * Array's length is equal to the number
     * of neurons in the layer
     *
     * @see #nbOfNeurons
     */
    private double[] trainingOutput;
    /**
     * Represents the number of correct classifications performed on the
     * training set during the current epoch.
     */
    private double tr_correct = 0;
    /**
     * Represents the number of correct classifications performed on the
     * validation set during the current epoch.
     */
    private double valid_correct = 0;
    /**
     * Represents the number of training lines processed during the current epoch.
     */
    private double proccessed_training_sets = 0;
    /**
     * Represents the number of validation lines processed during the current epoch.
     */
    private double proccessed_validation_sets = 0;

    /**
     * Creates an output layer with {@code nbOfColors} neurons
     *
     * @param nbOfColors number of neurons in the layer
     */
    public OutputLayer(int nbOfColors) {
        super(--nbOfColors /*compensate bias*/);
    }

    /**
     * Generates the output layer's neurons,
     * without adding a bias neuron.
     */
    @Override
    protected void generateNeurons() {
        neurons = new Neuron[nbOfNeurons];
        for (int i = 0; i < nbOfNeurons; i++) {
            neurons[i] = new Neuron(prevLayer.getNbOfNeurons());
        }
    }

    /**
     * Returns the output layer's output,
     * represented in an array where the first
     * element is the output of the first neuron
     * of the output layer, the second the second's
     * and so on.
     *
     * @return an array containing the neurons output, rounded on the second decimal values
     */
    public double[] getOutputVector() {
        double[] tmp;
        tmp = new double[nbOfNeurons];
        for (int i = 0; i < nbOfNeurons; i++) {
            tmp[i] = this.neurons[i].getNeuronOutput();
            tmp[i] = Math.round(tmp[i] * 100);
            tmp[i] = tmp[i] / 100;
        }
        return tmp;
    }

    /**
     * Calculates the delta of the output layer using the
     * back-propagation algorithm (Using stochastic gradient descent)
     * As the output layer uses a softmax function, this method differs
     * from the one in the {@code HiddenLayer}
     */
    @Override
    public void calculate_delta() {
        double deltaArray[];
        deltaArray = new double[nbOfNeurons];
        for (int k = 0; k < nbOfNeurons; k++) {
            //note the different derivative (a soft-max function is used in the output layer)
            deltaArray[k] = (this.trainingOutput[k] - neurons[k].getNeuronOutput());
        }
        this.delta = new Matrix(deltaArray, nbOfNeurons);

    }

    /**
     * Activates the neurons in the layer and overrides their output with the output of a
     * softmax function to get a 1-N encoding
     */
    @Override
    public void process() {
        super.process();
        override_softmax();
    }

    /**
     * Overrides the neuron's sigmoid function with a soft-max function to provide a 1 out of N
     * encoding of the output vector.
     */
    private void override_softmax() {
        double expSum = 0;
        //Override the layer's neuron output with the soft-max function:
        //sum up exp(weightedIn) of the layer's neurons
        for (int k2 = 0; k2 < nbOfNeurons; k2++) {
            expSum += Math.exp(neurons[k2].getWeightedInput());
        }
        //Override the layer's neuron outputs
        for (int k = 0; k < nbOfNeurons; k++) {
            neurons[k].setNeuronOutput(Math.exp(neurons[k].getWeightedInput()) / expSum);
        }
    }

    /**
     * Increments {@link #tr_correct} and {@link #valid_correct}
     * if the index of the highest value in the ouput vector matches
     * the index of the highest value in the training / validation
     * arrays.
     *
     * @param trainingErr Used to distinguish to which sum is to be incremented (training / validation)
     */
    public void calc_class_err(boolean trainingErr) {
        if (trainingErr) proccessed_training_sets++;
        else proccessed_validation_sets++;
        List<Double> trainingList = new ArrayList<>();
        for (double current : trainingOutput) {
            trainingList.add(current);
        }
        List<Double> outputVectorList = new ArrayList<>();
        double[] outputVector = getOutputVector();
        for (double current : outputVector) {
            outputVectorList.add(current);
        }
        int i = trainingList.indexOf(1.0);
        if (outputVectorList.indexOf(Collections.max(outputVectorList)) == i) {
            if (trainingErr) tr_correct++;
            else valid_correct++;
        }
    }

    /**
     * Used to retrieve the training or validation classification error
     * of the current epoch.
     *
     * @param trainingErr if set to true returns the training classification error, otherwise the validation
     *                    classification error
     * @return the classification error percentage (Ex: 5% means that 5% of the data lines i
     * the training or validation (depending on the param) where incorrectly classified)
     */
    public double get_class_err(boolean trainingErr) {
        double error_rate;
        if (trainingErr) {
            error_rate = (1 - (tr_correct / proccessed_training_sets)) * 100;
            proccessed_training_sets = 0;
            tr_correct = 0;
        } else {
            error_rate = (1 - (valid_correct / proccessed_validation_sets)) * 100;
            proccessed_validation_sets = 0;
            valid_correct = 0;
        }
        return error_rate;
    }

    /**
     * Specifies the layers training output
     *
     * @param trainingOutput array containing the training output
     */
    public void setTrainingOutput(double[] trainingOutput) {
        this.trainingOutput = trainingOutput;
    }


}
