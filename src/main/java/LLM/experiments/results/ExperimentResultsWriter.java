package LLM.experiments.results;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.List;
import java.util.Locale;

import LLM.experiments.ExpectedCubeQuery;

public class ExperimentResultsWriter implements Closeable {

    private final BufferedWriter writer;
    private boolean headerWritten;


    public ExperimentResultsWriter(
            Path outputPath
    ) throws IOException {

        if (outputPath == null) {
            throw new IllegalArgumentException(
                    "outputPath cannot be null."
            );
        }

        Path parent =
                outputPath
                        .toAbsolutePath()
                        .getParent();

        if (parent != null) {
            Files.createDirectories(
                    parent
            );
        }

        this.writer =
                Files.newBufferedWriter(
                        outputPath,
                        StandardCharsets.UTF_8
                );

        this.headerWritten = false;
    }


    /*
     * ==========================================================
     * HEADER
     *
     * Columns are intentionally ordered from the most useful
     * experiment information to progressively more detailed
     * diagnostic / technical information.
     * ==========================================================
     */

    public void writeHeaderIfNeeded()
            throws IOException {

        if (headerWritten) {
            return;
        }

        writer.write(
                joinTsv(
                        new String[]{

                                /*
                                 * ==================================================
                                 * EXPERIMENT IDENTITY
                                 * ==================================================
                                 */

                                "run_id",
                                "llm",
                                "prompt_combination",

                                /*
                                 * Test case identity.
                                 *
                                 * pair_id is shared by the structured and
                                 * natural versions of the same semantic query.
                                 *
                                 * Example:
                                 *
                                 * test_id:
                                 *   pair_01_structured
                                 *
                                 * pair_id:
                                 *   pair_01
                                 *
                                 * wording_type:
                                 *   structured
                                 */
                                "test_id",
                                "pair_id",
                                "wording_type",

                                "difficulty",
                                "category",


                                /*
                                 * ==================================================
                                 * PRIMARY EXPERIMENT METRICS
                                 *
                                 * These are intentionally placed together so that
                                 * they are immediately visible in TSV / Excel.
                                 * ==================================================
                                 */

                                "strict_accuracy",
                                "overall_similarity",
                                "structural_accuracy",

                                /*
                                 * Original end-to-end response time measured
                                 * by the Java client.
                                 */
                                "response_time_ms",

                                /*
                                 * Shows whether similarity was actually calculated.
                                 */
                                "similarity_available",


                                /*
                                 * ==================================================
                                 * COMPONENT SIMILARITY
                                 *
                                 * Used mainly for diagnostic analysis.
                                 * ==================================================
                                 */

                                "gamma_similarity",
                                "sigma_similarity",
                                "measure_similarity",
                                "aggregate_similarity",
                                "cube_similarity",


                                /*
                                 * ==================================================
                                 * VALIDATION SUMMARY
                                 * ==================================================
                                 */

                                "strict_valid",
                                "structural_valid",

                                "strict_weighted_score",
                                "structural_weighted_score",


                                /*
                                 * ==================================================
                                 * DETAILED EXACT VALIDATION
                                 * ==================================================
                                 */

                                "format_valid",

                                "cube_correct",
                                "aggregate_correct",
                                "measure_correct",
                                "gamma_correct",
                                "sigma_correct",

                                "structural_sigma_correct",


                                /*
                                 * ==================================================
                                 * ERROR COUNTS
                                 * ==================================================
                                 */

                                "unknown_fields_count",

                                "missing_gamma_count",
                                "extra_gamma_count",

                                "missing_sigma_count",
                                "extra_sigma_count",

                                "wrong_sigma_value_count",


                                /*
                                 * ==================================================
                                 * QUESTION / QUERY INFORMATION
                                 * ==================================================
                                 */

                                "question",

                                "expected_cube",
                                "expected_aggregate",
                                "expected_measure",
                                "expected_gamma",
                                "expected_sigma",

                                "actual_answer",


                                /*
                                 * ==================================================
                                 * PROMPT SIZE
                                 * ==================================================
                                 */

                                "prompt_chars",
                                "prompt_utf8_bytes",

                                "prompt_estimated_tokens_chars",
                                "prompt_estimated_tokens_bytes",


                                /*
                                 * ==================================================
                                 * ACTUAL OLLAMA TOKEN COUNTS
                                 * ==================================================
                                 */

                                "prompt_eval_count",
                                "eval_count",
                                "total_tokens",


                                /*
                                 * ==================================================
                                 * OLLAMA TIMING
                                 * ==================================================
                                 */

                                "total_duration_ms",
                                "load_duration_ms",

                                /*
                                 * Primary timing metric for later analysis:
                                 *
                                 * total_duration_ms - load_duration_ms
                                 *
                                 * This reduces the influence of model-loading
                                 * overhead on timing comparisons.
                                 */
                                "adjusted_duration_ms",
                                "adjusted_duration_s",

                                "prompt_eval_duration_ms",
                                "eval_duration_ms",


                                /*
                                 * ==================================================
                                 * THROUGHPUT
                                 * ==================================================
                                 */

                                "prompt_tokens_per_second",
                                "output_tokens_per_second",


                                /*
                                 * ==================================================
                                 * TECHNICAL / ERROR INFORMATION
                                 * ==================================================
                                 */

                                "llm_call_success",
                                "error_type",
                                "error_message",


                                /*
                                 * Timestamp is useful for reproducibility,
                                 * but not important during normal analysis,
                                 * therefore it is deliberately placed last.
                                 */
                                "timestamp"
                        }
                )
        );

        writer.newLine();
        writer.flush();

        headerWritten = true;
    }


    /*
     * ==========================================================
     * WRITE ONE RESULT
     * ==========================================================
     */

    public void writeResult(
            ExperimentResult result
    ) throws IOException {

        if (result == null) {
            throw new IllegalArgumentException(
                    "ExperimentResult cannot be null."
            );
        }

        writeHeaderIfNeeded();

        ExpectedCubeQuery expectedQuery =
                result.getExpectedQuery();

        writer.write(
                joinTsv(
                        new String[]{

                                /*
                                 * ==================================================
                                 * EXPERIMENT IDENTITY
                                 * ==================================================
                                 */

                                result.getRunId(),

                                result.getLlmName(),

                                result.getPromptCombinationName(),

                                result.getTestId(),

                                result.getPairId(),

                                result.getWordingType(),

                                result.getDifficulty(),

                                result.getCategory(),


                                /*
                                 * ==================================================
                                 * PRIMARY EXPERIMENT METRICS
                                 * ==================================================
                                 */

                                formatDouble(
                                        result.getStrictAccuracy()
                                ),

                                formatSimilarity(
                                        result,
                                        result.getOverallSimilarity()
                                ),

                                formatDouble(
                                        result.getStructuralAccuracy()
                                ),

                                String.valueOf(
                                        result.getResponseTimeMs()
                                ),

                                booleanToString(
                                        result.isQuerySimilarityAvailable()
                                ),


                                /*
                                 * ==================================================
                                 * COMPONENT SIMILARITY
                                 * ==================================================
                                 */

                                formatSimilarity(
                                        result,
                                        result.getGammaSimilarity()
                                ),

                                formatSimilarity(
                                        result,
                                        result.getSigmaSimilarity()
                                ),

                                formatSimilarity(
                                        result,
                                        result.getMeasureSimilarity()
                                ),

                                formatSimilarity(
                                        result,
                                        result.getAggregateSimilarity()
                                ),

                                formatSimilarity(
                                        result,
                                        result.getCubeSimilarity()
                                ),


                                /*
                                 * ==================================================
                                 * VALIDATION SUMMARY
                                 * ==================================================
                                 */

                                booleanToString(
                                        result.isStrictValid()
                                ),

                                booleanToString(
                                        result.isStructuralValid()
                                ),

                                formatDouble(
                                        result.getStrictWeightedScore()
                                ),

                                formatDouble(
                                        result.getStructuralWeightedScore()
                                ),


                                /*
                                 * ==================================================
                                 * DETAILED EXACT VALIDATION
                                 * ==================================================
                                 */

                                booleanToString(
                                        result.isFormatValid()
                                ),

                                booleanToString(
                                        result.isCubeCorrect()
                                ),

                                booleanToString(
                                        result.isAggregateFunctionCorrect()
                                ),

                                booleanToString(
                                        result.isMeasureCorrect()
                                ),

                                booleanToString(
                                        result.isGammaCorrect()
                                ),

                                booleanToString(
                                        result.isSigmaCorrect()
                                ),

                                booleanToString(
                                        result.isStructuralSigmaCorrect()
                                ),


                                /*
                                 * ==================================================
                                 * ERROR COUNTS
                                 * ==================================================
                                 */

                                String.valueOf(
                                        result.getUnknownFieldsCount()
                                ),

                                String.valueOf(
                                        result.getMissingGammaCount()
                                ),

                                String.valueOf(
                                        result.getExtraGammaCount()
                                ),

                                String.valueOf(
                                        result.getMissingSigmaCount()
                                ),

                                String.valueOf(
                                        result.getExtraSigmaCount()
                                ),

                                String.valueOf(
                                        result.getWrongSigmaValueCount()
                                ),


                                /*
                                 * ==================================================
                                 * QUESTION / QUERY INFORMATION
                                 * ==================================================
                                 */

                                result.getQuestion(),

                                getExpectedCube(
                                        expectedQuery
                                ),

                                getExpectedAggregate(
                                        expectedQuery
                                ),

                                getExpectedMeasure(
                                        expectedQuery
                                ),

                                getExpectedGamma(
                                        expectedQuery
                                ),

                                getExpectedSigma(
                                        expectedQuery
                                ),

                                result.getActualAnswer(),


                                /*
                                 * ==================================================
                                 * PROMPT SIZE
                                 * ==================================================
                                 */

                                String.valueOf(
                                        result.getPromptCharacterCount()
                                ),

                                String.valueOf(
                                        result.getPromptUtf8ByteCount()
                                ),

                                String.valueOf(
                                        result.getPromptEstimatedTokensByCharacters()
                                ),

                                String.valueOf(
                                        result.getPromptEstimatedTokensByBytes()
                                ),


                                /*
                                 * ==================================================
                                 * ACTUAL OLLAMA TOKEN COUNTS
                                 * ==================================================
                                 */

                                String.valueOf(
                                        result.getPromptEvalCount()
                                ),

                                String.valueOf(
                                        result.getEvalCount()
                                ),

                                String.valueOf(
                                        result.getTotalTokenCount()
                                ),


                                /*
                                 * ==================================================
                                 * OLLAMA TIMING
                                 * ==================================================
                                 */

                                String.valueOf(
                                        result.getTotalDurationMs()
                                ),

                                String.valueOf(
                                        result.getLoadDurationMs()
                                ),

                                formatAdjustedDurationMs(
                                        result
                                ),

                                formatAdjustedDurationSeconds(
                                        result
                                ),

                                String.valueOf(
                                        result.getPromptEvalDurationMs()
                                ),

                                String.valueOf(
                                        result.getEvalDurationMs()
                                ),


                                /*
                                 * ==================================================
                                 * THROUGHPUT
                                 * ==================================================
                                 */

                                formatDouble(
                                        result.getPromptTokensPerSecond()
                                ),

                                formatDouble(
                                        result.getOutputTokensPerSecond()
                                ),


                                /*
                                 * ==================================================
                                 * TECHNICAL / ERROR INFORMATION
                                 * ==================================================
                                 */

                                booleanToString(
                                        result.isLlmCallSuccessful()
                                ),

                                result.getErrorType(),

                                result.getErrorMessage(),

                                result.getTimestamp()
                        }
                )
        );

        writer.newLine();

        /*
         * Flush after every run.
         *
         * Completed results remain available even if a later
         * experiment crashes.
         */
        writer.flush();
    }


    /*
     * ==========================================================
     * EXPECTED QUERY HELPERS
     * ==========================================================
     */

    private static String getExpectedCube(
            ExpectedCubeQuery expectedQuery
    ) {

        if (expectedQuery == null) {
            return "";
        }

        return safeString(
                expectedQuery.getCubeName()
        );
    }


    private static String getExpectedAggregate(
            ExpectedCubeQuery expectedQuery
    ) {

        if (expectedQuery == null) {
            return "";
        }

        return safeString(
                expectedQuery.getAggregateFunction()
        );
    }


    private static String getExpectedMeasure(
            ExpectedCubeQuery expectedQuery
    ) {

        if (expectedQuery == null) {
            return "";
        }

        return safeString(
                expectedQuery.getMeasure()
        );
    }


    private static String getExpectedGamma(
            ExpectedCubeQuery expectedQuery
    ) {

        if (expectedQuery == null) {
            return "";
        }

        return joinList(
                expectedQuery.getGammaFields()
        );
    }


    private static String getExpectedSigma(
            ExpectedCubeQuery expectedQuery
    ) {

        if (expectedQuery == null) {
            return "";
        }

        return joinList(
                expectedQuery.getSigmaConditions()
        );
    }


    private static String joinList(
            List<String> values
    ) {

        if (values == null
                || values.isEmpty()) {

            return "[]";
        }

        StringBuilder builder =
                new StringBuilder();

        builder.append("[");

        for (int i = 0;
             i < values.size();
             i++) {

            if (i > 0) {
                builder.append(", ");
            }

            builder.append(
                    safeString(
                            values.get(i)
                    )
            );
        }

        builder.append("]");

        return builder.toString();
    }


    /*
     * ==========================================================
     * TIMING HELPERS
     * ==========================================================
     */

    /*
     * adjustedDuration =
     *
     * totalDuration - loadDuration
     *
     * If Ollama timing information is unavailable,
     * an empty TSV cell is written.
     */
    private static long calculateAdjustedDurationMs(
            ExperimentResult result
    ) {

        if (result == null) {
            return -1L;
        }

        long totalDurationMs =
                result.getTotalDurationMs();

        long loadDurationMs =
                result.getLoadDurationMs();

        if (totalDurationMs < 0L
                || loadDurationMs < 0L) {

            return -1L;
        }

        long adjustedDurationMs =
                totalDurationMs
                        - loadDurationMs;

        if (adjustedDurationMs < 0L) {
            return -1L;
        }

        return adjustedDurationMs;
    }


    private static String formatAdjustedDurationMs(
            ExperimentResult result
    ) {

        long adjustedDurationMs =
                calculateAdjustedDurationMs(
                        result
                );

        if (adjustedDurationMs < 0L) {
            return "";
        }

        return String.valueOf(
                adjustedDurationMs
        );
    }


    private static String formatAdjustedDurationSeconds(
            ExperimentResult result
    ) {

        long adjustedDurationMs =
                calculateAdjustedDurationMs(
                        result
                );

        if (adjustedDurationMs < 0L) {
            return "";
        }

        double adjustedDurationSeconds =
                adjustedDurationMs
                        / 1000.0;

        return String.format(
                Locale.US,
                "%.3f",
                adjustedDurationSeconds
        );
    }


    /*
     * ==========================================================
     * SIMILARITY HELPERS
     * ==========================================================
     */

    /*
     * An unavailable similarity result must not be written as
     * 0.00 because:
     *
     * unavailable != actual similarity of zero.
     *
     * Empty TSV cells also become empty Excel cells later.
     */
    private static String formatSimilarity(
            ExperimentResult result,
            double value
    ) {

        if (result == null
                || !result.isQuerySimilarityAvailable()) {

            return "";
        }

        return formatDouble(
                value
        );
    }


    /*
     * ==========================================================
     * TSV HELPERS
     * ==========================================================
     */

    private static String joinTsv(
            String[] values
    ) {

        StringBuilder builder =
                new StringBuilder();

        for (int i = 0;
             i < values.length;
             i++) {

            builder.append(
                    escapeTsv(
                            values[i]
                    )
            );

            if (i < values.length - 1) {
                builder.append('\t');
            }
        }

        return builder.toString();
    }


    private static String escapeTsv(
            String value
    ) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\t", " ")
                .replace("\r\n", "\\n")
                .replace("\r", "\\n")
                .replace("\n", "\\n")
                .trim();
    }


    private static String booleanToString(
            boolean value
    ) {

        return value
                ? "true"
                : "false";
    }


    private static String formatDouble(
            double value
    ) {

        return String.format(
                Locale.US,
                "%.2f",
                value
        );
    }


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
     * CLOSE
     * ==========================================================
     */

    @Override
    public void close()
            throws IOException {

        writer.flush();
        writer.close();
    }
}