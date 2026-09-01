package LLM.experiments.runner;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.List;

import LLM.experiments.CubeQueryValidator;
import LLM.experiments.ExpectedCubeQuery;
import LLM.experiments.LLMExperimentCase;
import LLM.experiments.ValidationResult;

import LLM.prompts.PromptTechnique;

import LLM.experiments.results.ExperimentResult;
import LLM.experiments.results.ExperimentResultsWriter;
import LLM.experiments.results.ValidationLogWriter;

import LLM.schema.CubeSchema;

public class LLMExperimentRunner {

    private final OllamaLLMClient llmClient;

    private int runCounter;

    public LLMExperimentRunner(OllamaLLMClient llmClient) {
        this.llmClient = llmClient;
        this.runCounter = 0;
    }

    public void runExperiments(
            CubeSchema cubeSchema,
            List<LLMModelConfig> modelConfigs,
            List<PromptTechnique> promptTechniques,
            List<LLMExperimentCase> experimentCases,
            ExperimentResultsWriter resultsWriter,
            ValidationLogWriter validationLogWriter
    ) throws Exception {

        if (cubeSchema == null) {
            throw new IllegalArgumentException("cubeSchema cannot be null.");
        }

        if (modelConfigs == null || modelConfigs.isEmpty()) {
            throw new IllegalArgumentException("modelConfigs cannot be null or empty.");
        }

        if (promptTechniques == null || promptTechniques.isEmpty()) {
            throw new IllegalArgumentException("promptTechniques cannot be null or empty.");
        }

        if (experimentCases == null || experimentCases.isEmpty()) {
            throw new IllegalArgumentException("experimentCases cannot be null or empty.");
        }

        if (resultsWriter == null) {
            throw new IllegalArgumentException("resultsWriter cannot be null.");
        }

        if (validationLogWriter == null) {
            throw new IllegalArgumentException("validationLogWriter cannot be null.");
        }

        int totalRuns = modelConfigs.size() * promptTechniques.size() * experimentCases.size();

        System.out.println("Starting LLM experiments.");
        System.out.println("Models: " + modelConfigs.size());
        System.out.println("Prompt techniques: " + promptTechniques.size());
        System.out.println("Test cases: " + experimentCases.size());
        System.out.println("Total runs: " + totalRuns);
        System.out.println();

        for (LLMModelConfig modelConfig : modelConfigs) {

            warmUpModel(modelConfig);

            for (PromptTechnique promptTechnique : promptTechniques) {
                for (LLMExperimentCase experimentCase : experimentCases) {
                    ExperimentResult result = runSingleExperiment(
                            cubeSchema,
                            modelConfig,
                            promptTechnique,
                            experimentCase
                    );

                    resultsWriter.writeResult(result);
                    validationLogWriter.writeResult(result);

                    printRunSummary(result, totalRuns);
                }
            }
        }

        System.out.println();
        System.out.println("LLM experiments completed.");
        System.out.println("Completed runs: " + runCounter + "/" + totalRuns);
    }

    private ExperimentResult runSingleExperiment(
            CubeSchema cubeSchema,
            LLMModelConfig modelConfig,
            PromptTechnique promptTechnique,
            LLMExperimentCase experimentCase
    ) {
        String runId = nextRunId();
        String timestamp = currentTimestamp();

        String prompt = "";
        PromptSizeStats promptSizeStats = PromptSizeStats.fromPrompt("");

        try {
            prompt = promptTechnique.buildPrompt(
                    cubeSchema,
                    experimentCase.getQuestion()
            );

            promptSizeStats = PromptSizeStats.fromPrompt(prompt);

            LLMCallResult llmCallResult = llmClient.generate(
                    modelConfig,
                    prompt
            );

            String actualAnswer = llmCallResult.getAnswer();

            ValidationResult validationResult = CubeQueryValidator.validateIgnoringSigmaValues(
                    actualAnswer,
                    experimentCase.getExpectedQuery(),
                    cubeSchema
            );

            return ExperimentResult.from(
                    runId,
                    timestamp,
                    modelConfig,
                    promptTechnique.getName(),
                    experimentCase,
                    actualAnswer,
                    validationResult,
                    promptSizeStats,
                    llmCallResult
            );

        } catch (Exception e) {
            LLMCallResult failedCallResult = LLMCallResult.failure(
                    0L,
                    "RUNNER_EXCEPTION",
                    e.getClass().getSimpleName() + ": " + safe(e.getMessage()),
                    ""
            );

            ExpectedCubeQuery expectedQuery = experimentCase == null
                    ? null
                    : experimentCase.getExpectedQuery();

            ValidationResult validationResult;

            if (expectedQuery == null) {
                validationResult = new ValidationResult(
                        false,
                        null
                );
            } else {
                validationResult = CubeQueryValidator.validate(
                        "",
                        expectedQuery,
                        cubeSchema
                );
            }

            return ExperimentResult.from(
                    runId,
                    timestamp,
                    modelConfig,
                    promptTechnique == null ? "" : promptTechnique.getName(),
                    experimentCase,
                    "",
                    validationResult,
                    promptSizeStats,
                    failedCallResult
            );
        }
    }

    private void warmUpModel(LLMModelConfig modelConfig) {
        if (modelConfig == null) {
            return;
        }

        System.out.println("Warming up model: " + modelConfig.getModelName());

        LLMModelConfig warmUpConfig = new LLMModelConfig(
                modelConfig.getModelName(),
                1024,
                8,
                0.0,
                modelConfig.getTopK(),
                modelConfig.getTopP(),
                modelConfig.getKeepAlive(),
                modelConfig.isStreamEnabled()
        );

        String warmUpPrompt =
                "Return exactly this word and nothing else:\nOK";

        LLMCallResult warmUpResult = llmClient.generate(
                warmUpConfig,
                warmUpPrompt
        );

        if (warmUpResult.isSuccess()) {
            System.out.println(
                    "Warm-up completed for "
                            + modelConfig.getModelName()
                            + " | timeMs="
                            + warmUpResult.getResponseTimeMs()
            );
        } else {
            System.out.println(
                    "Warm-up failed for "
                            + modelConfig.getModelName()
                            + " | error="
                            + warmUpResult.getErrorType()
                            + " | message="
                            + warmUpResult.getErrorMessage()
            );
            System.out.println("Continuing with experiments anyway.");
        }

        System.out.println();
    }

    private void printRunSummary(ExperimentResult result, int totalRuns) {
        System.out.println(
                "[" + runCounter + "/" + totalRuns + "] "
                        + "llm=" + result.getLlmName()
                        + " | technique=" + result.getPromptTechniqueName()
                        + " | test=" + result.getTestId()
                        + " | score=" + result.getWeightedScore()
                        + " | valid=" + result.isValid()
                        + " | timeMs=" + result.getResponseTimeMs()
                        + " | error=" + result.getErrorType()
        );
    }

    private String nextRunId() {
        runCounter++;

        if (runCounter < 10) {
            return "run_000" + runCounter;
        }

        if (runCounter < 100) {
            return "run_00" + runCounter;
        }

        if (runCounter < 1000) {
            return "run_0" + runCounter;
        }

        return "run_" + runCounter;
    }

    private String currentTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.now().format(formatter);
    }

    private String safe(String value) {
        if (value == null) {
            return "";
        }

        return value.trim();
    }
}