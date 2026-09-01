package LLM.prompts;

import LLM.schema.CubeSchema;

public class RulesTemplatePromptTechnique implements PromptTechnique {

    @Override
    public String getName() {
        return PromptTechniqueFactory.RULES_TEMPLATE;
    }

    @Override
    public String buildPrompt(CubeSchema cubeSchema, String naturalLanguageQuestion) {
        CubeSchemaPromptFormatterCompact formatter = new CubeSchemaPromptFormatterCompact();
        String formattedSchema = formatter.format(cubeSchema);

        StringBuilder prompt = new StringBuilder();

        prompt.append("You translate a natural language question into CineCubes query components.\n");
        prompt.append("Use only the cube schema given below.\n");
        prompt.append("\n");

        prompt.append("Schema explanation:\n");
        prompt.append("- Cube is the only cube that can be queried.\n");
        prompt.append("- Measures are the numeric values that can be aggregated.\n");
        prompt.append("- Dimensions describe analysis perspectives, such as time, location, product, customer, store, or promotion.\n");
        prompt.append("- Dimensions may be hierarchical. A hierarchy contains levels from more detailed to more general concepts.\n");
        prompt.append("- For example, a time dimension may contain day, month, quarter, and year levels.\n");
        prompt.append("- For example, a location dimension may contain city, state, country, and all-location levels.\n");
        prompt.append("- Allowed fields are the only dimension attributes that can be used in gamma or sigma.\n");
        prompt.append("- Each allowed field has the form dimensionName.attributeName.\n");
        prompt.append("- When choosing a gamma or sigma field, select an allowed field that belongs to the dimension and hierarchy level that best matches the meaning and granularity requested by the question.\n");
        prompt.append("\n");

        prompt.append("Main goal:\n");
        prompt.append("Given a user's question in natural language, identify the cube, aggregation, measure, grouping fields, and filter conditions needed to build a CineCubes query.\n");
        prompt.append("\n");

        prompt.append("Important rules:\n");
        prompt.append("1. Return only the six required fields, in the exact output format.\n");
        prompt.append("2. cubeName must use the cube name shown in the schema.\n");
        prompt.append("3. aggregateFunction must be one of: Sum, Avg, Count, Min, Max.\n");
        prompt.append("4. measure must be one of the measures shown in the schema.\n");
        prompt.append("5. Do not invent cube names, measures, dimensions, attributes, or values.\n");
        prompt.append("6. gamma is the analysis/grouping part of the query.\n");
        prompt.append("7. gamma contains the field or fields by which the result should be grouped.\n");
        prompt.append("8. Every gamma item must be copied exactly from the Allowed fields in the schema.\n");
        prompt.append("9. sigma is the filtering part of the query.\n");
        prompt.append("10. sigma contains filter conditions on allowed fields.\n");
        prompt.append("11. Every sigma field must be copied exactly from the Allowed fields in the schema.\n");
        prompt.append("12. A gamma item must look like dimensionName.attributeName.\n");
        prompt.append("13. A sigma condition must look like dimensionName.attributeName='value'.\n");
        prompt.append("14. If there is no gamma, leave gamma empty.\n");
        prompt.append("15. If there is no sigma, leave sigma empty.\n");
        prompt.append("16. Use comma-separated values when gamma or sigma contains multiple items.\n");
        prompt.append("17. The order of gamma and sigma items is not important, but the fields and conditions must be correct.\n");
        prompt.append("18. Dimensions may contain multiple hierarchy levels, from detailed levels to more general levels.\n");
        prompt.append("19. Choose the field whose hierarchy level matches the requested granularity in the question.\n");
        prompt.append("20. If the question asks for results by a detailed concept, choose a detailed-level field.\n");
        prompt.append("21. If the question asks for results by a more general concept, choose a higher-level field.\n");
        prompt.append("22. Do not use a more detailed field when the question asks for a general level.\n");
        prompt.append("23. Do not use a more general field when the question asks for a detailed level.\n");
        prompt.append("24. If several fields seem related, prefer the one whose dimension entity and hierarchy level both match the question.\n");
        prompt.append("25. For example, if a question asks by month, choose a month-level field, not a day-level or year-level field.\n");
        prompt.append("26. For example, if a question asks by country, choose a country-level field, not a city-level field.\n");
        prompt.append("27. Match each requested concept to the most appropriate allowed field in the current schema.\n");
        prompt.append("28. If a concept appears in multiple dimensions, choose the dimension whose entity is mentioned in the question.\n");
        prompt.append("29. Customer-related concepts should use customer-related fields when such fields exist.\n");
        prompt.append("30. Store-related concepts should use store-related fields when such fields exist.\n");
        prompt.append("31. Product-related concepts should use product-related fields when such fields exist.\n");
        prompt.append("32. Promotion-related concepts should use promotion-related fields when such fields exist.\n");
        prompt.append("33. Time-related concepts should use date or time fields when such fields exist.\n");
        prompt.append("34. Preserve explicit filter values from the user question exactly when possible, including codes, labels, abbreviations, and compound period values.\n");
        prompt.append("35. Do not output SQL.\n");
        prompt.append("36. Do not output explanations.\n");
        prompt.append("\n");

        prompt.append("Aggregate mapping hints:\n");
        prompt.append("- total, sum, overall, how many usually mean aggregateFunction: Sum.\n");
        prompt.append("- average or mean usually means aggregateFunction: Avg.\n");
        prompt.append("- count or number of records usually means aggregateFunction: Count.\n");
        prompt.append("- minimum or lowest usually means aggregateFunction: Min.\n");
        prompt.append("- maximum or highest usually means aggregateFunction: Max.\n");
        prompt.append("\n");

        prompt.append("Output format:\n");
        prompt.append("Return exactly these six lines:\n");
        prompt.append("\n");
        prompt.append("cubeName: \n");
        prompt.append("queryName: \n");
        prompt.append("aggregateFunction: \n");
        prompt.append("measure: \n");
        prompt.append("gamma: \n");
        prompt.append("sigma: \n");
        prompt.append("\n");

        prompt.append("Do not use JSON, markdown, code blocks, semicolons, bullets, numbering, or explanations in the final answer.\n");
        prompt.append("The final answer must contain only the six output lines.\n");
        prompt.append("\n");

        prompt.append("Cube schema:\n");
        prompt.append(formattedSchema);
        prompt.append("\n\n");

        prompt.append("User's Natural language question:\n");
        prompt.append(naturalLanguageQuestion == null ? "" : naturalLanguageQuestion.trim());
        prompt.append("\n\n");

        prompt.append("Final answer:\n");

        return prompt.toString();
    }
}