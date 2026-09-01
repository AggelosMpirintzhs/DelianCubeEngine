package LLM.experiments.results;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.Locale;

public class ExperimentResultsWriter implements Closeable {

    private final BufferedWriter writer;
    private boolean headerWritten;

    public ExperimentResultsWriter(Path outputPath) throws IOException {
        this.writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8);
        this.headerWritten = false;
    }

    public void writeHeaderIfNeeded() throws IOException {
        if (headerWritten) {
            return;
        }

        writer.write(joinTsv(new String[]{
                "llm",
                "prompt_technique",
                "test_id",
                "weighted_score",
                "response_time_ms",

                "run_id",
                "timestamp",
                "difficulty",
                "question",

                "llm_call_success",
                "strict_valid",
                "strict_accuracy",

                "format_valid",
                "cube_correct",
                "aggregate_correct",
                "measure_correct",
                "gamma_correct",
                "sigma_correct",

                "unknown_fields_count",
                "missing_gamma_count",
                "extra_gamma_count",
                "missing_sigma_count",
                "extra_sigma_count",
                "wrong_sigma_value_count",

                "prompt_chars",
                "prompt_utf8_bytes",
                "prompt_estimated_tokens",

                "prompt_eval_count",
                "eval_count",
                "total_duration_ms",
                "prompt_eval_duration_ms",
                "eval_duration_ms",

                "error_type",
                "error_message"
        }));

        writer.newLine();
        writer.flush();

        headerWritten = true;
    }

    public void writeResult(ExperimentResult result) throws IOException {
        writeHeaderIfNeeded();

        writer.write(joinTsv(new String[]{
                result.getLlmName(),
                result.getPromptTechniqueName(),
                result.getTestId(),
                formatDouble(result.getWeightedScore()),
                String.valueOf(result.getResponseTimeMs()),

                result.getRunId(),
                result.getTimestamp(),
                result.getDifficulty(),
                result.getQuestion(),

                booleanToString(result.isLlmCallSuccessful()),
                booleanToString(result.isValid()),
                formatDouble(result.getStrictAccuracy()),

                booleanToString(result.isFormatValid()),
                booleanToString(result.isCubeCorrect()),
                booleanToString(result.isAggregateFunctionCorrect()),
                booleanToString(result.isMeasureCorrect()),
                booleanToString(result.isGammaCorrect()),
                booleanToString(result.isSigmaCorrect()),

                String.valueOf(result.getUnknownFieldsCount()),
                String.valueOf(result.getMissingGammaCount()),
                String.valueOf(result.getExtraGammaCount()),
                String.valueOf(result.getMissingSigmaCount()),
                String.valueOf(result.getExtraSigmaCount()),
                String.valueOf(result.getWrongSigmaValueCount()),

                String.valueOf(result.getPromptCharacterCount()),
                String.valueOf(result.getPromptUtf8ByteCount()),
                String.valueOf(result.getPromptEstimatedTokens()),

                String.valueOf(result.getPromptEvalCount()),
                String.valueOf(result.getEvalCount()),
                String.valueOf(result.getTotalDurationMs()),
                String.valueOf(result.getPromptEvalDurationMs()),
                String.valueOf(result.getEvalDurationMs()),

                result.getErrorType(),
                result.getErrorMessage()
        }));

        writer.newLine();
        writer.flush();
    }

    private static String joinTsv(String[] values) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < values.length; i++) {
            builder.append(escapeTsv(values[i]));

            if (i < values.length - 1) {
                builder.append('\t');
            }
        }

        return builder.toString();
    }

    private static String escapeTsv(String value) {
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

    private static String booleanToString(boolean value) {
        return value ? "true" : "false";
    }

    private static String formatDouble(double value) {
        return String.format(Locale.US, "%.2f", value);
    }

    @Override
    public void close() throws IOException {
        writer.flush();
        writer.close();
    }
}