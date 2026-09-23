package LLM.experiments.results;

import LLM.experiments.ExpectedCubeQuery;
import LLM.experiments.LLMExperimentCase;
import LLM.experiments.ValidationResult;

import LLM.experiments.runner.LLMCallResult;
import LLM.experiments.runner.LLMModelConfig;
import LLM.experiments.runner.PromptSizeStats;

import LLM.experiments.similarity.QuerySimilarityResult;

public class ExperimentResult {

    private final String runId;
    private final String timestamp;

    private final String llmName;

    /*
     * Examples:
     *
     * baseline
     * instructions_for_query_generation
     * examples
     * instructions_for_query_generation+examples
     */
    private final String promptCombinationName;

    /*
     * ==========================================================
     * TEST CASE INFORMATION
     * ==========================================================
     */

    private final String testId;

    /*
     * Shared identifier between the structured and natural
     * versions of the same semantic query.
     *
     * Example:
     *
     * pair_01
     */
    private final String pairId;

    /*
     * Wording type:
     *
     * structured
     * natural
     */
    private final String wordingType;

    private final String difficulty;
    private final String category;
    private final String question;

    /*
     * Ground truth and actual LLM output.
     */
    private final ExpectedCubeQuery expectedQuery;
    private final String actualAnswer;

    /*
     * Strict validation:
     *
     * - format
     * - cube
     * - aggregation
     * - measure
     * - gamma
     * - sigma fields
     * - sigma values
     */
    private final ValidationResult strictValidationResult;

    /*
     * Structural validation:
     *
     * Same validation as strict validation,
     * but sigma value differences are ignored.
     */
    private final ValidationResult structuralValidationResult;

    /*
     * OLAP query similarity.
     *
     * Complementary metric used to measure how close
     * the generated query is to the expected query.
     *
     * It does NOT replace exact validation.
     */
    private final QuerySimilarityResult querySimilarityResult;

    /*
     * Prompt statistics.
     */
    private final PromptSizeStats promptSizeStats;

    /*
     * LLM request/result statistics.
     */
    private final LLMCallResult llmCallResult;


    /*
     * ==========================================================
     * MAIN CONSTRUCTOR
     *
     * Full constructor used by the new paired experiment.
     * ==========================================================
     */

    public ExperimentResult(
            String runId,
            String timestamp,
            String llmName,
            String promptCombinationName,
            String testId,
            String pairId,
            String wordingType,
            String difficulty,
            String category,
            String question,
            ExpectedCubeQuery expectedQuery,
            String actualAnswer,
            ValidationResult strictValidationResult,
            ValidationResult structuralValidationResult,
            QuerySimilarityResult querySimilarityResult,
            PromptSizeStats promptSizeStats,
            LLMCallResult llmCallResult
    ) {

        this.runId =
                safeString(
                        runId
                );

        this.timestamp =
                safeString(
                        timestamp
                );

        this.llmName =
                safeString(
                        llmName
                );

        this.promptCombinationName =
                safeString(
                        promptCombinationName
                );

        this.testId =
                safeString(
                        testId
                );

        this.pairId =
                safeString(
                        pairId
                );

        this.wordingType =
                safeString(
                        wordingType
                );

        this.difficulty =
                safeString(
                        difficulty
                );

        this.category =
                safeString(
                        category
                );

        this.question =
                safeString(
                        question
                );

        this.expectedQuery =
                expectedQuery;

        this.actualAnswer =
                safeString(
                        actualAnswer
                );

        this.strictValidationResult =
                strictValidationResult;

        this.structuralValidationResult =
                structuralValidationResult;

        this.querySimilarityResult =
                querySimilarityResult == null
                        ? QuerySimilarityResult.unavailable()
                        : querySimilarityResult;

        this.promptSizeStats =
                promptSizeStats;

        this.llmCallResult =
                llmCallResult;
    }


    /*
     * ==========================================================
     * COMPATIBILITY CONSTRUCTOR WITH SIMILARITY
     *
     * Allows older experiment code to continue working
     * without pair metadata.
     * ==========================================================
     */

    public ExperimentResult(
            String runId,
            String timestamp,
            String llmName,
            String promptCombinationName,
            String testId,
            String difficulty,
            String category,
            String question,
            ExpectedCubeQuery expectedQuery,
            String actualAnswer,
            ValidationResult strictValidationResult,
            ValidationResult structuralValidationResult,
            QuerySimilarityResult querySimilarityResult,
            PromptSizeStats promptSizeStats,
            LLMCallResult llmCallResult
    ) {

        this(
                runId,
                timestamp,
                llmName,
                promptCombinationName,
                testId,

                "",
                "",

                difficulty,
                category,
                question,
                expectedQuery,
                actualAnswer,
                strictValidationResult,
                structuralValidationResult,
                querySimilarityResult,
                promptSizeStats,
                llmCallResult
        );
    }


    /*
     * ==========================================================
     * COMPATIBILITY CONSTRUCTOR WITHOUT SIMILARITY
     *
     * Kept for older code.
     * ==========================================================
     */

    public ExperimentResult(
            String runId,
            String timestamp,
            String llmName,
            String promptCombinationName,
            String testId,
            String difficulty,
            String category,
            String question,
            ExpectedCubeQuery expectedQuery,
            String actualAnswer,
            ValidationResult strictValidationResult,
            ValidationResult structuralValidationResult,
            PromptSizeStats promptSizeStats,
            LLMCallResult llmCallResult
    ) {

        this(
                runId,
                timestamp,
                llmName,
                promptCombinationName,
                testId,
                difficulty,
                category,
                question,
                expectedQuery,
                actualAnswer,
                strictValidationResult,
                structuralValidationResult,
                QuerySimilarityResult.unavailable(),
                promptSizeStats,
                llmCallResult
        );
    }


    /*
     * ==========================================================
     * OPTIONAL FULL CONSTRUCTOR WITHOUT SIMILARITY
     *
     * Supports pair metadata while using unavailable similarity.
     * ==========================================================
     */

    public ExperimentResult(
            String runId,
            String timestamp,
            String llmName,
            String promptCombinationName,
            String testId,
            String pairId,
            String wordingType,
            String difficulty,
            String category,
            String question,
            ExpectedCubeQuery expectedQuery,
            String actualAnswer,
            ValidationResult strictValidationResult,
            ValidationResult structuralValidationResult,
            PromptSizeStats promptSizeStats,
            LLMCallResult llmCallResult
    ) {

        this(
                runId,
                timestamp,
                llmName,
                promptCombinationName,
                testId,
                pairId,
                wordingType,
                difficulty,
                category,
                question,
                expectedQuery,
                actualAnswer,
                strictValidationResult,
                structuralValidationResult,
                QuerySimilarityResult.unavailable(),
                promptSizeStats,
                llmCallResult
        );
    }


    /*
     * ==========================================================
     * MAIN FACTORY METHOD
     * ==========================================================
     */

    public static ExperimentResult from(
            String runId,
            String timestamp,
            LLMModelConfig modelConfig,
            String promptCombinationName,
            LLMExperimentCase experimentCase,
            String actualAnswer,
            ValidationResult strictValidationResult,
            ValidationResult structuralValidationResult,
            QuerySimilarityResult querySimilarityResult,
            PromptSizeStats promptSizeStats,
            LLMCallResult llmCallResult
    ) {

        return new ExperimentResult(
                runId,
                timestamp,

                modelConfig == null
                        ? ""
                        : modelConfig.getModelName(),

                promptCombinationName,

                experimentCase == null
                        ? ""
                        : experimentCase.getTestId(),

                experimentCase == null
                        ? ""
                        : experimentCase.getPairId(),

                experimentCase == null
                        ? ""
                        : experimentCase.getWordingType(),

                experimentCase == null
                        ? ""
                        : experimentCase.getDifficulty(),

                experimentCase == null
                        ? ""
                        : experimentCase.getCategory(),

                experimentCase == null
                        ? ""
                        : experimentCase.getQuestion(),

                experimentCase == null
                        ? null
                        : experimentCase.getExpectedQuery(),

                actualAnswer,

                strictValidationResult,
                structuralValidationResult,
                querySimilarityResult,

                promptSizeStats,
                llmCallResult
        );
    }


    /*
     * ==========================================================
     * COMPATIBILITY FACTORY METHOD
     *
     * Existing runner code can continue using this version
     * until similarity evaluation is connected.
     * ==========================================================
     */

    public static ExperimentResult from(
            String runId,
            String timestamp,
            LLMModelConfig modelConfig,
            String promptCombinationName,
            LLMExperimentCase experimentCase,
            String actualAnswer,
            ValidationResult strictValidationResult,
            ValidationResult structuralValidationResult,
            PromptSizeStats promptSizeStats,
            LLMCallResult llmCallResult
    ) {

        return from(
                runId,
                timestamp,
                modelConfig,
                promptCombinationName,
                experimentCase,
                actualAnswer,
                strictValidationResult,
                structuralValidationResult,
                QuerySimilarityResult.unavailable(),
                promptSizeStats,
                llmCallResult
        );
    }


    /*
     * ==========================================================
     * BASIC EXPERIMENT INFORMATION
     * ==========================================================
     */

    public String getRunId() {
        return runId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getLlmName() {
        return llmName;
    }

    public String getPromptCombinationName() {
        return promptCombinationName;
    }

    /*
     * Compatibility getter.
     *
     * Older writers may still call this method.
     * It now returns the prompt combination name.
     */
    public String getPromptTechniqueName() {
        return promptCombinationName;
    }


    /*
     * ==========================================================
     * TEST CASE INFORMATION
     * ==========================================================
     */

    public String getTestId() {
        return testId;
    }

    public String getPairId() {
        return pairId;
    }

    public String getWordingType() {
        return wordingType;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public String getCategory() {
        return category;
    }

    public String getQuestion() {
        return question;
    }


    /*
     * ==========================================================
     * EXPECTED / ACTUAL QUERY
     * ==========================================================
     */

    public ExpectedCubeQuery getExpectedQuery() {
        return expectedQuery;
    }

    public String getActualAnswer() {
        return actualAnswer;
    }


    /*
     * ==========================================================
     * RAW RESULT OBJECTS
     * ==========================================================
     */

    public PromptSizeStats getPromptSizeStats() {
        return promptSizeStats;
    }

    public LLMCallResult getLlmCallResult() {
        return llmCallResult;
    }

    public QuerySimilarityResult getQuerySimilarityResult() {
        return querySimilarityResult;
    }


    /*
     * ==========================================================
     * VALIDATION RESULTS
     * ==========================================================
     */

    public ValidationResult getStrictValidationResult() {
        return strictValidationResult;
    }

    public ValidationResult getStructuralValidationResult() {
        return structuralValidationResult;
    }


    /*
     * ==========================================================
     * STRICT METRICS
     * ==========================================================
     */

    public boolean isStrictValid() {

        return strictValidationResult != null
                && strictValidationResult.isValid();
    }

    public double getStrictAccuracy() {

        if (strictValidationResult == null) {
            return 0.0;
        }

        return strictValidationResult
                .getStrictAccuracy();
    }

    public double getStrictWeightedScore() {

        if (strictValidationResult == null) {
            return 0.0;
        }

        return strictValidationResult
                .getWeightedScore();
    }


    /*
     * ==========================================================
     * QUERY SIMILARITY METRICS
     *
     * All similarity scores are in [0, 1].
     *
     * Strict accuracy remains the primary correctness metric.
     * Similarity is complementary and describes error severity.
     * ==========================================================
     */

    public boolean isQuerySimilarityAvailable() {

        return querySimilarityResult != null
                && querySimilarityResult.isAvailable();
    }

    public double getOverallSimilarity() {

        if (!isQuerySimilarityAvailable()) {
            return 0.0;
        }

        return querySimilarityResult
                .getOverallSimilarity();
    }

    public double getGammaSimilarity() {

        if (!isQuerySimilarityAvailable()) {
            return 0.0;
        }

        return querySimilarityResult
                .getGammaSimilarity();
    }

    public double getSigmaSimilarity() {

        if (!isQuerySimilarityAvailable()) {
            return 0.0;
        }

        return querySimilarityResult
                .getSigmaSimilarity();
    }

    public double getMeasureSimilarity() {

        if (!isQuerySimilarityAvailable()) {
            return 0.0;
        }

        return querySimilarityResult
                .getMeasureSimilarity();
    }

    public double getAggregateSimilarity() {

        if (!isQuerySimilarityAvailable()) {
            return 0.0;
        }

        return querySimilarityResult
                .getAggregateSimilarity();
    }

    public double getCubeSimilarity() {

        if (!isQuerySimilarityAvailable()) {
            return 0.0;
        }

        return querySimilarityResult
                .getCubeSimilarity();
    }


    /*
     * ==========================================================
     * STRUCTURAL METRICS
     * ==========================================================
     */

    public boolean isStructuralValid() {

        return structuralValidationResult != null
                && structuralValidationResult.isValid();
    }

    public double getStructuralAccuracy() {

        if (structuralValidationResult == null) {
            return 0.0;
        }

        return structuralValidationResult
                .getStrictAccuracy();
    }

    public double getStructuralWeightedScore() {

        if (structuralValidationResult == null) {
            return 0.0;
        }

        return structuralValidationResult
                .getWeightedScore();
    }


    /*
     * ==========================================================
     * LEGACY / DEFAULT METRICS
     *
     * For compatibility, the default result is the STRICT result.
     * ==========================================================
     */

    public boolean isValid() {
        return isStrictValid();
    }

    public double getWeightedScore() {
        return getStrictWeightedScore();
    }

    public ValidationResult getValidationResult() {
        return strictValidationResult;
    }


    /*
     * ==========================================================
     * DETAILED STRICT VALIDATION METRICS
     * ==========================================================
     */

    public boolean isFormatValid() {

        return strictValidationResult != null
                && strictValidationResult.isFormatValid();
    }

    public boolean isCubeCorrect() {

        return strictValidationResult != null
                && strictValidationResult.isCubeCorrect();
    }

    public boolean isAggregateFunctionCorrect() {

        return strictValidationResult != null
                && strictValidationResult
                .isAggregateFunctionCorrect();
    }

    public boolean isMeasureCorrect() {

        return strictValidationResult != null
                && strictValidationResult.isMeasureCorrect();
    }

    public boolean isGammaCorrect() {

        return strictValidationResult != null
                && strictValidationResult.isGammaCorrect();
    }

    public boolean isSigmaCorrect() {

        return strictValidationResult != null
                && strictValidationResult.isSigmaCorrect();
    }

    public boolean isStructuralSigmaCorrect() {

        return structuralValidationResult != null
                && structuralValidationResult.isSigmaCorrect();
    }

    public int getUnknownFieldsCount() {

        if (strictValidationResult == null) {
            return 0;
        }

        return strictValidationResult
                .getUnknownFieldsCount();
    }

    public int getMissingGammaCount() {

        if (strictValidationResult == null) {
            return 0;
        }

        return strictValidationResult
                .getMissingGammaCount();
    }

    public int getExtraGammaCount() {

        if (strictValidationResult == null) {
            return 0;
        }

        return strictValidationResult
                .getExtraGammaCount();
    }

    public int getMissingSigmaCount() {

        if (strictValidationResult == null) {
            return 0;
        }

        return strictValidationResult
                .getMissingSigmaCount();
    }

    public int getExtraSigmaCount() {

        if (strictValidationResult == null) {
            return 0;
        }

        return strictValidationResult
                .getExtraSigmaCount();
    }

    public int getWrongSigmaValueCount() {

        if (strictValidationResult == null) {
            return 0;
        }

        return strictValidationResult
                .getWrongSigmaValueCount();
    }


    /*
     * ==========================================================
     * LLM CALL STATUS
     * ==========================================================
     */

    public boolean isLlmCallSuccessful() {

        return llmCallResult != null
                && llmCallResult.isSuccess();
    }

    public long getResponseTimeMs() {

        if (llmCallResult == null) {
            return -1L;
        }

        return llmCallResult
                .getResponseTimeMs();
    }

    public String getErrorType() {

        if (llmCallResult == null) {
            return "";
        }

        return llmCallResult
                .getErrorType();
    }

    public String getErrorMessage() {

        if (llmCallResult == null) {
            return "";
        }

        return llmCallResult
                .getErrorMessage();
    }


    /*
     * ==========================================================
     * PROMPT SIZE METRICS
     * ==========================================================
     */

    public int getPromptCharacterCount() {

        if (promptSizeStats == null) {
            return 0;
        }

        return promptSizeStats
                .getCharacterCount();
    }

    public int getPromptUtf8ByteCount() {

        if (promptSizeStats == null) {
            return 0;
        }

        return promptSizeStats
                .getUtf8ByteCount();
    }

    public long getPromptEstimatedTokensByCharacters() {

        if (promptSizeStats == null) {
            return 0L;
        }

        return promptSizeStats
                .getEstimatedTokensByCharacters();
    }

    public long getPromptEstimatedTokensByBytes() {

        if (promptSizeStats == null) {
            return 0L;
        }

        return promptSizeStats
                .getEstimatedTokensByBytes();
    }

    /*
     * Compatibility getter.
     */
    public long getPromptEstimatedTokens() {
        return getPromptEstimatedTokensByCharacters();
    }


    /*
     * ==========================================================
     * OLLAMA TOKEN METRICS
     * ==========================================================
     */

    public long getPromptEvalCount() {

        if (llmCallResult == null) {
            return -1L;
        }

        return llmCallResult
                .getPromptEvalCount();
    }

    public long getEvalCount() {

        if (llmCallResult == null) {
            return -1L;
        }

        return llmCallResult
                .getEvalCount();
    }

    public long getTotalTokenCount() {

        if (llmCallResult == null) {
            return 0L;
        }

        return llmCallResult
                .getTotalTokenCount();
    }


    /*
     * ==========================================================
     * OLLAMA TIMING METRICS
     * ==========================================================
     */

    public long getTotalDurationMs() {

        if (llmCallResult == null) {
            return -1L;
        }

        return llmCallResult
                .getTotalDurationMs();
    }

    public long getLoadDurationMs() {

        if (llmCallResult == null) {
            return -1L;
        }

        return llmCallResult
                .getLoadDurationMs();
    }

    public long getPromptEvalDurationMs() {

        if (llmCallResult == null) {
            return -1L;
        }

        return llmCallResult
                .getPromptEvalDurationMs();
    }

    public long getEvalDurationMs() {

        if (llmCallResult == null) {
            return -1L;
        }

        return llmCallResult
                .getEvalDurationMs();
    }

    public double getPromptTokensPerSecond() {

        if (llmCallResult == null) {
            return 0.0;
        }

        return llmCallResult
                .getPromptTokensPerSecond();
    }

    public double getOutputTokensPerSecond() {

        if (llmCallResult == null) {
            return 0.0;
        }

        return llmCallResult
                .getOutputTokensPerSecond();
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
     * DEBUG
     * ==========================================================
     */

    @Override
    public String toString() {

        return "ExperimentResult{" +

                "runId='"
                + runId
                + '\'' +

                ", timestamp='"
                + timestamp
                + '\'' +

                ", llmName='"
                + llmName
                + '\'' +

                ", promptCombinationName='"
                + promptCombinationName
                + '\'' +

                ", testId='"
                + testId
                + '\'' +

                ", pairId='"
                + pairId
                + '\'' +

                ", wordingType='"
                + wordingType
                + '\'' +

                ", difficulty='"
                + difficulty
                + '\'' +

                ", category='"
                + category
                + '\'' +

                ", strictValid="
                + isStrictValid() +

                ", overallSimilarity="
                + getOverallSimilarity() +

                ", structuralValid="
                + isStructuralValid() +

                ", responseTimeMs="
                + getResponseTimeMs() +

                ", errorType='"
                + getErrorType()
                + '\'' +

                '}';
    }
}