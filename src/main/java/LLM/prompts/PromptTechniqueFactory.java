package LLM.prompts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PromptTechniqueFactory {

    public static final String INSTRUCTIONS_FOR_QUERY_GENERATION =
            "instructions_for_query_generation";

    public static final String PRESCRIPTION_OF_RESULT =
            "prescription_of_result";

    public static final String EXAMPLES =
            "examples";

    private PromptTechniqueFactory() {
        // Utility class
    }

    /*
     * Creates one prompt technique based on its name.
     */
    public static PromptTechnique createByName(String promptTechniqueName) {

        String normalizedName = normalizeName(promptTechniqueName);

        if (INSTRUCTIONS_FOR_QUERY_GENERATION.equals(normalizedName)) {
            return new InstructionsForQueryGenerationPromptTechnique();
        }

        if (PRESCRIPTION_OF_RESULT.equals(normalizedName)) {
            return new PrescriptionOfResultPromptTechnique();
        }

        if (EXAMPLES.equals(normalizedName)) {
            return new ExamplesPromptTechnique();
        }

        throw new IllegalArgumentException(
                "Unknown prompt technique: " + promptTechniqueName
        );
    }

    /*
     * Returns all currently available experimental techniques.
     *
     * These are individual techniques, not combinations.
     */
    public static List<PromptTechnique> getAllPromptTechniques() {

        List<PromptTechnique> techniques =
                new ArrayList<PromptTechnique>();

        techniques.add(
                new InstructionsForQueryGenerationPromptTechnique()
        );

        techniques.add(
                new PrescriptionOfResultPromptTechnique()
        );

        techniques.add(
                new ExamplesPromptTechnique()
        );

        return Collections.unmodifiableList(techniques);
    }

    /*
     * Returns the names of all available techniques.
     */
    public static List<String> getAllPromptTechniqueNames() {

        List<String> names =
                new ArrayList<String>();

        names.add(INSTRUCTIONS_FOR_QUERY_GENERATION);
        names.add(PRESCRIPTION_OF_RESULT);
        names.add(EXAMPLES);

        return Collections.unmodifiableList(names);
    }

    private static String normalizeName(String name) {

        if (name == null) {
            return "";
        }

        return name.trim().toLowerCase();
    }
}