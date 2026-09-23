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
import LLM.prompts.PromptTechniqueCombinationGenerator;

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


    public static void main(
            String[] args
    ) throws Exception {

        /*
         * ======================================================
         * DATASET / CUBE CONFIGURATION
         * ======================================================
         */

        String inputFolder =
                "foodmart_reduced";


        /*
         * INI file:
         *
         * sales.ini
         *
         * Actual cube name extracted from the file:
         *
         * sales_cube
         */

        String cubeFileName =
                "sales";


        Path iniPath =
                Paths.get(
                        "InputFiles",
                        inputFolder,
                        cubeFileName + ".ini"
                );


        if (!Files.exists(
                iniPath
        )) {

            throw new RuntimeException(
                    "INI file not found: "
                            + iniPath.toAbsolutePath()
            );
        }


        System.out.println(
                "===== LLM EXPERIMENT MAIN CLIENT ====="
        );

        System.out.println(
                "LLM endpoint: "
                        + LLM_ENDPOINT
        );

        System.out.println(
                "INI file: "
                        + iniPath.toAbsolutePath()
        );

        System.out.println();


        /*
         * ======================================================
         * 1. LOAD CUBE SCHEMA
         * ======================================================
         */

        CubeIniSchemaExtractor extractor =
                new CubeIniSchemaExtractor();


        CubeSchema cubeSchema =
                extractor.extractFromFile(
                        iniPath.toString()
                );


        printCubeSchemaSummary(
                cubeSchema
        );


        /*
         * ======================================================
         * 2. LOAD MODEL CONFIGURATIONS
         * ======================================================
         */

        List<LLMModelConfig> modelConfigs =
                buildModelConfigs();


        /*
         * ======================================================
         * 3. LOAD PROMPT TECHNIQUES
         * ======================================================
         */

        List<PromptTechnique> promptTechniques =
                PromptTechniqueFactory
                        .getAllPromptTechniques();


        /*
         * ======================================================
         * 4. GENERATE ALL PROMPT COMBINATIONS
         *
         * 3 optional prompt components:
         *
         * 2^3 = 8 combinations
         *
         * including baseline.
         * ======================================================
         */

        List<List<PromptTechnique>> promptCombinations =
                PromptTechniqueCombinationGenerator
                        .generateAllCombinations(
                                promptTechniques
                        );


        /*
         * ======================================================
         * 5. LOAD PAIRED FOODMART WORKLOAD
         *
         * 15 semantic query pairs:
         *
         * 5 simple
         * 5 medium
         * 5 complex
         *
         * Each semantic query has:
         *
         * - 1 structured wording version
         * - 1 natural wording version
         *
         * Therefore:
         *
         * 15 pairs
         * x 2 wording styles
         *
         * = 30 experiment cases
         *
         * The two versions of each pair share exactly the same
         * ExpectedCubeQuery object.
         * ======================================================
         */

        List<LLMExperimentCase> experimentCases =
                LLMBasicExperiments
                        .getBasicFoodmartExperiments();


        /*
         * ======================================================
         * 6. CREATE OUTPUT FILES
         * ======================================================
         */

        Path outputDirectory =
                Paths.get(
                        "experiment_outputs"
                );


        Files.createDirectories(
                outputDirectory
        );


        String timestampForFile =
                createTimestampForFileName();


        Path resultsPath =
                outputDirectory.resolve(
                        "experiment_results_"
                                + timestampForFile
                                + ".tsv"
                );


        Path validationLogPath =
                outputDirectory.resolve(
                        "validation_log_"
                                + timestampForFile
                                + ".txt"
                );


        System.out.println(
                "Output TSV:"
        );

        System.out.println(
                resultsPath.toAbsolutePath()
        );

        System.out.println();


        System.out.println(
                "Validation log:"
        );

        System.out.println(
                validationLogPath.toAbsolutePath()
        );

        System.out.println();


        /*
         * ======================================================
         * 7. PRINT EXPERIMENT PLAN
         * ======================================================
         */

        printExperimentPlan(
                modelConfigs,
                promptTechniques,
                promptCombinations,
                experimentCases
        );


        /*
         * ======================================================
         * 8. CREATE LLM CLIENT
         * ======================================================
         */

        OllamaLLMClient llmClient =
                new OllamaLLMClient(
                        LLM_ENDPOINT,
                        API_KEY
                );


        /*
         * ======================================================
         * 9. CREATE EXPERIMENT RUNNER
         *
         * The runner handles:
         *
         * - paired deterministic random ordering
         * - seed = 42
         * - warm-up per Model x Prompt Combination
         * - strict validation
         * - structural validation
         * - OLAP query similarity
         *
         * Warm-ups are not written to the experiment results.
         * ======================================================
         */

        LLMExperimentRunner runner =
                new LLMExperimentRunner(
                        llmClient
                );


        /*
         * ======================================================
         * 10. RUN EXPERIMENTS
         * ======================================================
         */

        try (
                ExperimentResultsWriter resultsWriter =
                        new ExperimentResultsWriter(
                                resultsPath
                        );

                ValidationLogWriter validationLogWriter =
                        new ValidationLogWriter(
                                validationLogPath
                        )
        ) {

            runner.runExperiments(
                    cubeSchema,
                    modelConfigs,
                    promptCombinations,
                    experimentCases,
                    resultsWriter,
                    validationLogWriter
            );
        }


        /*
         * ======================================================
         * COMPLETION
         * ======================================================
         */

        System.out.println();

        System.out.println(
                "Experiment run completed."
        );

        System.out.println(
                "Results file: "
                        + resultsPath.toAbsolutePath()
        );

        System.out.println(
                "Validation log: "
                        + validationLogPath.toAbsolutePath()
        );
    }


    /*
     * ==========================================================
     * MODEL CONFIGURATIONS
     * ==========================================================
     */

    private static List<LLMModelConfig> buildModelConfigs() {

        List<LLMModelConfig> modelConfigs =
                new ArrayList<LLMModelConfig>();


        modelConfigs.add(
                LLMModelConfig.createExperimentConfig(
                        "gemma4:26b",
                        4096,
                        256,
                        true
                )
        );


        modelConfigs.add(
                LLMModelConfig.createExperimentConfig(
                        "qwen3.5:35b",
                        4096,
                        256,
                        true
                )
        );


        modelConfigs.add(
                LLMModelConfig.createExperimentConfig(
                        "gpt-oss:20b",
                        4096,
                        256,
                        true
                )
        );


        modelConfigs.add(
                LLMModelConfig.createExperimentConfig(
                        "devstral-small-2:24b",
                        4096,
                        256,
                        true
                )
        );


        modelConfigs.add(
                LLMModelConfig.createExperimentConfig(
                        "qwen3-coder:30b",
                        4096,
                        256,
                        true
                )
        );


        return modelConfigs;
    }


    /*
     * ==========================================================
     * CUBE SCHEMA SUMMARY
     * ==========================================================
     */

    private static void printCubeSchemaSummary(
            CubeSchema cubeSchema
    ) {

        System.out.println(
                "===== CUBE SCHEMA SUMMARY ====="
        );


        System.out.println(
                "Cube Name: "
                        + cubeSchema.getCubeName()
        );


        System.out.println(
                "Cube Datasource: "
                        + cubeSchema.getCubeDataSource()
        );


        System.out.println(
                "Datasource Type: "
                        + cubeSchema.getDataSourceType()
        );


        System.out.println(
                "DBC INI Path: "
                        + cubeSchema.getDbcIniPath()
        );


        System.out.println(
                "Dimensions Count: "
                        + cubeSchema
                        .getDimensions()
                        .size()
        );


        System.out.println(
                "Measures Count: "
                        + cubeSchema
                        .getMeasures()
                        .size()
        );


        System.out.println(
                "References Count: "
                        + cubeSchema
                        .getReferences()
                        .size()
        );


        System.out.println();
    }


    /*
     * ==========================================================
     * EXPERIMENT PLAN
     * ==========================================================
     */

    private static void printExperimentPlan(
            List<LLMModelConfig> modelConfigs,
            List<PromptTechnique> promptTechniques,
            List<List<PromptTechnique>> promptCombinations,
            List<LLMExperimentCase> experimentCases
    ) {

        int totalMeasuredRuns =
                modelConfigs.size()
                        * promptCombinations.size()
                        * experimentCases.size();


        /*
         * One warm-up before every:
         *
         * Model x Prompt Combination
         */

        int totalWarmUpCalls =
                modelConfigs.size()
                        * promptCombinations.size();


        /*
         * Every semantic pair contains exactly two cases:
         *
         * structured
         * natural
         */

        int semanticPairCount =
                experimentCases.size()
                        / 2;


        int structuredCount =
                0;

        int naturalCount =
                0;


        for (LLMExperimentCase experimentCase
                : experimentCases) {

            if (LLMExperimentCase
                    .WORDING_TYPE_STRUCTURED
                    .equals(
                            experimentCase.getWordingType()
                    )) {

                structuredCount++;
            }


            if (LLMExperimentCase
                    .WORDING_TYPE_NATURAL
                    .equals(
                            experimentCase.getWordingType()
                    )) {

                naturalCount++;
            }
        }


        System.out.println(
                "===== EXPERIMENT PLAN ====="
        );


        /*
         * ======================================================
         * MODELS
         * ======================================================
         */

        System.out.println(
                "Models: "
                        + modelConfigs.size()
        );


        for (LLMModelConfig modelConfig
                : modelConfigs) {

            System.out.println(
                    "- "
                            + modelConfig.getModelName()

                            + " | numCtx="
                            + modelConfig.getNumCtx()

                            + " | numPredict="
                            + modelConfig.getNumPredict()

                            + " | stream="
                            + modelConfig.isStreamEnabled()
            );
        }


        System.out.println();


        /*
         * ======================================================
         * PROMPT TECHNIQUES
         * ======================================================
         */

        System.out.println(
                "Available prompt techniques: "
                        + promptTechniques.size()
        );


        for (PromptTechnique promptTechnique
                : promptTechniques) {

            System.out.println(
                    "- "
                            + promptTechnique.getName()
            );
        }


        System.out.println();


        /*
         * ======================================================
         * PROMPT COMBINATIONS
         * ======================================================
         */

        System.out.println(
                "Prompt combinations: "
                        + promptCombinations.size()
        );


        for (List<PromptTechnique> combination
                : promptCombinations) {

            String combinationName =
                    PromptTechniqueCombinationGenerator
                            .getCombinationName(
                                    combination
                            );


            System.out.println(
                    "- "
                            + combinationName
            );
        }


        System.out.println();


        /*
         * ======================================================
         * PAIRED WORKLOAD
         * ======================================================
         */

        System.out.println(
                "Semantic pairs: "
                        + semanticPairCount
        );


        System.out.println(
                "Total test cases: "
                        + experimentCases.size()
        );


        System.out.println(
                "Structured cases: "
                        + structuredCount
        );


        System.out.println(
                "Natural cases: "
                        + naturalCount
        );


        System.out.println();


        /*
         * Print cases in their original workload definition order.
         *
         * NOTE:
         *
         * This is NOT necessarily the execution order.
         *
         * The runner creates the final deterministic randomized
         * execution order using seed 42.
         */

        System.out.println(
                "Defined test cases:"
        );


        for (LLMExperimentCase experimentCase
                : experimentCases) {

            System.out.println(
                    "- "
                            + experimentCase.getTestId()

                            + " | pair="
                            + experimentCase.getPairId()

                            + " | wording="
                            + experimentCase.getWordingType()

                            + " | difficulty="
                            + experimentCase.getDifficulty()

                            + " | category="
                            + experimentCase.getCategory()
            );
        }


        System.out.println();


        /*
         * ======================================================
         * RUN COUNTS
         * ======================================================
         */

        System.out.println(
                "Measured experiment runs: "
                        + totalMeasuredRuns
        );


        System.out.println(
                "Warm-up calls: "
                        + totalWarmUpCalls
        );


        System.out.println(
                "Total LLM requests including warm-up: "
                        + (
                        totalMeasuredRuns
                                + totalWarmUpCalls
                )
        );


        System.out.println();


        /*
         * ======================================================
         * EXECUTION ORDER
         * ======================================================
         */

        System.out.println(
                "Execution structure:"
        );


        System.out.println(
                "LLM"
                        + " -> prompt combination"
                        + " -> warm-up"
                        + " -> 30 paired test cases"
        );


        System.out.println(
                "Measured case order is randomized once "
                        + "with seed 42 and reused for all blocks."
        );


        System.out.println();
    }


    /*
     * ==========================================================
     * FILE TIMESTAMP
     * ==========================================================
     */

    private static String createTimestampForFileName() {

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern(
                        "yyyy-MM-dd_HH-mm-ss"
                );


        return LocalDateTime
                .now()
                .format(
                        formatter
                );
    }


    /*
     * ==========================================================
     * ENVIRONMENT VARIABLES
     * ==========================================================
     */

    private static String getRequiredEnv(
            String name
    ) {

        String value =
                System.getenv(
                        name
                );


        if (value == null
                || value.trim().isEmpty()) {

            throw new IllegalStateException(
                    "Missing required environment variable: "
                            + name
            );
        }


        return value.trim();
    }
}