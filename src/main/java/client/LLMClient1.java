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
 * 5) sending the prompt to an LLM endpoint
 * 6) printing only the LLM response field
 */
public class LLMClient1 {

    private static final String HOST = "localhost";
    private static final int PORT = 2020;

    private static final String LLM_ENDPOINT = getRequiredEnv("HTTP_GATE");
    private static final String API_KEY = getRequiredEnv("API_KEY");
    private static final String LLM_MODEL = "llama3.1:8b";

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
        String naturalLanguageQuestion = "What were the store sales each month and region in California during Q3 of 1997 for Daily Paper media?";

        PromptBuilder promptBuilder = new PromptBuilder();
        String prompt = promptBuilder.buildPrompt(cubeSchema, naturalLanguageQuestion);

        String llmRawResponse = sendPromptToLLM(prompt);
        String llmAnswer = extractResponseField(llmRawResponse);

        System.out.println("\n===== LLM RESPONSE =====");
        System.out.println(llmAnswer);

        System.out.println("LLM client completed successfully.");


//        CubeSchemaPromptFormatter formatter = new CubeSchemaPromptFormatter();
//        String formattedCubeSchema = formatter.format(cubeSchema);
//
//        System.out.println("===== CUBE SCHEMA PROMPT FORMATTER OUTPUT =====");
//        System.out.println(formattedCubeSchema);
//        System.out.println("===== END =====");
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
        URL url = new URL(LLM_ENDPOINT);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");

        if (API_KEY != null && !API_KEY.trim().isEmpty()) {
            connection.setRequestProperty("X-API-Key", API_KEY);
        }

        connection.setDoOutput(true);

        String jsonPayload = buildJsonPayload(prompt);

        OutputStream outputStream = connection.getOutputStream();
        byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
        outputStream.write(input, 0, input.length);
        outputStream.flush();
        outputStream.close();

        int responseCode = connection.getResponseCode();

        BufferedReader reader;

        if (responseCode >= 200 && responseCode < 300) {
            reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)
            );
        } else {
            reader = new BufferedReader(
                    new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8)
            );
        }

        StringBuilder response = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line).append("\n");
        }

        reader.close();
        connection.disconnect();

        if (responseCode < 200 || responseCode >= 300) {
            throw new RuntimeException(
                    "LLM request failed with HTTP status: "
                            + responseCode
                            + "\nResponse:\n"
                            + response.toString()
            );
        }

        return response.toString();
    }

    private static String buildJsonPayload(String prompt) {
        StringBuilder json = new StringBuilder();

        json.append("{");
        json.append("\"model\":\"").append(escapeJson(LLM_MODEL)).append("\",");
        json.append("\"prompt\":\"").append(escapeJson(prompt)).append("\",");
        json.append("\"stream\":false");
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

    private static String getRequiredEnv(String name) {
        String value = System.getenv(name);

        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException(
                    "Missing required environment variable: " + name
            );
        }

        return value.trim();
    }
}