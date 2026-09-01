package LLM.experiments;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LLMBasicExperiments {

    private LLMBasicExperiments() {
        // Utility class
    }

    public static List<LLMExperimentCase> getBasicFoodmartExperiments() {
        return Arrays.asList(

                new LLMExperimentCase(
                        "test1",
                        "What are the total store sales by product category for the year 1997?",
                        new ExpectedCubeQuery(
                                "sales_cube",
                                "Sum",
                                "store_sales",
                                Arrays.asList("product_dim.product_category"),
                                Arrays.asList("date_dim.the_year='1997'")
                        ),
                        "Total store sales grouped by product category for a specific year.",
                        "simple"
                ),

                new LLMExperimentCase(
                        "test2",
                        "How many units were sold by store state and quarter in 1998 for stores located in the USA?",
                        new ExpectedCubeQuery(
                                "sales_cube",
                                "Sum",
                                "unit_sales",
                                Arrays.asList("store_dim.store_state", "date_dim.year_quarter"),
                                Arrays.asList("store_dim.store_country='USA'", "date_dim.the_year='1998'")
                        ),
                        "Unit sales grouped by store state and quarter with country and year filters.",
                        "medium"
                ),

                new LLMExperimentCase(
                        "test3",
                        "For stores in California, what was the average sales amount for each promotion media type in Q3 1997?",
                        new ExpectedCubeQuery(
                                "sales_cube",
                                "Avg",
                                "store_sales",
                                Arrays.asList("promotion_dim.media_type"),
                                Arrays.asList("date_dim.year_quarter='1997-Q3'", "store_dim.store_state='CA'")
                        ),
                        "Average store sales grouped by promotion media type with quarter and state filters.",
                        "medium"
                ),

                new LLMExperimentCase(
                        "test4",
                        "What is the total store cost by product family and store city for stores in the USA?",
                        new ExpectedCubeQuery(
                                "sales_cube",
                                "Sum",
                                "store_cost",
                                Arrays.asList("product_dim.product_family", "store_dim.store_city"),
                                Arrays.asList("store_dim.store_country='USA'")
                        ),
                        "Total store cost grouped by product family and store city.",
                        "medium"
                ),

                new LLMExperimentCase(
                        "test5",
                        "What are the total store sales of low-fat products by month in 1997?",
                        new ExpectedCubeQuery(
                                "sales_cube",
                                "Sum",
                                "store_sales",
                                Arrays.asList("date_dim.year_and_month"),
                                Arrays.asList("product_dim.low_fat='1'", "date_dim.the_year='1997'")
                        ),
                        "Total store sales for low-fat products grouped by month.",
                        "medium"
                ),

                new LLMExperimentCase(
                        "test6",
                        "How many units were sold by customer yearly income and gender for married customers with a Golden member card?",
                        new ExpectedCubeQuery(
                                "sales_cube",
                                "Sum",
                                "unit_sales",
                                Arrays.asList("customer_dim.yearly_income", "customer_dim.gender"),
                                Arrays.asList("customer_dim.marital_status='M'", "customer_dim.member_card='Golden'")
                        ),
                        "Unit sales grouped by customer income and gender with customer filters.",
                        "complex"
                ),

                new LLMExperimentCase(
                        "test7",
                        "What is the average store sales amount by store type for stores that have both a coffee bar and a salad bar in 1998?",
                        new ExpectedCubeQuery(
                                "sales_cube",
                                "Avg",
                                "store_sales",
                                Arrays.asList("store_dim.store_type"),
                                Arrays.asList("store_dim.coffee_bar='1'", "store_dim.salad_bar='1'", "date_dim.the_year='1998'")
                        ),
                        "Average store sales grouped by store type with facility and year filters.",
                        "complex"
                ),

                new LLMExperimentCase(
                        "test8",
                        "What are the total store sales by brand name and product subcategory for customers from Washington, USA, during the first quarter of 1997?",
                        new ExpectedCubeQuery(
                                "sales_cube",
                                "Sum",
                                "store_sales",
                                Arrays.asList("product_dim.brand_name", "product_dim.product_subcategory"),
                                Arrays.asList("customer_dim.country='USA'", "customer_dim.state_province='WA'", "date_dim.year_quarter='1997-Q1'")
                        ),
                        "Total store sales grouped by brand and product subcategory with customer location and quarter filters.",
                        "complex"
                ),

                new LLMExperimentCase(
                        "test9",
                        "How much food was sold in each store by product department in January 1997?",
                        new ExpectedCubeQuery(
                                "sales_cube",
                                "Sum",
                                "unit_sales",
                                Arrays.asList("store_dim.store_name", "product_dim.product_department"),
                                Arrays.asList("product_dim.product_family='Food'", "date_dim.year_and_month='1997-01'")
                        ),
                        "Food unit sales grouped by store name and product department for January 1997.",
                        "complex"
                ),

                new LLMExperimentCase(
                        "test10",
                        "What are the total store sales across the entire sales cube?",
                        new ExpectedCubeQuery(
                                "sales_cube",
                                "Sum",
                                "store_sales",
                                Collections.<String>emptyList(),
                                Collections.<String>emptyList()
                        ),
                        "Total store sales without grouping or filters.",
                        "simple"
                )
        );
    }
}