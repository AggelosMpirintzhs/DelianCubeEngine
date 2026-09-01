package LLM.experiments.results;

import LLM.experiments.ExpectedCubeQuery;
import LLM.experiments.LLMExperimentCase;
import LLM.experiments.ValidationResult;
import LLM.experiments.runner.LLMCallResult;
import LLM.experiments.runner.LLMModelConfig;
import LLM.experiments.runner.PromptSizeStats;

public class ExperimentResult {

    private final String runId;
    private final String timestamp;

    private final String llmName;
    private final String promptTechniqueName;

    private final String testId;
    private final String difficulty;
    private final String question;

    private final ExpectedCubeQuery expectedQuery;
    private final String actualAnswer;

    private final ValidationResult validationResult;
    private final PromptSizeStats promptSizeStats;
    private final LLMCallResult llmCallResult;

    public ExperimentResult(
            String runId,
            String timestamp,
            String llmName,
            String promptTechniqueName,
            String testId,
            String difficulty,
            String question,
            ExpectedCubeQuery expectedQuery,
            String actualAnswer,
            ValidationResult validationResult,
            PromptSizeStats promptSizeStats,
            LLMCallResult llmCallResult
    ) {
        this.runId = safeString(runId);
        this.timestamp = safeString(timestamp);
        this.llmName = safeString(llmName);
        this.promptTechniqueName = safeString(promptTechniqueName);
        this.testId = safeString(testId);
        this.difficulty = safeString(difficulty);
        this.question = safeString(question);
        this.expectedQuery = expectedQuery;
        this.actualAnswer = safeString(actualAnswer);
        this.validationResult = validationResult;
        this.promptSizeStats = promptSizeStats;
        this.llmCallResult = llmCallResult;
    }

    public static ExperimentResult from(
            String runId,
            String timestamp,
            LLMModelConfig modelConfig,
            String promptTechniqueName,
            LLMExperimentCase experimentCase,
            String actualAnswer,
            ValidationResult validationResult,
            PromptSizeStats promptSizeStats,
            LLMCallResult llmCallResult
    ) {
        return new ExperimentResult(
                runId,
                timestamp,
                modelConfig == null ? "" : modelConfig.getModelName(),
                promptTechniqueName,
                experimentCase == null ? "" : experimentCase.getTestId(),
                experimentCase == null ? "" : experimentCase.getDifficulty(),
                experimentCase == null ? "" : experimentCase.getQuestion(),
                experimentCase == null ? null : experimentCase.getExpectedQuery(),
                actualAnswer,
                validationResult,
                promptSizeStats,
                llmCallResult
        );
    }

    public String getRunId() {
        return runId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getLlmName() {
        return llmName;
    }

    public String getPromptTechniqueName() {
        return promptTechniqueName;
    }

    public String getTestId() {
        return testId;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public String getQuestion() {
        return question;
    }

    public ExpectedCubeQuery getExpectedQuery() {
        return expectedQuery;
    }

    public String getActualAnswer() {
        return actualAnswer;
    }

    public ValidationResult getValidationResult() {
        return validationResult;
    }

    public PromptSizeStats getPromptSizeStats() {
        return promptSizeStats;
    }

    public LLMCallResult getLlmCallResult() {
        return llmCallResult;
    }

    public boolean isLlmCallSuccessful() {
        return llmCallResult != null && llmCallResult.isSuccess();
    }

    public boolean isValid() {
        return validationResult != null && validationResult.isValid();
    }

    public double getWeightedScore() {
        if (validationResult == null) {
            return 0.0;
        }

        return validationResult.getWeightedScore();
    }

    public double getStrictAccuracy() {
        if (validationResult == null) {
            return 0.0;
        }

        return validationResult.getStrictAccuracy();
    }

    public long getResponseTimeMs() {
        if (llmCallResult == null) {
            return -1L;
        }

        return llmCallResult.getResponseTimeMs();
    }

    public String getErrorType() {
        if (llmCallResult == null) {
            return "";
        }

        return llmCallResult.getErrorType();
    }

    public String getErrorMessage() {
        if (llmCallResult == null) {
            return "";
        }

        return llmCallResult.getErrorMessage();
    }

    public int getPromptCharacterCount() {
        if (promptSizeStats == null) {
            return 0;
        }

        return promptSizeStats.getCharacterCount();
    }

    public int getPromptUtf8ByteCount() {
        if (promptSizeStats == null) {
            return 0;
        }

        return promptSizeStats.getUtf8ByteCount();
    }

    public long getPromptEstimatedTokens() {
        if (promptSizeStats == null) {
            return 0L;
        }

        return promptSizeStats.getEstimatedTokensByCharacters();
    }

    public long getPromptEvalCount() {
        if (llmCallResult == null) {
            return -1L;
        }

        return llmCallResult.getPromptEvalCount();
    }

    public long getEvalCount() {
        if (llmCallResult == null) {
            return -1L;
        }

        return llmCallResult.getEvalCount();
    }

    public long getTotalDurationMs() {
        if (llmCallResult == null) {
            return -1L;
        }

        return llmCallResult.getTotalDurationMs();
    }

    public long getPromptEvalDurationMs() {
        if (llmCallResult == null) {
            return -1L;
        }

        return llmCallResult.getPromptEvalDurationMs();
    }

    public long getEvalDurationMs() {
        if (llmCallResult == null) {
            return -1L;
        }

        return llmCallResult.getEvalDurationMs();
    }

    public boolean isFormatValid() {
        return validationResult != null && validationResult.isFormatValid();
    }

    public boolean isCubeCorrect() {
        return validationResult != null && validationResult.isCubeCorrect();
    }

    public boolean isAggregateFunctionCorrect() {
        return validationResult != null && validationResult.isAggregateFunctionCorrect();
    }

    public boolean isMeasureCorrect() {
        return validationResult != null && validationResult.isMeasureCorrect();
    }

    public boolean isGammaCorrect() {
        return validationResult != null && validationResult.isGammaCorrect();
    }

    public boolean isSigmaCorrect() {
        return validationResult != null && validationResult.isSigmaCorrect();
    }

    public int getUnknownFieldsCount() {
        if (validationResult == null) {
            return 0;
        }

        return validationResult.getUnknownFieldsCount();
    }

    public int getMissingGammaCount() {
        if (validationResult == null) {
            return 0;
        }

        return validationResult.getMissingGammaCount();
    }

    public int getExtraGammaCount() {
        if (validationResult == null) {
            return 0;
        }

        return validationResult.getExtraGammaCount();
    }

    public int getMissingSigmaCount() {
        if (validationResult == null) {
            return 0;
        }

        return validationResult.getMissingSigmaCount();
    }

    public int getExtraSigmaCount() {
        if (validationResult == null) {
            return 0;
        }

        return validationResult.getExtraSigmaCount();
    }

    public int getWrongSigmaValueCount() {
        if (validationResult == null) {
            return 0;
        }

        return validationResult.getWrongSigmaValueCount();
    }

    private static String safeString(String value) {
        if (value == null) {
            return "";
        }

        return value.trim();
    }

    @Override
    public String toString() {
        return "ExperimentResult{" +
                "runId='" + runId + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", llmName='" + llmName + '\'' +
                ", promptTechniqueName='" + promptTechniqueName + '\'' +
                ", testId='" + testId + '\'' +
                ", difficulty='" + difficulty + '\'' +
                ", valid=" + isValid() +
                ", weightedScore=" + getWeightedScore() +
                ", responseTimeMs=" + getResponseTimeMs() +
                ", errorType='" + getErrorType() + '\'' +
                '}';
    }
}