package client;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.InputStream;

import java.net.HttpURLConnection;
import java.net.URL;

import LLM.prompts.PromptBuilder;
import LLM.extractor.CubeIniSchemaExtractor;
import LLM.schema.CubeReference;
import LLM.schema.CubeSchema;
import LLM.schema.DimensionSchema;
import LLM.schema.LevelAttributeSchema;
import LLM.schema.LevelSchema;
import LLM.schema.MeasureSchema;
import mainengine.IMainEngine;

/**
 * Simple client for:
 * 1) connecting to server
 * 2) initializing dataset connection
 * 3) locally loading cube schema from ini file
 * 4) building an LLM prompt from the cube schema and a natural language question
 * 5) sending the prompt to an Ollama-compatible LLM endpoint
 * 6) printing the LLM response
 * 7) printing prompt/context/token statistics
 * 8) measuring HTTP request time, even when the request fails
 */
public class LLMClient {

    private static final String HOST = "localhost";
    private static final int PORT = 2020;

    private static final String LLM_ENDPOINT =
            getRequiredEnv("HTTP_GATE");

    /*
     * Recommended:
     * Set this as environment variable:
     *
     * Windows PowerShell:
     *   $env:LLM_API_KEY="your_api_key_here"
     *
     * Or temporarily paste it here instead of "".
     */
    private static final String API_KEY =
            getRequiredEnv("API_KEY");

    private static final String LLM_MODEL =
            getEnvOrDefault("LLM_MODEL", "llama3.1:70b");

    /*
     * Since your prompt is around 1900 real tokens,
     * 4096 context is enough for now and should be faster than 16384.
     */
    private static final int OLLAMA_NUM_CTX = 4096;
    private static final int RESERVED_OUTPUT_TOKENS = 256;
    private static final int NUM_PREDICT = 128;

    private static final double WARNING_CONTEXT_USAGE_RATIO = 0.80;
    private static final boolean PRINT_RAW_LLM_JSON = false;

    private static Registry registry;

    public static void main(String[] args) throws Exception {
        registry = LocateRegistry.getRegistry(HOST, PORT);

        IMainEngine service = (IMainEngine) registry.lookup(IMainEngine.class.getSimpleName());
        if (service == null) {
            System.err.println("Server not found. Exiting...");
            System.exit(-100);
        }

        String typeOfConnection = "RDBMS";

        HashMap<String, String> userInputList = new HashMap<String, String>();
        userInputList.put("schemaName", "foodmart_reduced");
        userInputList.put("username", "CinecubesUser");
        userInputList.put("password", "Cinecubes");
        userInputList.put("cubeName", "sales");
        userInputList.put("inputFolder", "foodmart_reduced");

//        userInputList.put("schemaName", "adult");
//        userInputList.put("username", "CinecubesUser");
//        userInputList.put("password", "Cinecubes");
//        userInputList.put("cubeName", "adult");
//        userInputList.put("inputFolder", "adult");

        service.initializeConnection(typeOfConnection, userInputList);
        System.out.println("Connection is successful.");

        CubeIniSchemaExtractor extractor = new CubeIniSchemaExtractor();

        String inputFolder = userInputList.get("inputFolder");
        String cubeName = userInputList.get("cubeName");

        Path iniPath = Paths.get("InputFiles", inputFolder, cubeName + ".ini");

        if (!Files.exists(iniPath)) {
            throw new RuntimeException("INI file not found: " + iniPath.toAbsolutePath());
        }

        CubeSchema cubeSchema = extractor.extractFromFile(iniPath.toString());

        printCubeSchemaDebugInfo(cubeSchema);

        /*
         * The user question.
         */
//        String naturalLanguageQuestion = "What is the average hours per week by age?";
//        String naturalLanguageQuestion = "What is the average hours per week by native country and race for people with paid work and without post-secondary education?";
//        String naturalLanguageQuestion = "What is the average hours per week by occupation and age for people with paid work and some college education?";
        String naturalLanguageQuestion =
                "What were the store sales each month and region in California during Q3 of 1997 for Daily Paper media?";

        PromptBuilder promptBuilder = new PromptBuilder();
        String prompt = promptBuilder.buildPrompt(cubeSchema, naturalLanguageQuestion);

        PromptSizeStats promptSizeStats = PromptSizeStats.fromPrompt(prompt);
        printPromptSizeStats(promptSizeStats);

        String llmRawResponse = sendPromptToLLM(prompt);

        if (PRINT_RAW_LLM_JSON) {
            System.out.println("\n===== RAW LLM JSON RESPONSE =====");
            System.out.println(llmRawResponse);
        }

        String llmAnswer = extractResponseField(llmRawResponse);

        LLMResponseStats responseStats = LLMResponseStats.fromJson(
                llmRawResponse,
                OLLAMA_NUM_CTX,
                RESERVED_OUTPUT_TOKENS
        );

        System.out.println("\n===== LLM RESPONSE =====");
        System.out.println(llmAnswer);

        printLLMResponseStats(responseStats);

        System.out.println("LLM client completed successfully.");
    }

    private static String getEnvOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);

        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }

        return value.trim();
    }

    private static String getRequiredEnv(String name) {
        String value = System.getenv(name);

        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException(
                    "Missing required environment variable: " + name
            );
        }

        return value.trim();
    }

    private static void printCubeSchemaDebugInfo(CubeSchema cubeSchema) {
        System.out.println("===== LLM CLIENT TEST =====");
        System.out.println("Cube Name: " + cubeSchema.getCubeName());
        System.out.println("Cube Datasource: " + cubeSchema.getCubeDataSource());
        System.out.println("Datasource Type: " + cubeSchema.getDataSourceType());
        System.out.println("DBC INI Path: " + cubeSchema.getDbcIniPath());
        System.out.println("Dimensions Count: " + cubeSchema.getDimensions().size());
        System.out.println("Measures Count: " + cubeSchema.getMeasures().size());
        System.out.println("References Count: " + cubeSchema.getReferences().size());

        if (!cubeSchema.getMeasures().isEmpty()) {
            MeasureSchema firstMeasure = cubeSchema.getMeasures().get(0);
            System.out.println("First Measure: " + firstMeasure.getName() + " -> " + firstMeasure.getSource());
        }

        if (!cubeSchema.getDimensions().isEmpty()) {
            DimensionSchema firstDimension = cubeSchema.getDimensions().get(0);
            System.out.println("First Dimension: " + firstDimension.getName());
            System.out.println("First Dimension Datasource: " + firstDimension.getDataSource());
            System.out.println("First Dimension Type: " + firstDimension.getDimensionType());
            System.out.println("First Dimension Levels Count: " + firstDimension.getLevels().size());
            System.out.println("First Dimension Hierarchy: " + firstDimension.getHierarchy());

            if (!firstDimension.getLevels().isEmpty()) {
                LevelSchema firstLevel = firstDimension.getLevels().get(0);
                System.out.println("First Level Name: " + firstLevel.getLevelName());
                System.out.println("First Level ID: " + firstLevel.getId());
                System.out.println("First Level Description: " + firstLevel.getDescription());
                System.out.println("First Level Attributes Count: " + firstLevel.getAttributes().size());

                if (!firstLevel.getAttributes().isEmpty()) {
                    LevelAttributeSchema firstAttribute = firstLevel.getAttributes().get(0);
                    System.out.println("First Attribute Name: " + firstAttribute.getName());
                    System.out.println("First Attribute Type: " + firstAttribute.getType());
                    System.out.println("First Attribute Datasource: " + firstAttribute.getDataSource());
                }
            }
        }

        if (!cubeSchema.getReferences().isEmpty()) {
            CubeReference firstReference = cubeSchema.getReferences().get(0);
            System.out.println("First Reference: "
                    + firstReference.getDimensionName()
                    + " -> "
                    + firstReference.getCubeField());
        }

        System.out.println("LLM client schema loading completed successfully.");
    }

    private static String sendPromptToLLM(String prompt) throws Exception {
        long requestStartNs = System.nanoTime();
        boolean elapsedPrinted = false;

        HttpURLConnection connection = null;

        try {
            URL url = new URL(LLM_ENDPOINT);

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");

            if (API_KEY != null && !API_KEY.trim().isEmpty()) {
                connection.setRequestProperty("X-API-Key", API_KEY);
            }

            /*
             * connectTimeout:
             * How long Java waits to establish the connection.
             *
             * readTimeout = 0:
             * Infinite read timeout from Java's side.
             *
             * Important:
             * This does NOT remove nginx/proxy timeout.
             * If nginx cuts the request, you will still get HTTP 504.
             */
            connection.setConnectTimeout(60000);
            connection.setReadTimeout(0);

            connection.setDoOutput(true);

            String jsonPayload = buildJsonPayload(prompt);

            OutputStream outputStream = connection.getOutputStream();
            byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
            outputStream.write(input, 0, input.length);
            outputStream.flush();
            outputStream.close();

            int responseCode = connection.getResponseCode();

            InputStream responseStream;

            if (responseCode >= 200 && responseCode < 300) {
                responseStream = connection.getInputStream();
            } else {
                responseStream = connection.getErrorStream();

                if (responseStream == null) {
                    long elapsedNs = System.nanoTime() - requestStartNs;
                    printRequestElapsedTime(elapsedNs);
                    elapsedPrinted = true;

                    throw new RuntimeException(
                            "LLM request failed with HTTP status: "
                                    + responseCode
                                    + "\nElapsed time: "
                                    + formatDurationSeconds(elapsedNs)
                                    + " sec"
                    );
                }
            }

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(responseStream, StandardCharsets.UTF_8)
            );

            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line).append("\n");
            }

            reader.close();

            long elapsedNs = System.nanoTime() - requestStartNs;
            printRequestElapsedTime(elapsedNs);
            elapsedPrinted = true;

            if (responseCode < 200 || responseCode >= 300) {
                throw new RuntimeException(
                        "LLM request failed with HTTP status: "
                                + responseCode
                                + "\nElapsed time: "
                                + formatDurationSeconds(elapsedNs)
                                + " sec"
                                + "\nResponse:\n"
                                + response.toString()
                );
            }

            return response.toString();

        } catch (Exception e) {
            if (!elapsedPrinted) {
                long elapsedNs = System.nanoTime() - requestStartNs;

                System.out.println("\n===== REQUEST FAILED =====");
                printRequestElapsedTime(elapsedNs);
            }

            throw e;

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static String buildJsonPayload(String prompt) {
        StringBuilder json = new StringBuilder();

        json.append("{");
        json.append("\"model\":\"").append(escapeJson(LLM_MODEL)).append("\",");
        json.append("\"prompt\":\"").append(escapeJson(prompt)).append("\",");
        json.append("\"stream\":false,");
        json.append("\"keep_alive\":\"10m\",");
        json.append("\"options\":{");
        json.append("\"num_ctx\":").append(OLLAMA_NUM_CTX).append(",");
        json.append("\"num_predict\":").append(NUM_PREDICT).append(",");
        json.append("\"temperature\":0,");
        json.append("\"top_k\":20,");
        json.append("\"top_p\":0.9");
        json.append("}");
        json.append("}");

        return json.toString();
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        StringBuilder escaped = new StringBuilder();

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);

            switch (c) {
                case '"':
                    escaped.append("\\\"");
                    break;
                case '\\':
                    escaped.append("\\\\");
                    break;
                case '\b':
                    escaped.append("\\b");
                    break;
                case '\f':
                    escaped.append("\\f");
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                default:
                    escaped.append(c);
                    break;
            }
        }

        return escaped.toString();
    }

    private static String extractResponseField(String jsonResponse) {
        if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
            return "";
        }

        String key = "\"response\":\"";
        int start = jsonResponse.indexOf(key);

        if (start == -1) {
            return jsonResponse;
        }

        start = start + key.length();

        StringBuilder extracted = new StringBuilder();
        boolean escaping = false;

        for (int i = start; i < jsonResponse.length(); i++) {
            char c = jsonResponse.charAt(i);

            if (escaping) {
                switch (c) {
                    case 'n':
                        extracted.append('\n');
                        break;
                    case 'r':
                        extracted.append('\r');
                        break;
                    case 't':
                        extracted.append('\t');
                        break;
                    case '"':
                        extracted.append('"');
                        break;
                    case '\\':
                        extracted.append('\\');
                        break;
                    case 'b':
                        extracted.append('\b');
                        break;
                    case 'f':
                        extracted.append('\f');
                        break;
                    default:
                        extracted.append(c);
                        break;
                }

                escaping = false;
            } else {
                if (c == '\\') {
                    escaping = true;
                } else if (c == '"') {
                    break;
                } else {
                    extracted.append(c);
                }
            }
        }

        return extracted.toString();
    }

    private static void printPromptSizeStats(PromptSizeStats stats) {
        System.out.println("\n===== PROMPT SIZE STATS BEFORE REQUEST =====");
        System.out.println("Prompt characters: " + stats.getCharacterCount());
        System.out.println("Prompt UTF-8 bytes: " + stats.getUtf8ByteCount());
        System.out.println("Estimated tokens by characters chars/4: " + stats.getEstimatedTokensByCharacters());
        System.out.println("Estimated tokens by UTF-8 bytes bytes/4: " + stats.getEstimatedTokensByBytes());
        System.out.println("Requested Ollama num_ctx: " + OLLAMA_NUM_CTX);
        System.out.println("Reserved output tokens: " + RESERVED_OUTPUT_TOKENS);
        System.out.println("Configured num_predict: " + NUM_PREDICT);
    }

    private static void printLLMResponseStats(LLMResponseStats stats) {
        System.out.println("\n===== OLLAMA RESPONSE STATS =====");

        if (!stats.hasPromptEvalCount()) {
            System.out.println("prompt_eval_count was not found in the response.");
            System.out.println("This usually means the endpoint is not the standard Ollama /api/generate response,");
            System.out.println("or the server did not return the final non-streaming statistics.");
            return;
        }

        System.out.println("Actual prompt tokens prompt_eval_count: " + stats.getPromptEvalCount());
        System.out.println("Actual output tokens eval_count: " + stats.getEvalCount());
        System.out.println("Actual total tokens prompt + output: " + stats.getTotalActualTokens());

        System.out.println("Requested num_ctx: " + stats.getNumCtx());
        System.out.println("Input context usage: " + formatPercent(stats.getPromptContextUsageRatio()));
        System.out.println("Actual total context usage: " + formatPercent(stats.getTotalContextUsageRatio()));

        System.out.println("Remaining tokens after prompt only: " + stats.getRemainingTokensAfterPrompt());
        System.out.println("Remaining tokens after prompt with reserved output buffer: "
                + stats.getRemainingTokensAfterPromptAndReservedOutput());

        if (stats.getPromptContextUsageRatio() >= WARNING_CONTEXT_USAGE_RATIO) {
            System.out.println("WARNING: Prompt uses more than "
                    + formatPercent(WARNING_CONTEXT_USAGE_RATIO)
                    + " of num_ctx. You are close to the practical limit.");
        } else {
            System.out.println("Prompt is not close to the configured num_ctx limit.");
        }

        if (stats.getRemainingTokensAfterPromptAndReservedOutput() < 0) {
            System.out.println("WARNING: Prompt plus reserved output buffer exceeds num_ctx.");
        }

        System.out.println("\n===== TIMING STATS FROM OLLAMA =====");
        printDuration("total_duration", stats.getTotalDurationNs());
        printDuration("load_duration", stats.getLoadDurationNs());
        printDuration("prompt_eval_duration", stats.getPromptEvalDurationNs());
        printDuration("eval_duration", stats.getEvalDurationNs());

        if (stats.getPromptTokensPerSecond() > 0.0) {
            System.out.println("Prompt eval tokens/sec: " + formatDouble(stats.getPromptTokensPerSecond()));
        }

        if (stats.getOutputTokensPerSecond() > 0.0) {
            System.out.println("Output eval tokens/sec: " + formatDouble(stats.getOutputTokensPerSecond()));
        }
    }

    private static void printRequestElapsedTime(long elapsedNs) {
        System.out.println("\n===== HTTP REQUEST TIME =====");
        System.out.println("Elapsed time: " + formatDurationSeconds(elapsedNs) + " sec");
        System.out.println("Elapsed time: " + formatDurationMillis(elapsedNs) + " ms");
    }

    private static void printDuration(String label, long nanos) {
        if (nanos < 0) {
            System.out.println(label + ": not available");
            return;
        }

        double millis = nanos / 1000000.0;
        double seconds = nanos / 1000000000.0;

        System.out.println(label + ": " + nanos + " ns"
                + " | " + formatDouble(millis) + " ms"
                + " | " + formatDouble(seconds) + " sec");
    }

    private static String formatPercent(double value) {
        return formatDouble(value * 100.0) + "%";
    }

    private static String formatDouble(double value) {
        return String.format(java.util.Locale.US, "%.2f", value);
    }

    private static String formatDurationSeconds(long nanos) {
        double seconds = nanos / 1000000000.0;
        return String.format(java.util.Locale.US, "%.2f", seconds);
    }

    private static String formatDurationMillis(long nanos) {
        double millis = nanos / 1000000.0;
        return String.format(java.util.Locale.US, "%.2f", millis);
    }

    private static long extractLongField(String jsonResponse, String fieldName) {
        if (jsonResponse == null || fieldName == null) {
            return -1L;
        }

        String key = "\"" + fieldName + "\":";
        int start = jsonResponse.indexOf(key);

        if (start == -1) {
            return -1L;
        }

        start = start + key.length();

        while (start < jsonResponse.length()
                && Character.isWhitespace(jsonResponse.charAt(start))) {
            start++;
        }

        StringBuilder number = new StringBuilder();

        if (start < jsonResponse.length() && jsonResponse.charAt(start) == '-') {
            number.append('-');
            start++;
        }

        while (start < jsonResponse.length()
                && Character.isDigit(jsonResponse.charAt(start))) {
            number.append(jsonResponse.charAt(start));
            start++;
        }

        if (number.length() == 0 || "-".equals(number.toString())) {
            return -1L;
        }

        try {
            return Long.parseLong(number.toString());
        } catch (NumberFormatException e) {
            return -1L;
        }
    }

    private static class PromptSizeStats {

        private final int characterCount;
        private final int utf8ByteCount;
        private final long estimatedTokensByCharacters;
        private final long estimatedTokensByBytes;

        private PromptSizeStats(
                int characterCount,
                int utf8ByteCount,
                long estimatedTokensByCharacters,
                long estimatedTokensByBytes
        ) {
            this.characterCount = characterCount;
            this.utf8ByteCount = utf8ByteCount;
            this.estimatedTokensByCharacters = estimatedTokensByCharacters;
            this.estimatedTokensByBytes = estimatedTokensByBytes;
        }

        public static PromptSizeStats fromPrompt(String prompt) {
            if (prompt == null) {
                prompt = "";
            }

            int characterCount = prompt.length();
            int utf8ByteCount = prompt.getBytes(StandardCharsets.UTF_8).length;

            long estimatedTokensByCharacters = (long) Math.ceil(characterCount / 4.0);
            long estimatedTokensByBytes = (long) Math.ceil(utf8ByteCount / 4.0);

            return new PromptSizeStats(
                    characterCount,
                    utf8ByteCount,
                    estimatedTokensByCharacters,
                    estimatedTokensByBytes
            );
        }

        public int getCharacterCount() {
            return characterCount;
        }

        public int getUtf8ByteCount() {
            return utf8ByteCount;
        }

        public long getEstimatedTokensByCharacters() {
            return estimatedTokensByCharacters;
        }

        public long getEstimatedTokensByBytes() {
            return estimatedTokensByBytes;
        }
    }

    private static class LLMResponseStats {

        private final long promptEvalCount;
        private final long evalCount;

        private final long totalDurationNs;
        private final long loadDurationNs;
        private final long promptEvalDurationNs;
        private final long evalDurationNs;

        private final int numCtx;
        private final int reservedOutputTokens;

        private LLMResponseStats(
                long promptEvalCount,
                long evalCount,
                long totalDurationNs,
                long loadDurationNs,
                long promptEvalDurationNs,
                long evalDurationNs,
                int numCtx,
                int reservedOutputTokens
        ) {
            this.promptEvalCount = promptEvalCount;
            this.evalCount = evalCount;
            this.totalDurationNs = totalDurationNs;
            this.loadDurationNs = loadDurationNs;
            this.promptEvalDurationNs = promptEvalDurationNs;
            this.evalDurationNs = evalDurationNs;
            this.numCtx = numCtx;
            this.reservedOutputTokens = reservedOutputTokens;
        }

        public static LLMResponseStats fromJson(
                String jsonResponse,
                int numCtx,
                int reservedOutputTokens
        ) {
            return new LLMResponseStats(
                    extractLongField(jsonResponse, "prompt_eval_count"),
                    extractLongField(jsonResponse, "eval_count"),
                    extractLongField(jsonResponse, "total_duration"),
                    extractLongField(jsonResponse, "load_duration"),
                    extractLongField(jsonResponse, "prompt_eval_duration"),
                    extractLongField(jsonResponse, "eval_duration"),
                    numCtx,
                    reservedOutputTokens
            );
        }

        public boolean hasPromptEvalCount() {
            return promptEvalCount >= 0;
        }

        public long getPromptEvalCount() {
            return promptEvalCount;
        }

        public long getEvalCount() {
            return evalCount;
        }

        public long getTotalActualTokens() {
            long safePrompt = Math.max(promptEvalCount, 0);
            long safeEval = Math.max(evalCount, 0);
            return safePrompt + safeEval;
        }

        public int getNumCtx() {
            return numCtx;
        }

        public long getRemainingTokensAfterPrompt() {
            if (promptEvalCount < 0) {
                return -1L;
            }

            return numCtx - promptEvalCount;
        }

        public long getRemainingTokensAfterPromptAndReservedOutput() {
            if (promptEvalCount < 0) {
                return -1L;
            }

            return numCtx - promptEvalCount - reservedOutputTokens;
        }

        public double getPromptContextUsageRatio() {
            if (promptEvalCount < 0 || numCtx <= 0) {
                return 0.0;
            }

            return promptEvalCount / (double) numCtx;
        }

        public double getTotalContextUsageRatio() {
            if (numCtx <= 0) {
                return 0.0;
            }

            return getTotalActualTokens() / (double) numCtx;
        }

        public long getTotalDurationNs() {
            return totalDurationNs;
        }

        public long getLoadDurationNs() {
            return loadDurationNs;
        }

        public long getPromptEvalDurationNs() {
            return promptEvalDurationNs;
        }

        public long getEvalDurationNs() {
            return evalDurationNs;
        }

        public double getPromptTokensPerSecond() {
            if (promptEvalCount <= 0 || promptEvalDurationNs <= 0) {
                return 0.0;
            }

            return promptEvalCount / (promptEvalDurationNs / 1000000000.0);
        }

        public double getOutputTokensPerSecond() {
            if (evalCount <= 0 || evalDurationNs <= 0) {
                return 0.0;
            }

            return evalCount / (evalDurationNs / 1000000000.0);
        }
    }
}