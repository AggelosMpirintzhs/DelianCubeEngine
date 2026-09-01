package LLM.prompts;

import LLM.schema.CubeSchema;

public class StepByStepMethodologyPromptTechnique implements PromptTechnique {

    @Override
    public String getName() {
        return PromptTechniqueFactory.STEP_BY_STEP_METHODOLOGY;
    }

    @Override
    public String buildPrompt(CubeSchema cubeSchema, String naturalLanguageQuestion) {
        CubeSchemaPromptFormatterCompact formatter = new CubeSchemaPromptFormatterCompact();
        String formattedSchema = formatter.format(cubeSchema);

        StringBuilder prompt = new StringBuilder();

        prompt.append("You translate a natural language question into CineCubes query components.\n");
        prompt.append("Use only the cube schema given below.\n");
        prompt.append("\n");

        prompt.append("Task:\n");
        prompt.append("Convert the natural language question into the required six-line CineCubes query format.\n");
        prompt.append("Follow the methodology below before producing the final answer.\n");
        prompt.append("Do not output the methodology, reasoning, notes, or explanations.\n");
        prompt.append("Output only the final six lines.\n");
        prompt.append("\n");

        prompt.append("Basic schema interpretation:\n");
        prompt.append("- The cube name must be copied exactly from the schema.\n");
        prompt.append("- Measures are the numeric values that can be aggregated.\n");
        prompt.append("- Allowed fields are the only fields that can appear in gamma or sigma.\n");
        prompt.append("- Allowed fields have the form dimensionName.attributeName.\n");
        prompt.append("- Dimensions may be hierarchical, so choose the field whose level matches the requested granularity.\n");
        prompt.append("- Natural language concepts are not field names by themselves. They must be mapped to exact allowed fields.\n");
        prompt.append("\n");

        prompt.append("Methodology:\n");
        prompt.append("1. Identify the cube from the schema. Use exactly that cube name.\n");
        prompt.append("2. Identify the requested measure from the question.\n");
        prompt.append("3. Match the requested measure to one of the schema measures. Do not invent a measure.\n");
        prompt.append("4. Identify the requested aggregation.\n");
        prompt.append("5. Use Sum for total, sum, overall amounts, or how many units.\n");
        prompt.append("6. Use Avg for average or mean.\n");
        prompt.append("7. Use Count only when the question asks for a count of records or entities.\n");
        prompt.append("8. Use Min for minimum or lowest.\n");
        prompt.append("9. Use Max for maximum or highest.\n");
        prompt.append("10. Identify the words that describe how the result should be broken down, such as by, per, for each, grouped by, or broken down by.\n");
        prompt.append("11. These breakdown concepts become gamma fields.\n");
        prompt.append("12. For each gamma concept, find an allowed field from the schema that matches both the meaning and the requested hierarchy level.\n");
        prompt.append("13. If a candidate gamma field does not appear exactly in the Allowed fields of the current schema, do not use it.\n");
        prompt.append("14. If the question asks for a detailed concept, choose a detailed-level field.\n");
        prompt.append("15. If the question asks for a general concept, choose a general-level field.\n");
        prompt.append("16. For example, if the question asks by month, choose a month-level field, not a day-level or year-level field.\n");
        prompt.append("17. For example, if the question asks by country, choose a country-level field, not a city-level field.\n");
        prompt.append("18. Identify restrictions in the question, such as time, place, category, product, customer, store, promotion, status, or media.\n");
        prompt.append("19. These restrictions become sigma conditions.\n");
        prompt.append("20. For each sigma concept, choose an allowed field from the schema that matches the restricted concept.\n");
        prompt.append("21. If a candidate sigma field does not appear exactly in the Allowed fields of the current schema, do not use it.\n");
        prompt.append("22. A sigma condition must use the form dimensionName.attributeName='value'.\n");
        prompt.append("23. Preserve explicit filter values from the question when possible, including codes, labels, abbreviations, and compound period values.\n");
        prompt.append("24. If the same concept can refer to multiple entities, use the entity explicitly mentioned in the question.\n");
        prompt.append("25. For example, a location can belong to a customer, a store, a supplier, or another entity. Choose the dimension that matches the entity in the question.\n");
        prompt.append("26. Customer-related restrictions should use customer-related fields when such fields exist.\n");
        prompt.append("27. Store-related restrictions should use store-related fields when such fields exist.\n");
        prompt.append("28. Product-related restrictions should use product-related fields when such fields exist.\n");
        prompt.append("29. Promotion-related restrictions should use promotion-related fields when such fields exist.\n");
        prompt.append("30. Time-related restrictions should use date or time fields when such fields exist.\n");
        prompt.append("31. Never create a new field name by paraphrasing, shortening, or renaming schema fields.\n");
        prompt.append("32. If a requested concept cannot be matched to any allowed field, do not invent a field.\n");
        prompt.append("33. Leave the corresponding gamma or sigma part empty if no valid field can be found.\n");
        prompt.append("34. Build gamma as a comma-separated list of allowed fields.\n");
        prompt.append("35. Build sigma as a comma-separated list of conditions in the form dimensionName.attributeName='value'.\n");
        prompt.append("36. If there is no grouping concept, leave gamma empty.\n");
        prompt.append("37. If there is no filtering restriction, leave sigma empty.\n");
        prompt.append("38. Before producing the final answer, briefly verify that the selected cube, measure, gamma fields, and sigma fields exist in the current schema.\n");
        prompt.append("39. Do not output this verification.\n");
        prompt.append("40. Produce only the final six-line answer.\n");
        prompt.append("\n");

        prompt.append("Output constraints:\n");
        prompt.append("- Do not output SQL.\n");
        prompt.append("- Do not output JSON.\n");
        prompt.append("- Do not output markdown.\n");
        prompt.append("- Do not output explanations.\n");
        prompt.append("- Do not output the methodology steps.\n");
        prompt.append("- queryName may be arbitrary, but it must be a single line.\n");
        prompt.append("- The final answer must contain exactly the six required fields.\n");
        prompt.append("\n");

        prompt.append("Output format:\n");
        prompt.append("cubeName: \n");
        prompt.append("queryName: \n");
        prompt.append("aggregateFunction: \n");
        prompt.append("measure: \n");
        prompt.append("gamma: \n");
        prompt.append("sigma: \n");
        prompt.append("\n");

        prompt.append("Cube schema:\n");
        prompt.append(formattedSchema);
        prompt.append("\n\n");

        prompt.append("Natural language question:\n");
        prompt.append(naturalLanguageQuestion == null ? "" : naturalLanguageQuestion.trim());
        prompt.append("\n\n");

        prompt.append("Final answer:\n");

        return prompt.toString();
    }
}