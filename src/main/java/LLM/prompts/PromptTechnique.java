package LLM.prompts;

import LLM.schema.CubeSchema;

public interface PromptTechnique {

    String getName();

    String buildPrompt(CubeSchema cubeSchema, String naturalLanguageQuestion);
}