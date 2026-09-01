package client;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

import LLM.extractor.CubeIniSchemaExtractor;
import LLM.schema.CubeSchema;

import LLM.experiments.LLMBasicExperiments;
import LLM.experiments.LLMExperimentCase;

import LLM.prompts.PromptTechnique;
import LLM.prompts.PromptTechniqueFactory;

import LLM.experiments.runner.LLMExperimentRunner;
import LLM.experiments.runner.LLMModelConfig;
import LLM.experiments.runner.OllamaLLMClient;

import LLM.experiments.results.ExperimentResultsWriter;
import LLM.experiments.results.ValidationLogWriter;

public class LLMExperimentMainClient {

    private static final String LLM_ENDPOINT =
            getRequiredEnv("HTTP_GATE");

    private static final String API_KEY =
            getRequiredEnv("API_KEY");

    public static void main(String[] args) throws Exception {

        /*
         * Dataset / cube configuration.
         *
         * Για foodmart_reduced:
         * InputFiles/foodmart_reduced/sales.ini
         */
        String inputFolder = "foodmart_reduced";
        String cubeName = "sales";

        Path iniPath = Paths.get("InputFiles", inputFolder, cubeName + ".ini");

        if (!Files.exists(iniPath)) {
            throw new RuntimeException("INI file not found: " + iniPath.toAbsolutePath());
        }

        System.out.println("===== LLM EXPERIMENT MAIN CLIENT =====");
        System.out.println("LLM endpoint: " + LLM_ENDPOINT);
        System.out.println("INI file: " + iniPath.toAbsolutePath());
        System.out.println();

        /*
         * 1. Load cube schema from .ini.
         */
        CubeIniSchemaExtractor extractor = new CubeIniSchemaExtractor();
        CubeSchema cubeSchema = extractor.extractFromFile(iniPath.toString());

        printCubeSchemaSummary(cubeSchema);

        /*
         * 2. Load model configs.
         *
         * Η σειρά εδώ είναι η σειρά εκτέλεσης των LLMs.
         */
        List<LLMModelConfig> modelConfigs = buildModelConfigs();

        /*
         * 3. Load prompt techniques.
         *
         * Η σειρά έρχεται από το PromptTechniqueFactory:
         * 1. rules_template
         * 2. rules_generic_examples
         * 3. step_by_step_methodology
         * 4. hybrid_self_check
         */
        List<PromptTechnique> promptTechniques =
                PromptTechniqueFactory.getAllPromptTechniques();

        /*
         * 4. Load experiment cases.
         */
        List<LLMExperimentCase> experimentCases =
                LLMBasicExperiments.getBasicFoodmartExperiments();

        /*
         * 5. Create output files.
         */
        Path outputDirectory = Paths.get("experiment_outputs");
        Files.createDirectories(outputDirectory);

        String timestampForFile = createTimestampForFileName();

        Path resultsPath = outputDirectory.resolve(
                "experiment_results_" + timestampForFile + ".tsv"
        );

        Path validationLogPath = outputDirectory.resolve(
                "validation_log_" + timestampForFile + ".txt"
        );

        System.out.println("Output TSV:");
        System.out.println(resultsPath.toAbsolutePath());
        System.out.println();

        System.out.println("Validation log:");
        System.out.println(validationLogPath.toAbsolutePath());
        System.out.println();

        /*
         * 6. Print experiment plan.
         */
        printExperimentPlan(modelConfigs, promptTechniques, experimentCases);

        /*
         * 7. Run experiments.
         */
        OllamaLLMClient llmClient = new OllamaLLMClient(
                LLM_ENDPOINT,
                API_KEY
        );

        LLMExperimentRunner runner = new LLMExperimentRunner(llmClient);

        ExperimentResultsWriter resultsWriter = null;
        ValidationLogWriter validationLogWriter = null;

        try {
            resultsWriter = new ExperimentResultsWriter(resultsPath);
            validationLogWriter = new ValidationLogWriter(validationLogPath);

            runner.runExperiments(
                    cubeSchema,
                    modelConfigs,
                    promptTechniques,
                    experimentCases,
                    resultsWriter,
                    validationLogWriter
            );

        } finally {
            if (resultsWriter != null) {
                resultsWriter.close();
            }

            if (validationLogWriter != null) {
                validationLogWriter.close();
            }
        }

        System.out.println();
        System.out.println("Experiment run completed.");
        System.out.println("Results file: " + resultsPath.toAbsolutePath());
        System.out.println("Validation log: " + validationLogPath.toAbsolutePath());
    }

    private static List<LLMModelConfig> buildModelConfigs() {
        List<LLMModelConfig> modelConfigs = new ArrayList<LLMModelConfig>();

        modelConfigs.add(
                LLMModelConfig.createCustom(
                        "qwen3-coder:30b",
                        4096,
                        128,
                        true
                )
        );

        modelConfigs.add(
                LLMModelConfig.createCustom(
                        "llama3.3:70b",
                        4096,
                        96,
                        true
                )
        );

        modelConfigs.add(
                LLMModelConfig.createCustom(
                        "deepseek-coder-v2:latest",
                        4096,
                        128,
                        true
                )
        );

        return modelConfigs;
    }

    private static void printCubeSchemaSummary(CubeSchema cubeSchema) {
        System.out.println("===== CUBE SCHEMA SUMMARY =====");
        System.out.println("Cube Name: " + cubeSchema.getCubeName());
        System.out.println("Cube Datasource: " + cubeSchema.getCubeDataSource());
        System.out.println("Datasource Type: " + cubeSchema.getDataSourceType());
        System.out.println("DBC INI Path: " + cubeSchema.getDbcIniPath());
        System.out.println("Dimensions Count: " + cubeSchema.getDimensions().size());
        System.out.println("Measures Count: " + cubeSchema.getMeasures().size());
        System.out.println("References Count: " + cubeSchema.getReferences().size());
        System.out.println();
    }

    private static void printExperimentPlan(
            List<LLMModelConfig> modelConfigs,
            List<PromptTechnique> promptTechniques,
            List<LLMExperimentCase> experimentCases
    ) {
        int totalRuns =
                modelConfigs.size()
                        * promptTechniques.size()
                        * experimentCases.size();

        System.out.println("===== EXPERIMENT PLAN =====");
        System.out.println("Models: " + modelConfigs.size());
        for (LLMModelConfig modelConfig : modelConfigs) {
            System.out.println("- " + modelConfig.getModelName()
                    + " | numCtx=" + modelConfig.getNumCtx()
                    + " | numPredict=" + modelConfig.getNumPredict()
                    + " | stream=" + modelConfig.isStreamEnabled());
        }

        System.out.println();

        System.out.println("Prompt techniques: " + promptTechniques.size());
        for (PromptTechnique promptTechnique : promptTechniques) {
            System.out.println("- " + promptTechnique.getName());
        }

        System.out.println();

        System.out.println("Test cases: " + experimentCases.size());
        for (LLMExperimentCase experimentCase : experimentCases) {
            System.out.println("- " + experimentCase.getTestId()
                    + " | " + experimentCase.getDifficulty());
        }

        System.out.println();

        System.out.println("Total runs: " + totalRuns);
        System.out.println("Execution order:");
        System.out.println("for each LLM -> for each prompt technique -> for each test case");
        System.out.println();
    }

    private static String createTimestampForFileName() {
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

        return LocalDateTime.now().format(formatter);
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
}