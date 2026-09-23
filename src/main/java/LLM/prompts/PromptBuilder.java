package LLM.prompts;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import LLM.schema.CubeSchema;

public class PromptBuilder {

    private final CubeSchemaPromptFormatterCompact cubeSchemaFormatter;
    private final JsonOutputContract outputContract;

    public PromptBuilder() {
        this(
                new CubeSchemaPromptFormatterCompact(),
                new JsonOutputContract()
        );
    }

    public PromptBuilder(
            CubeSchemaPromptFormatterCompact cubeSchemaFormatter,
            JsonOutputContract outputContract
    ) {
        if (cubeSchemaFormatter == null) {
            throw new IllegalArgumentException(
                    "CubeSchemaPromptFormatterCompact cannot be null."
            );
        }

        if (outputContract == null) {
            throw new IllegalArgumentException(
                    "JsonOutputContract cannot be null."
            );
        }

        this.cubeSchemaFormatter = cubeSchemaFormatter;
        this.outputContract = outputContract;
    }

    /*
     * Builds the final prompt using any selected combination
     * of prompt techniques.
     */
    public String buildPrompt(
            CubeSchema cubeSchema,
            String naturalLanguageQuestion,
            List<PromptTechnique> promptTechniques
    ) {
        validateInputs(
                cubeSchema,
                naturalLanguageQuestion,
                promptTechniques
        );

        StringBuilder prompt = new StringBuilder();

        /*
         * 1. Add the selected experimental techniques.
         *
         * Examples:
         * - Instructions only
         * - Prescription only
         * - Examples only
         * - Instructions + Examples
         * - Instructions + Prescription + Examples
         */
        appendPromptTechniques(
                prompt,
                promptTechniques
        );

        /*
         * 2. Add the cube schema.
         * This is mandatory in every experiment.
         */
        appendCubeSchema(
                prompt,
                cubeSchema
        );

        /*
         * 3. Add the user's natural language question.
         * This is mandatory in every experiment.
         */
        appendUserQuestion(
                prompt,
                naturalLanguageQuestion
        );

        /*
         * 4. Add the mandatory output contract.
         *
         * It is placed near the end so that the expected
         * response format is explicit immediately before
         * the model generates the answer.
         */
        appendOutputContract(prompt);

        /*
         * 5. Final marker.
         */
        appendFinalAnswerMarker(prompt);

        return prompt.toString();
    }

    /*
     * Convenience method for the baseline experiment:
     * Cube schema + user question + output contract,
     * without any optional prompt technique.
     */
    public String buildPrompt(
            CubeSchema cubeSchema,
            String naturalLanguageQuestion
    ) {
        return buildPrompt(
                cubeSchema,
                naturalLanguageQuestion,
                java.util.Collections.<PromptTechnique>emptyList()
        );
    }

    private void appendPromptTechniques(
            StringBuilder prompt,
            List<PromptTechnique> promptTechniques
    ) {
        for (PromptTechnique technique : promptTechniques) {
            appendSection(
                    prompt,
                    technique.getContent()
            );
        }
    }

    private void appendCubeSchema(
            StringBuilder prompt,
            CubeSchema cubeSchema
    ) {
        prompt.append("Cube schema:\n");
        prompt.append("\n");

        prompt.append(
                cubeSchemaFormatter.format(cubeSchema)
        );

        ensureSectionEnding(prompt);
    }

    private void appendUserQuestion(
            StringBuilder prompt,
            String naturalLanguageQuestion
    ) {
        prompt.append("User question:\n");
        prompt.append("\n");

        prompt.append(
                naturalLanguageQuestion.trim()
        );

        prompt.append("\n\n");
    }

    private void appendOutputContract(
            StringBuilder prompt
    ) {
        appendSection(
                prompt,
                outputContract.getContent()
        );
    }

    private void appendFinalAnswerMarker(
            StringBuilder prompt
    ) {
        prompt.append("Final answer:\n");
    }

    private void appendSection(
            StringBuilder prompt,
            String content
    ) {
        if (content == null || content.trim().isEmpty()) {
            return;
        }

        prompt.append(content.trim());
        prompt.append("\n\n");
    }

    private void ensureSectionEnding(
            StringBuilder prompt
    ) {
        int length = prompt.length();

        if (length == 0) {
            return;
        }

        if (length >= 2
                && prompt.charAt(length - 1) == '\n'
                && prompt.charAt(length - 2) == '\n') {
            return;
        }

        if (prompt.charAt(length - 1) == '\n') {
            prompt.append("\n");
        } else {
            prompt.append("\n\n");
        }
    }

    private void validateInputs(
            CubeSchema cubeSchema,
            String naturalLanguageQuestion,
            List<PromptTechnique> promptTechniques
    ) {
        if (cubeSchema == null) {
            throw new IllegalArgumentException(
                    "CubeSchema cannot be null."
            );
        }

        if (naturalLanguageQuestion == null
                || naturalLanguageQuestion.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Natural language question cannot be null or empty."
            );
        }

        if (promptTechniques == null) {
            throw new IllegalArgumentException(
                    "Prompt techniques list cannot be null."
            );
        }

        validatePromptTechniques(promptTechniques);
    }

    private void validatePromptTechniques(
            List<PromptTechnique> promptTechniques
    ) {
        Set<String> techniqueNames =
                new HashSet<String>();

        for (PromptTechnique technique : promptTechniques) {

            if (technique == null) {
                throw new IllegalArgumentException(
                        "Prompt technique cannot be null."
                );
            }

            String techniqueName =
                    technique.getName();

            if (techniqueName == null
                    || techniqueName.trim().isEmpty()) {
                throw new IllegalArgumentException(
                        "Prompt technique name cannot be null or empty."
                );
            }

            if (!techniqueNames.add(techniqueName)) {
                throw new IllegalArgumentException(
                        "Duplicate prompt technique: "
                                + techniqueName
                );
            }
        }
    }
}