package LLM.prompts;

import LLM.schema.CubeSchema;

public class HybridSelfCheckPromptTechnique implements PromptTechnique {

    @Override
    public String getName() {
        return PromptTechniqueFactory.HYBRID_SELF_CHECK;
    }

    @Override
    public String buildPrompt(CubeSchema cubeSchema, String naturalLanguageQuestion) {
        CubeSchemaPromptFormatterCompact formatter = new CubeSchemaPromptFormatterCompact();
        String formattedSchema = formatter.format(cubeSchema);

        StringBuilder prompt = new StringBuilder();

        prompt.append("You translate a natural language question into CineCubes query components.\n");
        prompt.append("Use only the current cube schema given below.\n");
        prompt.append("\n");

        prompt.append("Task:\n");
        prompt.append("Convert the natural language question into the required six-line CineCubes query format.\n");
        prompt.append("You must use the rules, methodology, and self-check instructions below.\n");
        prompt.append("Do not output reasoning, notes, explanations, SQL, JSON, or markdown.\n");
        prompt.append("Output only the final six required lines.\n");
        prompt.append("\n");

        prompt.append("Schema interpretation:\n");
        prompt.append("- The cube name must be copied exactly from the current schema.\n");
        prompt.append("- Measures are the numeric values that can be aggregated.\n");
        prompt.append("- Allowed fields are the only fields that can appear in gamma or sigma.\n");
        prompt.append("- Allowed fields have the form dimensionName.attributeName.\n");
        prompt.append("- Dimensions may be hierarchical. Choose the field whose level matches the requested granularity.\n");
        prompt.append("- Natural language concepts are not field names by themselves. They must be mapped to exact allowed fields.\n");
        prompt.append("\n");

        prompt.append("Core rules:\n");
        prompt.append("1. cubeName must be copied exactly from the current schema.\n");
        prompt.append("2. aggregateFunction must be one of: Sum, Avg, Count, Min, Max.\n");
        prompt.append("3. measure must be one of the measures shown in the current schema.\n");
        prompt.append("4. gamma contains grouping fields.\n");
        prompt.append("5. sigma contains filtering conditions.\n");
        prompt.append("6. Every gamma field must be copied exactly from the Allowed fields of the current schema.\n");
        prompt.append("7. Every sigma field must be copied exactly from the Allowed fields of the current schema.\n");
        prompt.append("8. A gamma item must look like dimensionName.attributeName.\n");
        prompt.append("9. A sigma condition must look like dimensionName.attributeName='value'.\n");
        prompt.append("10. If there is no grouping, leave gamma empty.\n");
        prompt.append("11. If there is no filtering, leave sigma empty.\n");
        prompt.append("12. Do not invent, rename, shorten, or paraphrase schema fields.\n");
        prompt.append("13. queryName may be arbitrary, but it must be a single line.\n");
        prompt.append("\n");

        prompt.append("Semantic mapping rules:\n");
        prompt.append("- Words like total, sum, overall, or how many usually imply aggregateFunction: Sum.\n");
        prompt.append("- Words like average or mean usually imply aggregateFunction: Avg.\n");
        prompt.append("- Words like count or number of records usually imply aggregateFunction: Count.\n");
        prompt.append("- Words like minimum or lowest usually imply aggregateFunction: Min.\n");
        prompt.append("- Words like maximum or highest usually imply aggregateFunction: Max.\n");
        prompt.append("- Words like by, per, for each, grouped by, or broken down by usually indicate gamma fields.\n");
        prompt.append("- Restrictions such as time, place, category, product, customer, store, promotion, status, or media usually indicate sigma conditions.\n");
        prompt.append("- If a concept appears in multiple dimensions, choose the dimension whose entity is mentioned in the question.\n");
        prompt.append("- If the question mentions customers, prefer customer-related fields when available.\n");
        prompt.append("- If the question mentions stores, shops, branches, or outlets, prefer store-related fields when available.\n");
        prompt.append("- If the question mentions products, items, or goods, prefer product-related fields when available.\n");
        prompt.append("- If the question mentions promotions, campaigns, discounts, or media, prefer promotion-related fields when available.\n");
        prompt.append("- If the question mentions time periods, prefer date or time-related fields when available.\n");
        prompt.append("- If the question asks by month, choose a month-level field if one exists.\n");
        prompt.append("- If the question asks by year, choose a year-level field if one exists.\n");
        prompt.append("- If the question asks by city, choose a city-level field if one exists.\n");
        prompt.append("- If the question asks by country, choose a country-level field if one exists.\n");
        prompt.append("- Preserve explicit filter values from the question when possible, including codes, labels, abbreviations, and compound period values.\n");
        prompt.append("\n");

        prompt.append("Methodology:\n");
        prompt.append("1. Identify the cube from the current schema.\n");
        prompt.append("2. Identify the requested measure and match it to a schema measure.\n");
        prompt.append("3. Identify the requested aggregate function.\n");
        prompt.append("4. Identify grouping concepts from the question and map them to gamma fields.\n");
        prompt.append("5. Identify filtering restrictions from the question and map them to sigma conditions.\n");
        prompt.append("6. Resolve ambiguous concepts using the entity context in the question.\n");
        prompt.append("7. Use hierarchy granularity: detailed question concepts need detailed fields, general concepts need general fields.\n");
        prompt.append("8. Construct the final six-line answer.\n");
        prompt.append("\n");

        appendGenericExamples(prompt);

        prompt.append("Self-check before final answer:\n");
        prompt.append("Silently verify the following before producing the final answer:\n");
        prompt.append("- The cubeName exists in the current schema.\n");
        prompt.append("- The measure exists in the current schema.\n");
        prompt.append("- The aggregateFunction is one of: Sum, Avg, Count, Min, Max.\n");
        prompt.append("- Every gamma field appears exactly in the Allowed fields of the current schema.\n");
        prompt.append("- Every sigma field appears exactly in the Allowed fields of the current schema.\n");
        prompt.append("- No gamma or sigma field was copied from the artificial examples unless it also exists in the current schema.\n");
        prompt.append("- No invented, renamed, shortened, or paraphrased field names are used.\n");
        prompt.append("- If a concept appears in multiple dimensions, the selected dimension matches the entity context of the question.\n");
        prompt.append("- The answer contains only the six required lines.\n");
        prompt.append("If any selected gamma or sigma field does not exist in the current schema, replace it with the closest valid allowed field.\n");
        prompt.append("If no valid allowed field can be found, leave that gamma or sigma part empty instead of inventing a field.\n");
        prompt.append("Do not output the self-check.\n");
        prompt.append("\n");

        prompt.append("Current cube schema:\n");
        prompt.append(formattedSchema);
        prompt.append("\n\n");

        prompt.append("Natural language question:\n");
        prompt.append(naturalLanguageQuestion == null ? "" : naturalLanguageQuestion.trim());
        prompt.append("\n\n");

        prompt.append("Output format:\n");
        prompt.append("cubeName: \n");
        prompt.append("queryName: \n");
        prompt.append("aggregateFunction: \n");
        prompt.append("measure: \n");
        prompt.append("gamma: \n");
        prompt.append("sigma: \n");
        prompt.append("\n");

        prompt.append("Final answer:\n");

        return prompt.toString();
    }

    private void appendGenericExamples(StringBuilder prompt) {
        prompt.append("Generic examples using artificial schemas:\n");
        prompt.append("\n");

        prompt.append("Important note about the examples:\n");
        prompt.append("- The examples below use artificial schemas.\n");
        prompt.append("- They show the output format and mapping pattern only.\n");
        prompt.append("- Do not copy cube names, measure names, dimension names, or field names from the examples.\n");
        prompt.append("- For the final answer, use only the current cube schema.\n");
        prompt.append("\n");

        prompt.append("Example 1 artificial schema:\n");
        prompt.append("Cube:\n");
        prompt.append("example_sales_cube\n");
        prompt.append("Measures:\n");
        prompt.append("revenue, quantity\n");
        prompt.append("Allowed fields:\n");
        prompt.append("time_dim.year\n");
        prompt.append("item_dim.category\n");
        prompt.append("shop_dim.country\n");
        prompt.append("\n");

        prompt.append("Example 1 question:\n");
        prompt.append("What is the total revenue by item category for the year 2024?\n");
        prompt.append("\n");

        prompt.append("Example 1 answer:\n");
        prompt.append("cubeName: example_sales_cube\n");
        prompt.append("queryName: TotalRevenueByItemCategoryIn2024\n");
        prompt.append("aggregateFunction: Sum\n");
        prompt.append("measure: revenue\n");
        prompt.append("gamma: item_dim.category\n");
        prompt.append("sigma: time_dim.year='2024'\n");
        prompt.append("\n");

    }
}