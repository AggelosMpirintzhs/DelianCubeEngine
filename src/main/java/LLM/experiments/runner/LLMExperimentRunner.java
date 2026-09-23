package LLM.experiments.runner;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.List;

import LLM.experiments.CubeQueryValidator;
import LLM.experiments.ExpectedCubeQuery;
import LLM.experiments.LLMExperimentCase;
import LLM.experiments.ValidationResult;

import LLM.experiments.parsing.CubeQueryJsonParser;
import LLM.experiments.parsing.CubeQueryParseResult;

import LLM.experiments.similarity.HierarchyMetadataIndex;
import LLM.experiments.similarity.QuerySimilarityEvaluator;
import LLM.experiments.similarity.QuerySimilarityResult;

import LLM.prompts.PromptBuilder;
import LLM.prompts.PromptTechnique;
import LLM.prompts.PromptTechniqueCombinationGenerator;

import LLM.experiments.results.ExperimentResult;
import LLM.experiments.results.ExperimentResultsWriter;
import LLM.experiments.results.ValidationLogWriter;

import LLM.schema.CubeSchema;

public class LLMExperimentRunner {

    /*
     * ==========================================================
     * EXPERIMENT ORDER SEED
     * ==========================================================
     *
     * The paired structured/natural workload is randomized
     * exactly once using this fixed seed.
     *
     * The SAME resulting order is then reused for every:
     *
     * - LLM
     * - prompt combination
     *
     * This makes the experiment reproducible and prevents
     * different models from receiving different execution orders.
     * ==========================================================
     */

    private static final long EXPERIMENT_ORDER_SEED =
            42L;


    /*
     * ==========================================================
     * WARM-UP QUESTION
     * ==========================================================
     *
     * This question is NOT one of the measured experiment cases.
     *
     * It is used only to warm up each:
     *
     * Model x Prompt Combination
     *
     * before the measured runs begin.
     *
     * The normal PromptBuilder is used so that the warm-up
     * passes through the same prompt construction path as the
     * real experiments.
     * ==========================================================
     */

    private static final String WARM_UP_QUESTION =
            "What are the total store sales?";


    private final OllamaLLMClient llmClient;
    private final PromptBuilder promptBuilder;

    private int runCounter;


    /*
     * ==========================================================
     * CONSTRUCTORS
     * ==========================================================
     */

    public LLMExperimentRunner(
            OllamaLLMClient llmClient
    ) {

        this(
                llmClient,
                new PromptBuilder()
        );
    }


    public LLMExperimentRunner(
            OllamaLLMClient llmClient,
            PromptBuilder promptBuilder
    ) {

        if (llmClient == null) {

            throw new IllegalArgumentException(
                    "llmClient cannot be null."
            );
        }

        if (promptBuilder == null) {

            throw new IllegalArgumentException(
                    "promptBuilder cannot be null."
            );
        }

        this.llmClient =
                llmClient;

        this.promptBuilder =
                promptBuilder;

        this.runCounter =
                0;
    }


    /*
     * ==========================================================
     * RUN COMPLETE EXPERIMENT BATCH
     * ==========================================================
     */

    public void runExperiments(
            CubeSchema cubeSchema,
            List<LLMModelConfig> modelConfigs,
            List<List<PromptTechnique>> promptCombinations,
            List<LLMExperimentCase> experimentCases,
            ExperimentResultsWriter resultsWriter,
            ValidationLogWriter validationLogWriter
    ) throws Exception {

        validateInputs(
                cubeSchema,
                modelConfigs,
                promptCombinations,
                experimentCases,
                resultsWriter,
                validationLogWriter
        );


        /*
         * ======================================================
         * BUILD DETERMINISTIC PAIRED ORDER ONCE
         * ======================================================
         *
         * IMPORTANT:
         *
         * ExperimentCaseOrderer:
         *
         * - groups cases by pairId
         * - verifies one structured + one natural case
         * - verifies that both use the same ExpectedCubeQuery
         * - shuffles the 15 semantic pairs
         * - balances structured-first / natural-first
         *
         * This method is called ONCE.
         *
         * Therefore every model and every prompt combination
         * receives the exact same sequence of 30 cases.
         * ======================================================
         */

        List<LLMExperimentCase> orderedExperimentCases =
                ExperimentCaseOrderer.createPairedOrder(
                        experimentCases,
                        EXPERIMENT_ORDER_SEED
                );


        /*
         * ======================================================
         * BUILD SIMILARITY METADATA ONCE
         * ======================================================
         *
         * The hierarchy metadata depends only on the cube schema.
         *
         * It is therefore created once for the complete
         * experiment batch and reused for all models, prompt
         * combinations and test cases.
         * ======================================================
         */

        HierarchyMetadataIndex hierarchyMetadataIndex =
                new HierarchyMetadataIndex(
                        cubeSchema
                );

        QuerySimilarityEvaluator querySimilarityEvaluator =
                new QuerySimilarityEvaluator(
                        hierarchyMetadataIndex
                );


        /*
         * ======================================================
         * TOTAL MEASURED RUNS
         * ======================================================
         *
         * Warm-up calls are NOT included.
         * ======================================================
         */

        int totalRuns =
                modelConfigs.size()
                        * promptCombinations.size()
                        * orderedExperimentCases.size();


        printExperimentOverview(
                modelConfigs,
                promptCombinations,
                orderedExperimentCases,
                totalRuns
        );


        printExperimentOrder(
                orderedExperimentCases
        );


        /*
         * ======================================================
         * EXPERIMENT LOOP
         *
         * Model
         *   -> Prompt Combination
         *       -> Warm-up
         *       -> Ordered Test Cases
         *
         * Warm-up calls:
         *
         * - do NOT receive a run ID
         * - do NOT increase runCounter
         * - do NOT enter the TSV
         * - do NOT enter the validation log
         *
         * ======================================================
         */

        for (LLMModelConfig modelConfig
                : modelConfigs) {

            System.out.println();
            System.out.println(
                    "========================================"
            );

            System.out.println(
                    "MODEL: "
                            + modelConfig.getModelName()
            );

            System.out.println(
                    "========================================"
            );

            System.out.println();


            for (List<PromptTechnique> promptCombination
                    : promptCombinations) {

                String combinationName =
                        PromptTechniqueCombinationGenerator
                                .getCombinationName(
                                        promptCombination
                                );


                System.out.println(
                        "Running combination: "
                                + combinationName
                );


                /*
                 * ==================================================
                 * WARM-UP THIS MODEL x PROMPT COMBINATION
                 * ==================================================
                 */

                warmUpCombination(
                        cubeSchema,
                        modelConfig,
                        promptCombination,
                        combinationName
                );


                /*
                 * ==================================================
                 * MEASURED RUNS
                 * ==================================================
                 *
                 * Always use the same precomputed order.
                 * ==================================================
                 */

                for (LLMExperimentCase experimentCase
                        : orderedExperimentCases) {

                    ExperimentResult result =
                            runSingleExperiment(
                                    cubeSchema,
                                    modelConfig,
                                    promptCombination,
                                    experimentCase,
                                    querySimilarityEvaluator
                            );


                    /*
                     * Detailed experiment output.
                     */
                    resultsWriter.writeResult(
                            result
                    );


                    /*
                     * Detailed validation log.
                     */
                    validationLogWriter.writeResult(
                            result
                    );


                    printRunSummary(
                            result,
                            totalRuns
                    );
                }


                System.out.println();
            }
        }


        System.out.println();

        System.out.println(
                "LLM experiments completed."
        );

        System.out.println(
                "Completed measured runs: "
                        + runCounter
                        + "/"
                        + totalRuns
        );
    }


    /*
     * ==========================================================
     * SINGLE EXPERIMENT
     * ==========================================================
     */

    private ExperimentResult runSingleExperiment(
            CubeSchema cubeSchema,
            LLMModelConfig modelConfig,
            List<PromptTechnique> promptCombination,
            LLMExperimentCase experimentCase,
            QuerySimilarityEvaluator querySimilarityEvaluator
    ) {

        /*
         * Only measured runs call nextRunId().
         *
         * Warm-up calls therefore never affect the run numbering.
         */

        String runId =
                nextRunId();

        String timestamp =
                currentTimestamp();


        String combinationName =
                PromptTechniqueCombinationGenerator
                        .getCombinationName(
                                promptCombination
                        );


        String prompt =
                "";


        PromptSizeStats promptSizeStats =
                PromptSizeStats.fromPrompt(
                        ""
                );


        try {

            /*
             * ==================================================
             * 1. BUILD FINAL PROMPT
             *
             * Mandatory:
             *
             * - Cube schema
             * - User question
             * - JSON output contract
             *
             * Optional:
             *
             * - selected prompt techniques
             * ==================================================
             */

            prompt =
                    promptBuilder.buildPrompt(
                            cubeSchema,
                            experimentCase.getQuestion(),
                            promptCombination
                    );


            /*
             * ==================================================
             * 2. PROMPT SIZE METRICS
             * ==================================================
             */

            promptSizeStats =
                    PromptSizeStats.fromPrompt(
                            prompt
                    );


            /*
             * ==================================================
             * 3. CALL LLM
             *
             * Only ONE LLM request is performed for each
             * measured experiment.
             * ==================================================
             */

            LLMCallResult llmCallResult =
                    llmClient.generate(
                            modelConfig,
                            prompt
                    );


            String actualAnswer =
                    llmCallResult.getAnswer();


            /*
             * ==================================================
             * 4. PARSE LLM RESPONSE
             *
             * IMPORTANT:
             *
             * The response is parsed exactly ONCE.
             *
             * The same parsed result is reused by:
             *
             * - strict validation
             * - structural validation
             * - similarity evaluation
             * ==================================================
             */

            CubeQueryParseResult parseResult =
                    CubeQueryJsonParser.parse(
                            actualAnswer
                    );


            /*
             * ==================================================
             * 5. STRICT VALIDATION
             *
             * Checks:
             *
             * - JSON / output format
             * - cube
             * - aggregation
             * - measure
             * - gamma
             * - sigma fields
             * - sigma values
             * ==================================================
             */

            ValidationResult strictValidationResult =
                    CubeQueryValidator.validate(
                            parseResult,
                            experimentCase.getExpectedQuery(),
                            cubeSchema
                    );


            /*
             * ==================================================
             * 6. STRUCTURAL VALIDATION
             *
             * Same validation, but differences in sigma values
             * are ignored.
             *
             * Example:
             *
             * Expected:
             *
             * marital_status='M'
             *
             * Actual:
             *
             * marital_status='Married'
             *
             * Structural validation can still consider the
             * selected sigma field correct.
             * ==================================================
             */

            ValidationResult structuralValidationResult =
                    CubeQueryValidator
                            .validateIgnoringSigmaValues(
                                    parseResult,
                                    experimentCase.getExpectedQuery(),
                                    cubeSchema
                            );


            /*
             * ==================================================
             * 7. QUERY SIMILARITY
             *
             * Similarity is complementary to exact validation.
             *
             * It is calculated only when:
             *
             * - the LLM request completed successfully
             * - the response is syntactically valid JSON
             *
             * The JSON does NOT need to satisfy the complete
             * output contract.
             *
             * Example:
             *
             * Missing queryName:
             *
             * formatValid = false
             *
             * but semantic similarity can still be calculated.
             * ==================================================
             */

            QuerySimilarityResult querySimilarityResult;


            if (llmCallResult.isSuccess()
                    && parseResult.isValidJson()) {

                querySimilarityResult =
                        querySimilarityEvaluator.evaluate(
                                experimentCase
                                        .getExpectedQuery(),

                                parseResult
                                        .getParsedQuery()
                        );

            } else {

                querySimilarityResult =
                        QuerySimilarityResult
                                .unavailable();
            }


            /*
             * ==================================================
             * 8. CREATE FINAL EXPERIMENT RESULT
             * ==================================================
             *
             * ExperimentResult.from(...) also copies:
             *
             * - testId
             * - pairId
             * - wordingType
             * - difficulty
             * - category
             *
             * from LLMExperimentCase.
             * ==================================================
             */

            return ExperimentResult.from(
                    runId,
                    timestamp,
                    modelConfig,
                    combinationName,
                    experimentCase,
                    actualAnswer,
                    strictValidationResult,
                    structuralValidationResult,
                    querySimilarityResult,
                    promptSizeStats,
                    llmCallResult
            );


        } catch (Exception e) {

            /*
             * Any unexpected runner-side failure:
             *
             * - Prompt building
             * - Parsing
             * - Validation
             * - Similarity evaluation
             * - unexpected runtime exception
             *
             * The experiment is still recorded instead of
             * terminating the complete experiment batch.
             */

            return createFailedExperimentResult(
                    runId,
                    timestamp,
                    cubeSchema,
                    modelConfig,
                    combinationName,
                    experimentCase,
                    promptSizeStats,
                    e
            );
        }
    }


    /*
     * ==========================================================
     * FAILED EXPERIMENT
     * ==========================================================
     */

    private ExperimentResult createFailedExperimentResult(
            String runId,
            String timestamp,
            CubeSchema cubeSchema,
            LLMModelConfig modelConfig,
            String combinationName,
            LLMExperimentCase experimentCase,
            PromptSizeStats promptSizeStats,
            Exception exception
    ) {

        LLMCallResult failedCallResult =
                LLMCallResult.failure(
                        0L,
                        "RUNNER_EXCEPTION",
                        exception
                                .getClass()
                                .getSimpleName()
                                + ": "
                                + safe(
                                exception.getMessage()
                        ),
                        ""
                );


        ExpectedCubeQuery expectedQuery =
                experimentCase == null
                        ? null
                        : experimentCase
                        .getExpectedQuery();


        ValidationResult strictValidationResult;

        ValidationResult structuralValidationResult;


        if (expectedQuery == null) {

            strictValidationResult =
                    new ValidationResult(
                            false,
                            null
                    );

            structuralValidationResult =
                    new ValidationResult(
                            false,
                            null
                    );

        } else {

            /*
             * Parse the failed/empty answer once.
             *
             * The resulting validation metrics correctly
             * represent a failed experiment.
             */

            CubeQueryParseResult failedParseResult =
                    CubeQueryJsonParser.parse(
                            ""
                    );


            strictValidationResult =
                    CubeQueryValidator.validate(
                            failedParseResult,
                            expectedQuery,
                            cubeSchema
                    );


            structuralValidationResult =
                    CubeQueryValidator
                            .validateIgnoringSigmaValues(
                                    failedParseResult,
                                    expectedQuery,
                                    cubeSchema
                            );
        }


        /*
         * Similarity is deliberately unavailable for a
         * runner-side failure.
         *
         * We must not confuse:
         *
         * similarity = 0
         *
         * with:
         *
         * similarity was not calculated.
         */

        QuerySimilarityResult querySimilarityResult =
                QuerySimilarityResult
                        .unavailable();


        return ExperimentResult.from(
                runId,
                timestamp,
                modelConfig,
                combinationName,
                experimentCase,
                "",
                strictValidationResult,
                structuralValidationResult,
                querySimilarityResult,
                promptSizeStats,
                failedCallResult
        );
    }


    /*
     * ==========================================================
     * MODEL x PROMPT COMBINATION WARM-UP
     * ==========================================================
     *
     * A warm-up is executed before EVERY prompt-combination
     * block for every model.
     *
     * Example:
     *
     * Gemma + baseline
     *     -> warm-up
     *     -> 30 measured runs
     *
     * Gemma + examples
     *     -> warm-up
     *     -> 30 measured runs
     *
     * Qwen + baseline
     *     -> warm-up
     *     -> 30 measured runs
     *
     * ...
     *
     * Warm-up calls are intentionally excluded from all
     * experimental outputs.
     * ==========================================================
     */

    private void warmUpCombination(
            CubeSchema cubeSchema,
            LLMModelConfig modelConfig,
            List<PromptTechnique> promptCombination,
            String combinationName
    ) {

        if (cubeSchema == null
                || modelConfig == null
                || promptCombination == null) {

            return;
        }


        System.out.println(
                "Warm-up: "
                        + modelConfig.getModelName()
                        + " | "
                        + combinationName
        );


        try {

            /*
             * Build a normal experiment prompt using:
             *
             * - the real cube schema
             * - the current prompt combination
             * - the normal JSON output contract
             *
             * Only the question itself is a dummy warm-up
             * question.
             */

            String warmUpPrompt =
                    promptBuilder.buildPrompt(
                            cubeSchema,
                            WARM_UP_QUESTION,
                            promptCombination
                    );


            /*
             * Use the same model configuration as the real runs.
             *
             * This means the warm-up uses the same:
             *
             * - num_ctx
             * - num_predict
             * - temperature
             * - top_k
             * - top_p
             * - seed/configuration behavior
             * - keep_alive
             * - streaming configuration
             *
             * depending on the values stored in modelConfig.
             */

            LLMCallResult warmUpResult =
                    llmClient.generate(
                            modelConfig,
                            warmUpPrompt
                    );


            if (warmUpResult.isSuccess()) {

                System.out.println(
                        "Warm-up completed"
                                + " | llm="
                                + modelConfig.getModelName()
                                + " | combination="
                                + combinationName
                                + " | timeMs="
                                + warmUpResult
                                .getResponseTimeMs()
                );

            } else {

                System.out.println(
                        "Warm-up failed"
                                + " | llm="
                                + modelConfig.getModelName()
                                + " | combination="
                                + combinationName
                                + " | error="
                                + warmUpResult
                                .getErrorType()
                                + " | message="
                                + warmUpResult
                                .getErrorMessage()
                );

                System.out.println(
                        "Continuing with measured runs."
                );
            }


        } catch (Exception e) {

            /*
             * A warm-up failure must never increment the measured
             * run count or create an ExperimentResult.
             */

            System.out.println(
                    "Warm-up exception"
                            + " | llm="
                            + modelConfig.getModelName()
                            + " | combination="
                            + combinationName
                            + " | exception="
                            + e.getClass().getSimpleName()
                            + ": "
                            + safe(
                            e.getMessage()
                    )
            );

            System.out.println(
                    "Continuing with measured runs."
            );
        }


        System.out.println();
    }


    /*
     * ==========================================================
     * CONSOLE OUTPUT
     * ==========================================================
     */

    private void printExperimentOverview(
            List<LLMModelConfig> modelConfigs,
            List<List<PromptTechnique>> promptCombinations,
            List<LLMExperimentCase> experimentCases,
            int totalRuns
    ) {

        System.out.println();

        System.out.println(
                "========================================"
        );

        System.out.println(
                "STARTING LLM EXPERIMENTS"
        );

        System.out.println(
                "========================================"
        );

        System.out.println(
                "Models: "
                        + modelConfigs.size()
        );

        System.out.println(
                "Prompt combinations: "
                        + promptCombinations.size()
        );

        System.out.println(
                "Paired test cases: "
                        + experimentCases.size()
        );

        System.out.println(
                "Semantic pairs: "
                        + (
                        experimentCases.size()
                                / 2
                )
        );

        System.out.println(
                "Order seed: "
                        + EXPERIMENT_ORDER_SEED
        );

        System.out.println(
                "Total measured runs: "
                        + totalRuns
        );

        System.out.println(
                "Warm-up calls are excluded from total runs."
        );

        System.out.println();
    }


    /*
     * ==========================================================
     * PRINT FINAL CASE ORDER
     * ==========================================================
     *
     * Printed once before experiments begin.
     *
     * This is useful for reproducibility and allows the exact
     * randomized ordering to be documented if necessary.
     * ==========================================================
     */

    private void printExperimentOrder(
            List<LLMExperimentCase> experimentCases
    ) {

        System.out.println(
                "========================================"
        );

        System.out.println(
                "DETERMINISTIC EXPERIMENT ORDER"
        );

        System.out.println(
                "Seed: "
                        + EXPERIMENT_ORDER_SEED
        );

        System.out.println(
                "========================================"
        );


        for (int i = 0;
             i < experimentCases.size();
             i++) {

            LLMExperimentCase experimentCase =
                    experimentCases.get(i);


            System.out.println(
                    (i + 1)
                            + ". "
                            + experimentCase.getTestId()
                            + " | pair="
                            + experimentCase.getPairId()
                            + " | wording="
                            + experimentCase.getWordingType()
            );
        }


        System.out.println();
    }


    /*
     * ==========================================================
     * RUN SUMMARY
     * ==========================================================
     */

    private void printRunSummary(
            ExperimentResult result,
            int totalRuns
    ) {

        String similarityText =
                result.isQuerySimilarityAvailable()
                        ? formatSimilarity(
                        result.getOverallSimilarity()
                )
                        : "N/A";


        System.out.println(
                "["
                        + runCounter
                        + "/"
                        + totalRuns
                        + "] "

                        + "llm="
                        + result.getLlmName()

                        + " | combination="
                        + result.getPromptCombinationName()

                        + " | test="
                        + result.getTestId()

                        + " | pair="
                        + result.getPairId()

                        + " | wording="
                        + result.getWordingType()

                        + " | strictAccuracy="
                        + result.getStrictAccuracy()

                        + " | overallSimilarity="
                        + similarityText

                        + " | structuralAccuracy="
                        + result.getStructuralAccuracy()

                        + " | strictValid="
                        + result.isStrictValid()

                        + " | timeMs="
                        + result.getResponseTimeMs()

                        + " | error="
                        + result.getErrorType()
        );
    }


    /*
     * ==========================================================
     * INPUT VALIDATION
     * ==========================================================
     */

    private void validateInputs(
            CubeSchema cubeSchema,
            List<LLMModelConfig> modelConfigs,
            List<List<PromptTechnique>> promptCombinations,
            List<LLMExperimentCase> experimentCases,
            ExperimentResultsWriter resultsWriter,
            ValidationLogWriter validationLogWriter
    ) {

        if (cubeSchema == null) {

            throw new IllegalArgumentException(
                    "cubeSchema cannot be null."
            );
        }


        if (modelConfigs == null
                || modelConfigs.isEmpty()) {

            throw new IllegalArgumentException(
                    "modelConfigs cannot be null or empty."
            );
        }


        for (LLMModelConfig modelConfig
                : modelConfigs) {

            if (modelConfig == null) {

                throw new IllegalArgumentException(
                        "modelConfigs cannot contain null."
                );
            }
        }


        /*
         * The outer list must not be empty.
         *
         * However, an individual combination CAN be empty:
         *
         * []
         *
         * This represents the baseline experiment.
         */

        if (promptCombinations == null
                || promptCombinations.isEmpty()) {

            throw new IllegalArgumentException(
                    "promptCombinations cannot be null or empty."
            );
        }


        for (List<PromptTechnique> combination
                : promptCombinations) {

            if (combination == null) {

                throw new IllegalArgumentException(
                        "promptCombinations cannot contain null combinations."
                );
            }


            for (PromptTechnique technique
                    : combination) {

                if (technique == null) {

                    throw new IllegalArgumentException(
                            "A prompt combination cannot contain null techniques."
                    );
                }
            }
        }


        if (experimentCases == null
                || experimentCases.isEmpty()) {

            throw new IllegalArgumentException(
                    "experimentCases cannot be null or empty."
            );
        }


        for (LLMExperimentCase experimentCase
                : experimentCases) {

            if (experimentCase == null) {

                throw new IllegalArgumentException(
                        "experimentCases cannot contain null."
                );
            }


            if (experimentCase.getExpectedQuery()
                    == null) {

                throw new IllegalArgumentException(
                        "Experiment case "
                                + experimentCase.getTestId()
                                + " has null expectedQuery."
                );
            }


            if (experimentCase.getQuestion() == null
                    || experimentCase
                    .getQuestion()
                    .trim()
                    .isEmpty()) {

                throw new IllegalArgumentException(
                        "Experiment case "
                                + experimentCase.getTestId()
                                + " has an empty question."
                );
            }


            /*
             * The new paired workload requires pair metadata.
             */

            if (experimentCase.getPairId() == null
                    || experimentCase
                    .getPairId()
                    .trim()
                    .isEmpty()) {

                throw new IllegalArgumentException(
                        "Experiment case "
                                + experimentCase.getTestId()
                                + " has an empty pairId."
                );
            }


            if (experimentCase.getWordingType() == null
                    || experimentCase
                    .getWordingType()
                    .trim()
                    .isEmpty()) {

                throw new IllegalArgumentException(
                        "Experiment case "
                                + experimentCase.getTestId()
                                + " has an empty wordingType."
                );
            }


            String wordingType =
                    experimentCase
                            .getWordingType();


            if (!LLMExperimentCase
                    .WORDING_TYPE_STRUCTURED
                    .equals(wordingType)
                    && !LLMExperimentCase
                    .WORDING_TYPE_NATURAL
                    .equals(wordingType)) {

                throw new IllegalArgumentException(
                        "Experiment case "
                                + experimentCase.getTestId()
                                + " has invalid wordingType: "
                                + wordingType
                );
            }
        }


        if (resultsWriter == null) {

            throw new IllegalArgumentException(
                    "resultsWriter cannot be null."
            );
        }


        if (validationLogWriter == null) {

            throw new IllegalArgumentException(
                    "validationLogWriter cannot be null."
            );
        }
    }


    /*
     * ==========================================================
     * RUN IDENTIFICATION
     * ==========================================================
     */

    private String nextRunId() {

        runCounter++;


        if (runCounter < 10) {

            return "run_000"
                    + runCounter;
        }


        if (runCounter < 100) {

            return "run_00"
                    + runCounter;
        }


        if (runCounter < 1000) {

            return "run_0"
                    + runCounter;
        }


        return "run_"
                + runCounter;
    }


    private String currentTimestamp() {

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern(
                        "yyyy-MM-dd HH:mm:ss"
                );


        return LocalDateTime
                .now()
                .format(
                        formatter
                );
    }


    /*
     * ==========================================================
     * HELPERS
     * ==========================================================
     */

    private String safe(
            String value
    ) {

        if (value == null) {
            return "";
        }

        return value.trim();
    }


    private String formatSimilarity(
            double value
    ) {

        return String.format(
                java.util.Locale.US,
                "%.2f",
                value
        );
    }
}