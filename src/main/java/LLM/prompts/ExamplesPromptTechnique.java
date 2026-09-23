package LLM.prompts;

public class ExamplesPromptTechnique
        implements PromptTechnique {

    @Override
    public String getName() {
        return PromptTechniqueFactory.EXAMPLES;
    }

    @Override
    public String getContent() {
        return examples();
    }

    private String examples() {

        StringBuilder examples = new StringBuilder();

        examples.append(
                "Examples:\n\n" +

                        "The following examples use artificial cube schemas.\n" +
                        "They demonstrate how natural language concepts are mapped to measures, aggregation functions, gamma fields, and sigma conditions.\n" +
                        "Do not copy cube names, measure names, dimension names, field names, or values from these examples.\n" +
                        "For the real query, use only information from the provided cube schema and user question.\n\n"
        );


        /*
         * Example 1
         */
        examples.append(
                "Example 1:\n\n" +

                        "Cube schema:\n" +
                        "Cube:\n" +
                        "example_sales_cube\n\n" +

                        "Measures:\n" +
                        "- revenue_amount\n" +
                        "- quantity_sold\n\n" +

                        "Dimensions:\n\n" +

                        "product_dim\n" +
                        "hierarchy: Product > Category\n" +
                        "levels:\n" +
                        "- Product -> product_dim.product_name\n" +
                        "- Category -> product_dim.category\n\n" +

                        "time_dim\n" +
                        "hierarchy: Month > Quarter > Year\n" +
                        "levels:\n" +
                        "- Month -> time_dim.month\n" +
                        "- Quarter -> time_dim.quarter\n" +
                        "- Year -> time_dim.year\n\n" +

                        "User question:\n" +
                        "What is the total revenue by product category for the year 2024?\n\n" +

                        "Result:\n" +
                        "{\n" +
                        "  \"cubeName\": \"example_sales_cube\",\n" +
                        "  \"queryName\": \"TotalRevenueByProductCategoryFor2024\",\n" +
                        "  \"aggregateFunction\": \"Sum\",\n" +
                        "  \"measure\": \"revenue_amount\",\n" +
                        "  \"gamma\": [\"product_dim.category\"],\n" +
                        "  \"sigma\": [\"time_dim.year='2024'\"]\n" +
                        "}\n\n"
        );


        /*
         * Example 2
         *
         * The Month level deliberately uses a field name that is
         * different from the natural-language word "month".
         */
        examples.append(
                "Example 2:\n\n" +

                        "Cube schema:\n" +
                        "Cube:\n" +
                        "example_orders_cube\n\n" +

                        "Measures:\n" +
                        "- order_amount\n" +
                        "- shipping_cost\n\n" +

                        "Dimensions:\n\n" +

                        "calendar_dim\n" +
                        "hierarchy: Month > Quarter > Year\n" +
                        "levels:\n" +
                        "- Month -> calendar_dim.month_period\n" +
                        "- Quarter -> calendar_dim.quarter_period\n" +
                        "- Year -> calendar_dim.year_period\n\n" +

                        "customer_dim\n" +
                        "hierarchy: City > Country\n" +
                        "levels:\n" +
                        "- City -> customer_dim.city\n" +
                        "- Country -> customer_dim.country\n\n" +

                        "branch_dim\n" +
                        "hierarchy: City > Country\n" +
                        "levels:\n" +
                        "- City -> branch_dim.city\n" +
                        "- Country -> branch_dim.country\n\n" +

                        "User question:\n" +
                        "What is the average order amount by month for customers from Greece?\n\n" +

                        "Result:\n" +
                        "{\n" +
                        "  \"cubeName\": \"example_orders_cube\",\n" +
                        "  \"queryName\": \"AverageOrderAmountByMonthForGreekCustomers\",\n" +
                        "  \"aggregateFunction\": \"Avg\",\n" +
                        "  \"measure\": \"order_amount\",\n" +
                        "  \"gamma\": [\"calendar_dim.month_period\"],\n" +
                        "  \"sigma\": [\"customer_dim.country='Greece'\"]\n" +
                        "}\n\n"
        );


        /*
         * Example 3
         */
        examples.append(
                "Example 3:\n\n" +

                        "Cube schema:\n" +
                        "Cube:\n" +
                        "example_inventory_cube\n\n" +

                        "Measures:\n" +
                        "- stock_quantity\n" +
                        "- stock_value\n\n" +

                        "Dimensions:\n\n" +

                        "warehouse_dim\n" +
                        "hierarchy: City > Region > Country\n" +
                        "levels:\n" +
                        "- City -> warehouse_dim.city\n" +
                        "- Region -> warehouse_dim.region\n" +
                        "- Country -> warehouse_dim.country\n\n" +

                        "product_dim\n" +
                        "hierarchy: Product > Subcategory > Category\n" +
                        "levels:\n" +
                        "- Product -> product_dim.product_name, product_dim.brand\n" +
                        "- Subcategory -> product_dim.subcategory\n" +
                        "- Category -> product_dim.category\n\n" +

                        "date_dim\n" +
                        "hierarchy: Month > Quarter > Year\n" +
                        "levels:\n" +
                        "- Month -> date_dim.month\n" +
                        "- Quarter -> date_dim.quarter\n" +
                        "- Year -> date_dim.year\n\n" +

                        "User question:\n" +
                        "What is the total stock quantity by warehouse city and product brand for Electronics products in 2025?\n\n" +

                        "Result:\n" +
                        "{\n" +
                        "  \"cubeName\": \"example_inventory_cube\",\n" +
                        "  \"queryName\": \"TotalStockByWarehouseCityAndBrandForElectronicsIn2025\",\n" +
                        "  \"aggregateFunction\": \"Sum\",\n" +
                        "  \"measure\": \"stock_quantity\",\n" +
                        "  \"gamma\": [\"warehouse_dim.city\", \"product_dim.brand\"],\n" +
                        "  \"sigma\": [\"product_dim.category='Electronics'\", \"date_dim.year='2025'\"]\n" +
                        "}\n\n"
        );


        examples.append(
                "Reminder:\n" +
                        "- These examples are artificial and demonstrate the mapping process only.\n" +
                        "- Do not reuse schema elements from the examples unless they also appear in the actual cube schema.\n" +
                        "- Always solve the actual user question using the provided cube schema.\n"
        );

        return examples.toString();
    }
}