package LLM.experiments;

public final class QueryComponentWeights {

    public static final double CUBE = 10.0;
    public static final double AGGREGATE = 15.0;
    public static final double MEASURE = 15.0;
    public static final double GAMMA = 30.0;
    public static final double SIGMA = 30.0;

    public static final double TOTAL =
            CUBE
                    + AGGREGATE
                    + MEASURE
                    + GAMMA
                    + SIGMA;

    private QueryComponentWeights() {
        // Utility class
    }
}