package LLM.experiments.runner;

public class LLMModelConfig {

    private final String modelName;
    private final int numCtx;
    private final int numPredict;
    private final double temperature;
    private final int topK;
    private final double topP;
    private final String keepAlive;
    private final boolean streamEnabled;

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
        this.modelName = safeString(modelName);
        this.numCtx = numCtx;
        this.numPredict = numPredict;
        this.temperature = temperature;
        this.topK = topK;
        this.topP = topP;
        this.keepAlive = safeString(keepAlive);
        this.streamEnabled = streamEnabled;
    }

    public static LLMModelConfig createDefault(String modelName) {
        return new LLMModelConfig(
                modelName,
                4096,
                96,
                0.0,
                10,
                0.8,
                "30m",
                true
        );
    }

    public static LLMModelConfig createFast(String modelName) {
        return new LLMModelConfig(
                modelName,
                4096,
                80,
                0.0,
                10,
                0.8,
                "30m",
                true
        );
    }

    public static LLMModelConfig createNonStreamed(String modelName) {
        return new LLMModelConfig(
                modelName,
                4096,
                128,
                0.0,
                10,
                0.8,
                "30m",
                false
        );
    }

    public static LLMModelConfig createStreamed(String modelName) {
        return new LLMModelConfig(
                modelName,
                4096,
                96,
                0.0,
                10,
                0.8,
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
                0.0,
                10,
                0.8,
                "30m",
                streamEnabled
        );
    }

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

    public String getSafeFileName() {
        return modelName
                .replace(":", "_")
                .replace("/", "_")
                .replace("\\", "_")
                .replace(" ", "_");
    }

    private static String safeString(String value) {
        if (value == null) {
            return "";
        }

        return value.trim();
    }

    @Override
    public String toString() {
        return "LLMModelConfig{" +
                "modelName='" + modelName + '\'' +
                ", numCtx=" + numCtx +
                ", numPredict=" + numPredict +
                ", temperature=" + temperature +
                ", topK=" + topK +
                ", topP=" + topP +
                ", keepAlive='" + keepAlive + '\'' +
                ", streamEnabled=" + streamEnabled +
                '}';
    }
}