package LLM.experiments.results;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.List;

import LLM.experiments.ExpectedCubeQuery;
import LLM.experiments.ValidationResult;
import LLM.experiments.runner.LLMCallResult;

public class ValidationLogWriter implements Closeable {

    private final BufferedWriter writer;

    public ValidationLogWriter(Path outputPath) throws IOException {
        this.writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8);
    }

    public void writeResult(ExperimentResult result) throws IOException {
        writer.write("============================================================");
        writer.newLine();

        writer.write("Run ID: " + safe(result.getRunId()));
        writer.newLine();

        writer.write("Timestamp: " + safe(result.getTimestamp()));
        writer.newLine();

        writer.write("LLM: " + safe(result.getLlmName()));
        writer.newLine();

        writer.write("Prompt technique: " + safe(result.getPromptTechniqueName()));
        writer.newLine();

        writer.write("Test ID: " + safe(result.getTestId()));
        writer.newLine();

        writer.write("Difficulty: " + safe(result.getDifficulty()));
        writer.newLine();

        writer.newLine();

        writer.write("Question:");
        writer.newLine();
        writer.write(safe(result.getQuestion()));
        writer.newLine();

        writer.newLine();

        writer.write("Expected query:");
        writer.newLine();
        writeExpectedQuery(result.getExpectedQuery());

        writer.newLine();

        writer.write("Actual LLM answer:");
        writer.newLine();
        writeMultilineText(result.getActualAnswer());

        writer.newLine();

        writer.write("Validation summary:");
        writer.newLine();
        writeValidationSummary(result.getValidationResult());

        writer.newLine();

        writer.write("Validation errors:");
        writer.newLine();
        writeValidationErrors(result.getValidationResult());

        writer.newLine();

        writer.write("Timing and LLM stats:");
        writer.newLine();
        writeTimingAndStats(result.getLlmCallResult());

        writer.write("============================================================");
        writer.newLine();
        writer.newLine();

        writer.flush();
    }

    private void writeExpectedQuery(ExpectedCubeQuery expectedQuery) throws IOException {
        if (expectedQuery == null) {
            writer.write("No expected query available.");
            writer.newLine();
            return;
        }

        writer.write("cubeName: " + safe(expectedQuery.getCubeName()));
        writer.newLine();

        writer.write("aggregateFunction: " + safe(expectedQuery.getAggregateFunction()));
        writer.newLine();

        writer.write("measure: " + safe(expectedQuery.getMeasure()));
        writer.newLine();

        writer.write("gamma: " + joinList(expectedQuery.getGammaFields()));
        writer.newLine();

        writer.write("sigma: " + joinList(expectedQuery.getSigmaConditions()));
        writer.newLine();
    }

    private void writeValidationSummary(ValidationResult validationResult) throws IOException {
        if (validationResult == null) {
            writer.write("No validation result available.");
            writer.newLine();
            return;
        }

        writer.write("strict_valid: " + validationResult.isValid());
        writer.newLine();

        writer.write("strict_accuracy: " + validationResult.getStrictAccuracy());
        writer.newLine();

        writer.write("weighted_score: " + validationResult.getWeightedScore());
        writer.newLine();

        writer.write("format_valid: " + validationResult.isFormatValid());
        writer.newLine();

        writer.write("cube_correct: " + validationResult.isCubeCorrect());
        writer.newLine();

        writer.write("aggregate_correct: " + validationResult.isAggregateFunctionCorrect());
        writer.newLine();

        writer.write("measure_correct: " + validationResult.isMeasureCorrect());
        writer.newLine();

        writer.write("gamma_correct: " + validationResult.isGammaCorrect());
        writer.newLine();

        writer.write("sigma_correct: " + validationResult.isSigmaCorrect());
        writer.newLine();

        writer.write("unknown_fields_count: " + validationResult.getUnknownFieldsCount());
        writer.newLine();

        writer.write("missing_gamma_count: " + validationResult.getMissingGammaCount());
        writer.newLine();

        writer.write("extra_gamma_count: " + validationResult.getExtraGammaCount());
        writer.newLine();

        writer.write("missing_sigma_count: " + validationResult.getMissingSigmaCount());
        writer.newLine();

        writer.write("extra_sigma_count: " + validationResult.getExtraSigmaCount());
        writer.newLine();

        writer.write("wrong_sigma_value_count: " + validationResult.getWrongSigmaValueCount());
        writer.newLine();
    }

    private void writeValidationErrors(ValidationResult validationResult) throws IOException {
        if (validationResult == null) {
            writer.write("- No validation result available.");
            writer.newLine();
            return;
        }

        List<String> errors = validationResult.getErrors();

        if (errors == null || errors.isEmpty()) {
            writer.write("- No validation errors.");
            writer.newLine();
            return;
        }

        for (String error : errors) {
            writer.write("- " + safe(error));
            writer.newLine();
        }
    }

    private void writeTimingAndStats(LLMCallResult callResult) throws IOException {
        if (callResult == null) {
            writer.write("No LLM call result available.");
            writer.newLine();
            return;
        }

        writer.write("llm_call_success: " + callResult.isSuccess());
        writer.newLine();

        writer.write("response_time_ms: " + callResult.getResponseTimeMs());
        writer.newLine();

        writer.write("prompt_eval_count: " + callResult.getPromptEvalCount());
        writer.newLine();

        writer.write("eval_count: " + callResult.getEvalCount());
        writer.newLine();

        writer.write("total_tokens: " + callResult.getTotalTokenCount());
        writer.newLine();

        writer.write("total_duration_ms: " + callResult.getTotalDurationMs());
        writer.newLine();

        writer.write("prompt_eval_duration_ms: " + callResult.getPromptEvalDurationMs());
        writer.newLine();

        writer.write("eval_duration_ms: " + callResult.getEvalDurationMs());
        writer.newLine();

        writer.write("prompt_tokens_per_second: " + callResult.getPromptTokensPerSecond());
        writer.newLine();

        writer.write("output_tokens_per_second: " + callResult.getOutputTokensPerSecond());
        writer.newLine();

        if (callResult.isFailed()) {
            writer.write("error_type: " + safe(callResult.getErrorType()));
            writer.newLine();

            writer.write("error_message: " + safe(callResult.getErrorMessage()));
            writer.newLine();
        }
    }

    private void writeMultilineText(String text) throws IOException {
        if (text == null || text.trim().isEmpty()) {
            writer.write("");
            writer.newLine();
            return;
        }

        String normalizedText = text
                .replace("\r\n", "\n")
                .replace("\r", "\n");

        String[] lines = normalizedText.split("\n");

        for (String line : lines) {
            writer.write(line);
            writer.newLine();
        }
    }

    private String joinList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < values.size(); i++) {
            builder.append(safe(values.get(i)));

            if (i < values.size() - 1) {
                builder.append(", ");
            }
        }

        return builder.toString();
    }

    private String safe(String value) {
        if (value == null) {
            return "";
        }

        return value.trim();
    }

    @Override
    public void close() throws IOException {
        writer.flush();
        writer.close();
    }
}