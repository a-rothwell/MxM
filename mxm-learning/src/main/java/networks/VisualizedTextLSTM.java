package networks;

import org.deeplearning4j.api.storage.StatsStorage;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.GravesLSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.stats.StatsListener;
import org.deeplearning4j.ui.storage.InMemoryStatsStorage;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;


/**
 * Go to http://localhost:9000/train
 */
public class VisualizedTextLSTM {

    // Neural network dimensions
    private int hiddenLayerWidth;
    private int hiddenLayerCount;

    // Neural network and its configuration
    private MultiLayerConfiguration configuration;
    private MultiLayerNetwork network;
    private DataSet trainingData;

    // Specific to our text-based example
    private char[] inputString;
    private List<Character> possibleChars;

    /**
     * A constructor for this networks.TextLSTM with various initialization arguments
     * @param inputString The string we're using to train our neural net
     * @param hiddenLayerWidth The width of hidden layers to use
     * @param hiddenLayerNum The number of hidden layers to use
     */
    public VisualizedTextLSTM(String inputString, int hiddenLayerWidth, int hiddenLayerNum) {

        // Take in all of the arguments
        this.inputString        = inputString.toCharArray();
        this.hiddenLayerWidth   = hiddenLayerWidth;
        this.hiddenLayerCount   = hiddenLayerNum;

        // Make everything else null
        this.network        = null;
        this.trainingData   = null;
        this.configuration  = null;
        this.possibleChars  = null;
    }

    /**
     * Runs this neural network by running several private methods
     * which initialize, configure, build, and execute the network.
     */
    public void run() {
        // A way to measure elapsed time
        long startTime;

        // Initialize and configure the network
        startTime = System.nanoTime();
        initialize();
        System.out.println("Time elapsed: " + (System.nanoTime() - startTime)/1000000d + "ms");

        // Configure the network's data.
        startTime = System.nanoTime();
        configure();
        System.out.println("Time elapsed: " + (System.nanoTime() - startTime)/1000000d + "ms");

        // Create the actual neural network
        startTime = System.nanoTime();
        build();
        System.out.println("Time elapsed: " + (System.nanoTime() - startTime)/1000000d + "ms");

        // Set up GUI visualization stuff
        setUpGUI();

        // Train it on the provided data
        startTime = System.nanoTime();
        train();
        System.out.println("Time elapsed: " + (System.nanoTime() - startTime)/1000000d + "ms");

        // Do something with the output
    }

    private void setUpGUI() {
        UIServer uiServer = UIServer.getInstance();

        //Configure where the network information (gradients, score vs. time etc) is to be stored. Here: store in memory.
        StatsStorage statsStorage = new InMemoryStatsStorage(); //Alternative: new FileStatsStorage(File), for saving and loading later

        //Attach the StatsStorage instance to the UI: this allows the contents of the StatsStorage to be visualized
        uiServer.attach(statsStorage);

        //Then add the StatsListener to collect this information from the network, as it trains
        network.setListeners(new StatsListener(statsStorage));
    }

    /**
     * A method for initialization of our data. In here, we look
     * over our input, extract all possible values, and set them
     * aside for the configuration method.
     */
    private void initialize() {
        System.out.println("Beginning initialization...");
        // Extract all of the possible states
        LinkedHashSet<Character> possibleCharsSet = new LinkedHashSet<>();
        for (char c : inputString) {
            possibleCharsSet.add(c);
        }
        possibleChars = new ArrayList<>();
        possibleChars.addAll(possibleCharsSet);

        // Create input and output arrays
        INDArray input  = Nd4j.zeros(1, possibleChars.size(), inputString.length);
        INDArray labels = Nd4j.zeros(1, possibleChars.size(), inputString.length);

        // Loop through the input (and, for fun, assume the thing loops around
        // such that the next character after the end of the string is actually
        // the first character of the string).
        int samplePos = 0;
        for (char currentChar : inputString) {
            // Some fancy-pants math over here to do the above
            char nextChar = inputString[(samplePos + 1) % (inputString.length)];
            // The input neuron for current-char is 1 at "samplePos"
            input.putScalar(new int[] { 0, possibleChars.indexOf(currentChar), samplePos }, 1);
            // The output neuron for next-char is 1 at "samplePos"
            labels.putScalar(new int[] { 0, possibleChars.indexOf(nextChar), samplePos }, 1);
            samplePos++;
        }
        trainingData = new DataSet(input, labels);
        System.out.println(input.toString());
        System.out.println("...completed initialization.");
    }

    /**
     * An encapsulation method for configuring the neural net.
     * This method is gigantic by necessity as DL4J has individual
     * initialization methods per variable. Note that this method's
     * only tangible outcome is that it creates "configuration."
     */
    private void configure() {
        System.out.println("Beginning configuration...");
        // Create and initialize a NeuralNetConfiguration.Builder
        // This establishes all the settings for the neural network
        NeuralNetConfiguration.Builder builder = new NeuralNetConfiguration.Builder();
        builder.iterations(10);
        builder.learningRate(0.001);
        builder.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT);
        builder.seed(123);
        builder.biasInit(0);
        builder.miniBatch(false);
        builder.updater(Updater.RMSPROP);
        builder.weightInit(WeightInit.XAVIER);

        // A mouthful of a class that essentially turns the configuration
        // builder we've initialized into a listBuilder, which is capable
        // of doing stuff for multi-layered neural networks
        NeuralNetConfiguration.ListBuilder listBuilder = builder.list();

        // Set up inputs and outputs for each layer.
        for(int i = 0; i < hiddenLayerCount; i++) {
            // Create and initialize a GravesLSTM.Builder
            // This establishes each layer's settings
            GravesLSTM.Builder hiddenLayerBuilder = new GravesLSTM.Builder();
            if(i == 0) {
                hiddenLayerBuilder.nIn(possibleChars.size());
            }
            else {
                hiddenLayerBuilder.nIn(hiddenLayerWidth);
            }
            hiddenLayerBuilder.nOut(hiddenLayerWidth);
            hiddenLayerBuilder.activation("tanh");

            // Builds and adds this layer with index "i"
            listBuilder.layer(i, hiddenLayerBuilder.build());
        }

        // Create and initialize the RnnOutputLayer.Builder,
        // which does exactly what you'd think it does
        RnnOutputLayer.Builder outputLayerBuilder = new RnnOutputLayer.Builder(LossFunctions.LossFunction.MCXENT);
        outputLayerBuilder.activation("softmax");
        outputLayerBuilder.nIn(hiddenLayerWidth);
        outputLayerBuilder.nOut(possibleChars.size());
        listBuilder.layer(hiddenLayerCount, outputLayerBuilder.build());

        // Finish the last two settings for the now-infamous
        // NeuralNetConfiguration.ListBuilder
        listBuilder.pretrain(false);
        listBuilder.backprop(true);

        // Construct our MultiLayerConfig off of the our favorite
        // NeuralNetConfiguration.ListBuilder.
        configuration = listBuilder.build();
        System.out.println("...completed configuration.");
    }

    /**
     * Builds the actual network, utilizing the configuration we
     * already have, and initializes it. The last step before any
     * sort of training is done.
     */
    private void build() {
        System.out.println("Beginning building...");
        // Construct a neural network off of the configuration,
        // then initialize it and set how often it should print.
        network = new MultiLayerNetwork(configuration);
        network.init();
        network.setListeners(new ScoreIterationListener(10));
        System.out.println("...completed building.");
    }

    /**
     * A method to encapsulate the training process
     */
    private void train() {
        System.out.println("Beginning training...");

        // Each epoch is another iteration of training
        for (int epoch = 0; epoch < 100; epoch++) {
            System.out.println("Epoch " + epoch);

            // We're going to fit the network to how well it models training data-
            // i.e. how well it can take inputs and predict labels (outputs)
            network.fit(trainingData);

            // We need to clear the state (weightings) of the previous epoch
            network.rnnClearPreviousState();

            // Initialize this LSTM with the first value of the training data
            INDArray testInit = Nd4j.zeros(possibleChars.size());
            testInit.putScalar(possibleChars.indexOf(inputString[0]), 1);

            // Since this is an RNN, we need to call rnnTimeStep(), not output().
            // This is because rnnTimeStep() is optimized to save previous inputs,
            // rather than needing to load everything each time we predict a new
            // output. Note that we start with the training data's first entry
            INDArray output = network.rnnTimeStep(testInit);

            // Now we predict inputstring.length more characters (the same length
            // as our training data, so network.fit(trainingData) can compare.
            for (int j = 0; j < inputString.length; j++) {

                // We must choose an output from the probability distribution, i.e.
                // if 'e' had the highest value, we choose 'e' as our output.
                double[] outputProbDistribution = new double[possibleChars.size()];
                for (int k = 0; k < outputProbDistribution.length; k++) {
                    outputProbDistribution[k] = output.getDouble(k);
                }
                int sampledCharacterIdx = findIndexOfHighestValue(outputProbDistribution);

                // Print what output we chose
                System.out.print(possibleChars.get(sampledCharacterIdx));

                // Use this output as the next input by placing it in a one-dimensional
                // nextInput array, with 0s for everything we didn't choose.
                INDArray nextInput = Nd4j.zeros(possibleChars.size());
                nextInput.putScalar(sampledCharacterIdx, 1);

                // Repeat the process with the new output becoming input
                output = network.rnnTimeStep(nextInput);
            }
            System.out.print("\n");
            System.out.println("...completed training.");
        }
    }

    private static int findIndexOfHighestValue(double[] distribution) {
        int maxValueIndex = 0;
        double maxValue = 0;
        for (int i = 0; i < distribution.length; i++) {
            if(distribution[i] > maxValue) {
                maxValue = distribution[i];
                maxValueIndex = i;
            }
        }
        return maxValueIndex;
    }
}