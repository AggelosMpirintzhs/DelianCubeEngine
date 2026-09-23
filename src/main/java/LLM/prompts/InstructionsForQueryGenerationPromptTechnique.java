package LLM.prompts;

public class InstructionsForQueryGenerationPromptTechnique
        implements PromptTechnique {

    @Override
    public String getName() {
        return PromptTechniqueFactory.INSTRUCTIONS_FOR_QUERY_GENERATION;
    }

    @Override
    public String getContent() {
        return instructionsForQueryGeneration();
    }

    private String instructionsForQueryGeneration() {

        StringBuilder instructions = new StringBuilder();

        instructions.append("Instructions for query generation:\n");
        instructions.append("\n");

        instructions.append("Follow these guidelines to map the natural language question to the provided cube schema.\n");
        instructions.append("\n");


        /*
         * Measure selection
         */
        instructions.append("Measure selection:\n");
        instructions.append("Select the measure requested by the question exactly as it appears under \"Measures:\" in the cube schema.\n");
        instructions.append("Do not invent, rename, shorten, or paraphrase measure names.\n");
        instructions.append("\n");


        /*
         * Aggregation
         */
        instructions.append("Aggregation:\n");
        instructions.append("Use exactly one of the following aggregation functions:\n");
        instructions.append("- Sum for the total or sum of a numeric measure.\n");
        instructions.append("- Avg for average or mean.\n");
        instructions.append("- Count when the question asks for the number of records or entities.\n");
        instructions.append("- Min for minimum or lowest.\n");
        instructions.append("- Max for maximum or highest.\n");
        instructions.append("\n");

        instructions.append("When the question contains expressions such as \"how many\", determine the aggregation from the context:\n");
        instructions.append("- use Count when counting records or entities;\n");
        instructions.append("- use Sum when asking for the total amount of an explicit numeric quantity measure.\n");
        instructions.append("\n");


        /*
         * Gamma selection
         */
        instructions.append("Gamma selection:\n");
        instructions.append("Identify the concepts that describe how the result should be grouped.\n");
        instructions.append("Expressions such as \"by\", \"per\", \"for each\", \"grouped by\", or \"broken down by\" often indicate grouping concepts.\n");
        instructions.append("These concepts determine gamma.\n");
        instructions.append("\n");

        instructions.append("For each grouping concept:\n");
        instructions.append("- Treat the natural language concept as a semantic description, not as a schema field name.\n");
        instructions.append("- Find the dimension that best matches the meaning of the concept.\n");
        instructions.append("- Find the hierarchy level whose meaning and granularity best match the requested concept.\n");
        instructions.append("- Use the field shown after \"->\" for that level.\n");
        instructions.append("- Copy the selected field exactly as it appears in the cube schema.\n");
        instructions.append("\n");


        /*
         * Sigma selection
         */
        instructions.append("Sigma selection:\n");
        instructions.append("Identify restrictions or conditions expressed in the question.\n");
        instructions.append("These concepts determine sigma.\n");
        instructions.append("\n");

        instructions.append("For each filtering concept:\n");
        instructions.append("- Find the dimension that best matches the restricted concept.\n");
        instructions.append("- Find the hierarchy level whose meaning and granularity best match the restriction.\n");
        instructions.append("- Use the appropriate field shown after \"->\" for that level.\n");
        instructions.append("- Copy the selected field exactly as it appears in the cube schema.\n");
        instructions.append("\n");


        /*
         * Granularity
         */
        instructions.append("Granularity:\n");
        instructions.append("Match the requested granularity as closely as possible.\n");
        instructions.append("Do not replace the requested concept with a more detailed or more general hierarchy level.\n");
        instructions.append("For example, a request by month should use a field associated with a month-level concept rather than a day-level or year-level concept.\n");
        instructions.append("\n");


        /*
         * Multiple fields at the same level
         */
        instructions.append("Multiple fields at the same level:\n");
        instructions.append("A hierarchy level may contain more than one field.\n");
        instructions.append("If multiple fields are available for the same level, select the field whose meaning best matches the concept expressed in the question.\n");
        instructions.append("\n");


        /*
         * Similar concepts across dimensions
         */
        instructions.append("Similar concepts across dimensions:\n");
        instructions.append("The same or a similar concept may appear in more than one dimension.\n");
        instructions.append("Use the context of the question to determine which dimension is intended.\n");
        instructions.append("Prefer the dimension associated with the entity explicitly referred to in the question.\n");
        instructions.append("\n");


        /*
         * Filter values
         */
        instructions.append("Filter values:\n");
        instructions.append("Preserve explicit values from the user's question exactly whenever possible.\n");
        instructions.append("This includes labels, codes, abbreviations, numbers, dates, and compound period values.\n");
        instructions.append("Do not convert a value to another representation unless the required mapping is explicitly available in the provided information.\n");
        instructions.append("\n");


        /*
         * Schema constraints
         */
        instructions.append("Schema constraints:\n");
        instructions.append("Use only cube elements that are present in the provided cube schema.\n");
        instructions.append("Never invent dimensions, hierarchy levels, fields, or measures.\n");
        instructions.append("If a grouping or filtering concept cannot be matched to a valid field in the cube schema, do not invent a field to satisfy the request.\n");
        instructions.append("\n");


        /*
         * Final self-check
         */
        instructions.append("Final self-check:\n");
        instructions.append("Before returning the final answer, verify that the selected query components follow all of the instructions above.\n");
        instructions.append("Check that:\n");
        instructions.append("- the selected measure exists in the provided cube schema;\n");
        instructions.append("- the aggregation function matches the user's request;\n");
        instructions.append("- every gamma field exists in the provided cube schema and matches the requested grouping concept and granularity;\n");
        instructions.append("- every sigma field exists in the provided cube schema and matches the corresponding filtering concept;\n");
        instructions.append("- explicit filter values from the user's question have been preserved correctly;\n");
        instructions.append("- no cube element has been invented or renamed.\n");
        instructions.append("\n");

        instructions.append("If any check fails, revise the selected query components before returning the final answer.\n");
        instructions.append("Do not output the self-check. Output only the final answer.\n");

        return instructions.toString();
    }
}