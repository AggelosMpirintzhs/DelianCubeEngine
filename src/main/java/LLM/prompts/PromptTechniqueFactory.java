package LLM.prompts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PromptTechniqueFactory {

    public static final String RULES_TEMPLATE = "rules_template";
    public static final String RULES_GENERIC_EXAMPLES = "rules_generic_examples";
    public static final String STEP_BY_STEP_METHODOLOGY = "step_by_step_methodology";
    public static final String HYBRID_SELF_CHECK = "hybrid_self_check";

    private PromptTechniqueFactory() {
        // Utility class
    }

    public static PromptTechnique createByName(String promptTechniqueName) {
        String normalizedName = normalizeName(promptTechniqueName);

        if (RULES_TEMPLATE.equals(normalizedName)) {
            return new RulesTemplatePromptTechnique();
        }

        if (RULES_GENERIC_EXAMPLES.equals(normalizedName)) {
            return new RulesGenericExamplesPromptTechnique();
        }

        if (STEP_BY_STEP_METHODOLOGY.equals(normalizedName)) {
            return new StepByStepMethodologyPromptTechnique();
        }

        if (HYBRID_SELF_CHECK.equals(normalizedName)) {
            return new HybridSelfCheckPromptTechnique();
        }

        throw new IllegalArgumentException(
                "Unknown prompt technique: " + promptTechniqueName
        );
    }

    public static List<PromptTechnique> getAllPromptTechniques() {
        List<PromptTechnique> techniques = new ArrayList<PromptTechnique>();

        techniques.add(new RulesTemplatePromptTechnique());
        techniques.add(new RulesGenericExamplesPromptTechnique());
        techniques.add(new StepByStepMethodologyPromptTechnique());
        techniques.add(new HybridSelfCheckPromptTechnique());

        return Collections.unmodifiableList(techniques);
    }

    public static List<String> getAllPromptTechniqueNames() {
        List<String> names = new ArrayList<String>();

        names.add(RULES_TEMPLATE);
        names.add(RULES_GENERIC_EXAMPLES);
        names.add(STEP_BY_STEP_METHODOLOGY);
        names.add(HYBRID_SELF_CHECK);

        return Collections.unmodifiableList(names);
    }

    private static String normalizeName(String name) {
        if (name == null) {
            return "";
        }

        return name.trim().toLowerCase();
    }
}