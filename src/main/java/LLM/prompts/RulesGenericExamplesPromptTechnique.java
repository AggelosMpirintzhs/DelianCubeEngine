package LLM.prompts;

import LLM.schema.CubeSchema;

public class RulesGenericExamplesPromptTechnique implements PromptTechnique {

    @Override
    public String getName() {
        return PromptTechniqueFactory.RULES_GENERIC_EXAMPLES;
    }

    @Override
    public String buildPrompt(CubeSchema cubeSchema, String naturalLanguageQuestion) {
        CubeSchemaPromptFormatterCompact formatter = new CubeSchemaPromptFormatterCompact();
        String formattedSchema = formatter.format(cubeSchema);

        StringBuilder prompt = new StringBuilder();

        prompt.append("You translate a natural language question into CineCubes query components.\n");
        prompt.append("Use only the current cube schema given below.\n");
        prompt.append("\n");

        prompt.append("Important note about the examples:\n");
        prompt.append("- The examples below use small artificial schemas.\n");
        prompt.append("- The examples are only meant to show the reasoning pattern and the output format.\n");
        prompt.append("- Do not copy cube names, measure names, dimension names, or field names from the examples.\n");
        prompt.append("- For the final answer, use only the cube name, measures, and allowed fields from the current schema.\n");
        prompt.append("\n");

        prompt.append("Basic rules:\n");
        prompt.append("1. Return only the six required fields, in the exact output format.\n");
        prompt.append("2. cubeName must be copied exactly from the current schema.\n");
        prompt.append("3. aggregateFunction must be one of: Sum, Avg, Count, Min, Max.\n");
        prompt.append("4. measure must be one of the measures shown in the current schema.\n");
        prompt.append("5. gamma contains grouping fields.\n");
        prompt.append("6. sigma contains filtering conditions.\n");
        prompt.append("7. Every gamma field must be copied exactly from the Allowed fields of the current schema.\n");
        prompt.append("8. Every sigma field must be copied exactly from the Allowed fields of the current schema.\n");
        prompt.append("9. A gamma item must look like dimensionName.attributeName.\n");
        prompt.append("10. A sigma condition must look like dimensionName.attributeName='value'.\n");
        prompt.append("11. If there is no grouping, leave gamma empty.\n");
        prompt.append("12. If there is no filtering, leave sigma empty.\n");
        prompt.append("13. Do not invent, rename, shorten, or paraphrase schema fields.\n");
        prompt.append("14. Do not output SQL, JSON, markdown, bullets, numbering, or explanations in the final answer.\n");
        prompt.append("\n");

        prompt.append("Semantic rules:\n");
        prompt.append("- Words like total, sum, overall, or how many usually imply aggregateFunction: Sum.\n");
        prompt.append("- Words like average or mean usually imply aggregateFunction: Avg.\n");
        prompt.append("- Words like by, per, for each, grouped by, or broken down by usually indicate gamma fields.\n");
        prompt.append("- Restrictions such as time, place, product, customer, store, category, status, or media usually indicate sigma conditions.\n");
        prompt.append("- If a concept appears in multiple dimensions, choose the dimension whose entity is mentioned in the question.\n");
        prompt.append("- If the question mentions customers, prefer customer-related fields when available.\n");
        prompt.append("- If the question mentions stores, shops, branches, or outlets, prefer store-related fields when available.\n");
        prompt.append("- If the question mentions products, items, or goods, prefer product-related fields when available.\n");
        prompt.append("- If the question mentions time periods, prefer date or time-related fields when available.\n");
        prompt.append("- Dimensions may be hierarchical. Choose the allowed field whose level matches the requested granularity.\n");
        prompt.append("- If the question asks by month, choose a month-level field if one exists.\n");
        prompt.append("- If the question asks by year, choose a year-level field if one exists.\n");
        prompt.append("- If the question asks by city, choose a city-level field if one exists.\n");
        prompt.append("- If the question asks by country, choose a country-level field if one exists.\n");
        prompt.append("\n");

        appendGenericExamples(prompt);

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
        prompt.append("Generic examples:\n");
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

        prompt.append("Example 2 artificial schema:\n");
        prompt.append("Cube:\n");
        prompt.append("example_orders_cube\n");
        prompt.append("Measures:\n");
        prompt.append("order_amount, order_count\n");
        prompt.append("Allowed fields:\n");
        prompt.append("calendar_dim.month\n");
        prompt.append("customer_dim.country\n");
        prompt.append("customer_dim.segment\n");
        prompt.append("branch_dim.city\n");
        prompt.append("branch_dim.country\n");
        prompt.append("\n");

        prompt.append("Example 2 question:\n");
        prompt.append("What is the average order amount by month for customers from Greece?\n");
        prompt.append("\n");

        prompt.append("Example 2 answer:\n");
        prompt.append("cubeName: example_orders_cube\n");
        prompt.append("queryName: AverageOrderAmountByMonthForGreekCustomers\n");
        prompt.append("aggregateFunction: Avg\n");
        prompt.append("measure: order_amount\n");
        prompt.append("gamma: calendar_dim.month\n");
        prompt.append("sigma: customer_dim.country='Greece'\n");
        prompt.append("\n");

        prompt.append("Example 3 artificial schema:\n");
        prompt.append("Cube:\n");
        prompt.append("example_inventory_cube\n");
        prompt.append("Measures:\n");
        prompt.append("stock_quantity, stock_value\n");
        prompt.append("Allowed fields:\n");
        prompt.append("warehouse_dim.city\n");
        prompt.append("warehouse_dim.country\n");
        prompt.append("product_dim.family\n");
        prompt.append("product_dim.brand\n");
        prompt.append("date_dim.year\n");
        prompt.append("\n");

        prompt.append("Example 3 question:\n");
        prompt.append("How many stock units are available by warehouse city for products in the Electronics family?\n");
        prompt.append("\n");

        prompt.append("Example 3 answer:\n");
        prompt.append("cubeName: example_inventory_cube\n");
        prompt.append("queryName: TotalStockQuantityByWarehouseCityForElectronics\n");
        prompt.append("aggregateFunction: Sum\n");
        prompt.append("measure: stock_quantity\n");
        prompt.append("gamma: warehouse_dim.city\n");
        prompt.append("sigma: product_dim.family='Electronics'\n");
        prompt.append("\n");

        prompt.append("Reminder after examples:\n");
        prompt.append("- The final answer must not use fields from the artificial examples.\n");
        prompt.append("- The final answer must use only fields from the current cube schema below.\n");
        prompt.append("\n");
    }
}