package LLM.prompts;

import LLM.schema.CubeSchema;

public class PromptBuilder {

    private final CubeSchemaPromptFormatterCompact cubeSchemaPromptFormatterCompact;

    public PromptBuilder() {
        this.cubeSchemaPromptFormatterCompact = new CubeSchemaPromptFormatterCompact();
    }

    public PromptBuilder(CubeSchemaPromptFormatterCompact cubeSchemaPromptFormatterCompact) {
        if (cubeSchemaPromptFormatterCompact == null) {
            throw new IllegalArgumentException("CubeSchemaPromptFormatterCompact cannot be null.");
        }

        this.cubeSchemaPromptFormatterCompact = cubeSchemaPromptFormatterCompact;
    }

    public String buildPrompt(CubeSchema cubeSchema, String naturalLanguageQuestion) {
        if (cubeSchema == null) {
            throw new IllegalArgumentException("CubeSchema cannot be null.");
        }

        if (naturalLanguageQuestion == null || naturalLanguageQuestion.trim().isEmpty()) {
            throw new IllegalArgumentException("Natural language question cannot be null or empty.");
        }

        StringBuilder prompt = new StringBuilder();

        /*
         * First instruction block before the schema.
         */
        appendInstructions(prompt);
        appendOutputFormat(prompt);
//        appendExamples(prompt);
//        appendSchemaExplanation(prompt);
        appendAnalysisMethod(prompt);

        /*
         * Cube schema is included only once because it can be large.
         */
        prompt.append(cubeSchemaPromptFormatterCompact.format(cubeSchema));

        /*
         * Repeat only the compact instructions after the schema.
         * Do not repeat examples or schema explanation to keep the prompt smaller.
         */
//        appendUserQuestion(prompt, naturalLanguageQuestion);
        appendInstructions(prompt);
//        appendOutputFormat(prompt);
        appendExamples(prompt);
//        appendSchemaExplanation(prompt);
//        appendAnalysisMethod(prompt);

        appendUserQuestion(prompt, naturalLanguageQuestion);

        return prompt.toString();
    }

    private void appendInstructions(StringBuilder prompt) {
        prompt.append("You translate a natural language question into CineCubes query components.\n");
        prompt.append("Use only the cube schema given below.\n");
        prompt.append("\n");

        prompt.append("Main goal:\n");
        prompt.append("Given a user's question in natural language, identify the cube, aggregation, measure, grouping levels, and filter conditions needed to build a CineCubes query.\n");
        prompt.append("\n");

        prompt.append("Important rules:\n");
        prompt.append("1. Return only the six required fields, in the exact output format.\n");
        prompt.append("2. cubeName must use the cubeDataSource value from the schema.\n");
        prompt.append("3. aggregateFunction must be one of: Sum, Avg, Count, Min, Max.\n");
        prompt.append("4. measure must be the physical measure column. If the measure source is table.column, use only column.\n");
        prompt.append("5. Dimensions are hierarchical. Use dimension levels from the schema.\n");
        prompt.append("6. gamma is the analysis/grouping part of the query. It contains the level or levels at which the result should be broken down.\n");
        prompt.append("7. Each gamma item must be an existing dimension-level pair from the schema.\n");
        prompt.append("8. In gamma, the levelName after the dot must belong to the same dimensionName before the dot.\n");
        prompt.append("9. To create gamma, first choose the correct dimension from the schema, then choose one of its own levels.\n");
        prompt.append("10. sigma is the filtering part of the query. It contains conditions on dimension levels.\n");
        prompt.append("11. A gamma item must look like dimensionName.levelName.\n");
        prompt.append("12. A sigma condition must look like dimensionName.levelName='value'.\n");
        prompt.append("13. If there is no gamma or sigma, leave the value empty.\n");
        prompt.append("14. If a user concept matches a level attribute, output the level that contains that attribute using dimensionName.levelName.\n");
        prompt.append("15. Match each requested concept to the dimension that contains the corresponding level or attribute in the schema.\n");
        prompt.append("16. Preserve explicit filter values from the user question exactly when possible, including codes, labels, abbreviations, and compound period values.\n");
        prompt.append("17. Resolve ambiguous concepts using the closest context in the question. When a grouping concept and a filter concept belong to the same semantic area, prefer levels from the same dimension.\n");
        prompt.append("\n");
    }

    private void appendOutputFormat(StringBuilder prompt) {
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

        prompt.append("Do not use JSON, markdown, quotes around full values, semicolons, bullets, numbering, or explanations.\n");
        prompt.append("\n");
    }

    private void appendExamples(StringBuilder prompt) {
        prompt.append("Accepted answer examples using fictitious schema names only:\n");
        prompt.append("\n");

        prompt.append("Example 1:\n");
        prompt.append("cubeName: example_sales\n");
        prompt.append("queryName: TotalRevenueByProductAndRegion\n");
        prompt.append("aggregateFunction: Sum\n");
        prompt.append("measure: revenue_amount\n");
        prompt.append("gamma: product_dimension.category,region_dimension.region\n");
        prompt.append("sigma: time_dimension.quarter='2024-Q1',status_dimension.status='Active'\n");
        prompt.append("\n");

        prompt.append("Example 2:\n");
        prompt.append("cubeName: example_inventory\n");
        prompt.append("queryName: AverageStockByWarehouse\n");
        prompt.append("aggregateFunction: Avg\n");
        prompt.append("measure: stock_quantity\n");
        prompt.append("gamma: warehouse_dimension.city\n");
        prompt.append("sigma: \n");
        prompt.append("\n");

        prompt.append("The examples show syntax only. For the real answer, use only the actual cube schema below.\n");
        prompt.append("\n");
    }

    private void appendSchemaExplanation(StringBuilder prompt) {
        prompt.append("How to read the cube schema:\n");
        prompt.append("- cubeName is the logical name of the cube in the schema.\n");
        prompt.append("- cubeDataSource is the value that must be used as cubeName in the answer.\n");
        prompt.append("- Measures are numeric values that can be aggregated, such as sales, cost, quantity, or hours.\n");
        prompt.append("- Each measure may have a source like table.column. In the answer, use only the column part as the measure.\n");
        prompt.append("- Dimensions describe how the data can be analyzed, filtered, or broken down.\n");
        prompt.append("- Each dimension is hierarchical and contains ordered levels.\n");
        prompt.append("- A level is a valid analysis point inside a dimension, such as month, quarter, region, state, category, or lvl1.\n");
        prompt.append("- Level names are valid only inside the dimension where they are listed.\n");
        prompt.append("- gamma uses dimension levels from the schema. Each gamma item must combine a dimension with one of its own levels.\n");
        prompt.append("- Attributes describe a level. When the user's wording matches an attribute, use the level that contains that attribute.\n");
        prompt.append("- sigma must use dimension levels from the schema. It says which records should be filtered before the aggregation.\n");
        prompt.append("- References show how cube facts connect to dimensions. Use them to understand which dimensions belong to the cube.\n");
        prompt.append("- If multiple dimensions contain related concepts, use the rest of the question to choose the most consistent dimension.\n");
        prompt.append("- Preserve user-provided filter values exactly, especially codes, abbreviations, named categories, and compound time periods.\n");
        prompt.append("\n");
    }

    private void appendAnalysisMethod(StringBuilder prompt) {
        prompt.append("Analysis method:\n");
        prompt.append("1. Identify the requested measure and aggregation from the question.\n");
        prompt.append("2. Identify the words that describe how the result should be broken down; these become gamma levels.\n");
        prompt.append("3. For each gamma concept, find the dimension that contains the matching level in the schema.\n");
        prompt.append("4. If more than one dimension has a related level, use the filters and surrounding context to choose the most consistent dimension.\n");
        prompt.append("5. Identify restrictions such as time, place, category, or media; these become sigma conditions.\n");
        prompt.append("6. For each sigma concept, use the dimension level that contains the matching concept or attribute.\n");
        prompt.append("7. Preserve explicit values from the user question when possible.\n");
        prompt.append("\n");
    }

    private void appendUserQuestion(StringBuilder prompt, String naturalLanguageQuestion) {
        prompt.append("User question:\n");
        prompt.append(naturalLanguageQuestion.trim()).append("\n");
        prompt.append("\n");

        prompt.append("Final answer:\n");
    }
}