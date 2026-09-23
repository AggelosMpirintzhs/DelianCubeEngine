package LLM.experiments.runner;

public class LLMModelConfig {

    /*
     * ==========================================================
     * EXPERIMENT CONSTANTS
     * ==========================================================
     */

    public static final int EXPERIMENT_SEED =
            42;

    public static final double EXPERIMENT_TEMPERATURE =
            0.0;

    public static final int EXPERIMENT_TOP_K =
            1;

    public static final double EXPERIMENT_TOP_P =
            1.0;


    /*
     * ==========================================================
     * MODEL CONFIGURATION
     * ==========================================================
     */

    private final String modelName;

    private final int numCtx;
    private final int numPredict;

    private final double temperature;

    /*
     * Fixed random seed used for reproducible generation.
     */
    private final int seed;

    private final int topK;
    private final double topP;

    private final String keepAlive;
    private final boolean streamEnabled;


    /*
     * ==========================================================
     * FULL CONSTRUCTOR
     * ==========================================================
     */

    public LLMModelConfig(
            String modelName,
            int numCtx,
            int numPredict,
            double temperature,
            int seed,
            int topK,
            double topP,
            String keepAlive,
            boolean streamEnabled
    ) {

        validate(
                modelName,
                numCtx,
                numPredict,
                temperature,
                topK,
                topP
        );

        this.modelName =
                safeString(
                        modelName
                );

        this.numCtx =
                numCtx;

        this.numPredict =
                numPredict;

        this.temperature =
                temperature;

        this.seed =
                seed;

        this.topK =
                topK;

        this.topP =
                topP;

        this.keepAlive =
                safeString(
                        keepAlive
                );

        this.streamEnabled =
                streamEnabled;
    }


    /*
     * ==========================================================
     * COMPATIBILITY CONSTRUCTOR
     *
     * Existing code using the previous constructor continues
     * to compile.
     *
     * A fixed seed is automatically applied.
     * ==========================================================
     */

    public LLMModelConfig(
            String modelName,
            int numCtx,
            int numPredict,
            double temperature,
            int topK,
            double topP,
            String keepAlive,
            boolean streamEnabled
    ) {

        this(
                modelName,
                numCtx,
                numPredict,
                temperature,
                EXPERIMENT_SEED,
                topK,
                topP,
                keepAlive,
                streamEnabled
        );
    }


    /*
     * ==========================================================
     * RECOMMENDED CONFIGURATION FOR FINAL EXPERIMENTS
     * ==========================================================
     *
     * All compared models use exactly the same generation
     * settings.
     *
     * Only modelName should normally change.
     * ==========================================================
     */

    public static LLMModelConfig createExperimentConfig(
            String modelName,
            int numCtx,
            int numPredict,
            boolean streamEnabled
    ) {

        return new LLMModelConfig(
                modelName,
                numCtx,
                numPredict,

                /*
                 * Deterministic / greedy generation.
                 */
                EXPERIMENT_TEMPERATURE,

                /*
                 * Fixed seed.
                 */
                EXPERIMENT_SEED,

                /*
                 * Only the most probable candidate is retained.
                 */
                EXPERIMENT_TOP_K,

                /*
                 * Do not additionally restrict by nucleus
                 * sampling.
                 */
                EXPERIMENT_TOP_P,

                /*
                 * Keep model loaded during its experiment batch.
                 */
                "30m",

                streamEnabled
        );
    }


    /*
     * ==========================================================
     * LEGACY / CONVENIENCE CONFIGURATIONS
     * ==========================================================
     */

    public static LLMModelConfig createDefault(
            String modelName
    ) {

        return new LLMModelConfig(
                modelName,
                4096,
                128,
                EXPERIMENT_TEMPERATURE,
                EXPERIMENT_SEED,
                EXPERIMENT_TOP_K,
                EXPERIMENT_TOP_P,
                "30m",
                true
        );
    }


    public static LLMModelConfig createFast(
            String modelName
    ) {

        return new LLMModelConfig(
                modelName,
                4096,
                80,
                EXPERIMENT_TEMPERATURE,
                EXPERIMENT_SEED,
                EXPERIMENT_TOP_K,
                EXPERIMENT_TOP_P,
                "30m",
                true
        );
    }


    public static LLMModelConfig createNonStreamed(
            String modelName
    ) {

        return new LLMModelConfig(
                modelName,
                4096,
                128,
                EXPERIMENT_TEMPERATURE,
                EXPERIMENT_SEED,
                EXPERIMENT_TOP_K,
                EXPERIMENT_TOP_P,
                "30m",
                false
        );
    }


    public static LLMModelConfig createStreamed(
            String modelName
    ) {

        return new LLMModelConfig(
                modelName,
                4096,
                128,
                EXPERIMENT_TEMPERATURE,
                EXPERIMENT_SEED,
                EXPERIMENT_TOP_K,
                EXPERIMENT_TOP_P,
                "30m",
                true
        );
    }


    public static LLMModelConfig createCustom(
            String modelName,
            int numCtx,
            int numPredict,
            boolean streamEnabled
    ) {

        return new LLMModelConfig(
                modelName,
                numCtx,
                numPredict,
                EXPERIMENT_TEMPERATURE,
                EXPERIMENT_SEED,
                EXPERIMENT_TOP_K,
                EXPERIMENT_TOP_P,
                "30m",
                streamEnabled
        );
    }


    /*
     * ==========================================================
     * GETTERS
     * ==========================================================
     */

    public String getModelName() {
        return modelName;
    }

    public int getNumCtx() {
        return numCtx;
    }

    public int getNumPredict() {
        return numPredict;
    }

    public double getTemperature() {
        return temperature;
    }

    public int getSeed() {
        return seed;
    }

    public int getTopK() {
        return topK;
    }

    public double getTopP() {
        return topP;
    }

    public String getKeepAlive() {
        return keepAlive;
    }

    public boolean isStreamEnabled() {
        return streamEnabled;
    }


    /*
     * ==========================================================
     * SAFE FILE NAME
     * ==========================================================
     */

    public String getSafeFileName() {

        return modelName
                .replace(":", "_")
                .replace("/", "_")
                .replace("\\", "_")
                .replace(" ", "_");
    }


    /*
     * ==========================================================
     * VALIDATION
     * ==========================================================
     */

    private static void validate(
            String modelName,
            int numCtx,
            int numPredict,
            double temperature,
            int topK,
            double topP
    ) {

        if (modelName == null
                || modelName.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "modelName cannot be null or empty."
            );
        }

        if (numCtx <= 0) {

            throw new IllegalArgumentException(
                    "numCtx must be greater than 0."
            );
        }

        if (numPredict <= 0) {

            throw new IllegalArgumentException(
                    "numPredict must be greater than 0."
            );
        }

        if (temperature < 0.0) {

            throw new IllegalArgumentException(
                    "temperature cannot be negative."
            );
        }

        if (topK < 1) {

            throw new IllegalArgumentException(
                    "topK must be at least 1."
            );
        }

        if (topP <= 0.0
                || topP > 1.0) {

            throw new IllegalArgumentException(
                    "topP must be greater than 0 and less than or equal to 1."
            );
        }
    }


    /*
     * ==========================================================
     * HELPERS
     * ==========================================================
     */

    private static String safeString(
            String value
    ) {

        if (value == null) {
            return "";
        }

        return value.trim();
    }


    /*
     * ==========================================================
     * TO STRING
     * ==========================================================
     */

    @Override
    public String toString() {

        return "LLMModelConfig{" +

                "modelName='"
                + modelName
                + '\'' +

                ", numCtx="
                + numCtx +

                ", numPredict="
                + numPredict +

                ", temperature="
                + temperature +

                ", seed="
                + seed +

                ", topK="
                + topK +

                ", topP="
                + topP +

                ", keepAlive='"
                + keepAlive
                + '\'' +

                ", streamEnabled="
                + streamEnabled +

                '}';
    }
}