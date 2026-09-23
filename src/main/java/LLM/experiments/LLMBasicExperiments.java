package LLM.experiments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LLMBasicExperiments {

    private LLMBasicExperiments() {
        // Utility class
    }


    /*
     * ==========================================================
     * PAIRED FOODMART EXPERIMENTS
     * ==========================================================
     *
     * 15 semantic queries
     * x
     * 2 wording styles
     *
     * =
     *
     * 30 experiment cases
     *
     * Each pair contains:
     *
     * - one structured wording version
     * - one natural wording version
     *
     * Both versions use:
     *
     * - exactly the same ExpectedCubeQuery object
     * - exactly the same difficulty
     * - exactly the same analytical intent
     *
     * Description is currently left empty.
     * ==========================================================
     */

    public static List<LLMExperimentCase> getBasicFoodmartExperiments() {

        List<LLMExperimentCase> experiments =
                new ArrayList<LLMExperimentCase>();


        /*
         * ==========================================================
         * SIMPLE PAIRS
         * ==========================================================
         */


        /*
         * ======================================================
         * PAIR 01
         *
         * gamma = 1
         * sigma = 1
         * total = 2
         * ======================================================
         */

        ExpectedCubeQuery expected01 =
                new ExpectedCubeQuery(
                        "sales_cube",
                        "Sum",
                        "store_sales",

                        Arrays.asList(
                                "product_dim.product_category"
                        ),

                        Arrays.asList(
                                "date_dim.the_year='1997'"
                        )
                );


        addPair(
                experiments,

                "pair_01",

                "What are the total store sales by product category for the year 1997?",

                "In 1997, how much sales revenue did each product category generate?",

                expected01,

                LLMExperimentCase.DIFFICULTY_SIMPLE
        );


        /*
         * ======================================================
         * PAIR 02
         *
         * gamma = 1
         * sigma = 0
         * total = 1
         * ======================================================
         */

        ExpectedCubeQuery expected02 =
                new ExpectedCubeQuery(
                        "sales_cube",
                        "Avg",
                        "store_cost",

                        Arrays.asList(
                                "store_dim.store_type"
                        ),

                        Collections.<String>emptyList()
                );


        addPair(
                experiments,

                "pair_02",

                "What is the average store cost by store type?",

                "How does the average store cost compare across the different store types?",

                expected02,

                LLMExperimentCase.DIFFICULTY_SIMPLE
        );


        /*
         * ======================================================
         * PAIR 03
         *
         * gamma = 0
         * sigma = 0
         * total = 0
         * ======================================================
         */

        ExpectedCubeQuery expected03 =
                new ExpectedCubeQuery(
                        "sales_cube",
                        "Sum",
                        "store_sales",

                        Collections.<String>emptyList(),

                        Collections.<String>emptyList()
                );


        addPair(
                experiments,

                "pair_03",

                "What are the total store sales overall?",

                "How much sales revenue was generated overall?",

                expected03,

                LLMExperimentCase.DIFFICULTY_SIMPLE
        );


        /*
         * ======================================================
         * PAIR 04
         *
         * gamma = 1
         * sigma = 0
         * total = 1
         * ======================================================
         */

        ExpectedCubeQuery expected04 =
                new ExpectedCubeQuery(
                        "sales_cube",
                        "Sum",
                        "store_sales",

                        Arrays.asList(
                                "product_dim.product_family"
                        ),

                        Collections.<String>emptyList()
                );


        addPair(
                experiments,

                "pair_04",

                "What are the total store sales by product family?",

                "How much sales revenue did each product family generate?",

                expected04,

                LLMExperimentCase.DIFFICULTY_SIMPLE
        );


        /*
         * ======================================================
         * PAIR 05
         *
         * gamma = 0
         * sigma = 2
         * total = 2
         * ======================================================
         */

        ExpectedCubeQuery expected05 =
                new ExpectedCubeQuery(
                        "sales_cube",
                        "Sum",
                        "store_sales",

                        Collections.<String>emptyList(),

                        Arrays.asList(
                                "store_dim.store_state='CA'",
                                "date_dim.the_year='1998'"
                        )
                );


        addPair(
                experiments,

                "pair_05",

                "What are the total store sales for stores in California during 1998?",

                "How much sales revenue came from stores in California during 1998?",

                expected05,

                LLMExperimentCase.DIFFICULTY_SIMPLE
        );


        /*
         * ==========================================================
         * MEDIUM PAIRS
         * ==========================================================
         */


        /*
         * ======================================================
         * PAIR 06
         *
         * gamma = 2
         * sigma = 2
         * total = 4
         * ======================================================
         */

        ExpectedCubeQuery expected06 =
                new ExpectedCubeQuery(
                        "sales_cube",
                        "Sum",
                        "unit_sales",

                        Arrays.asList(
                                "store_dim.store_state",
                                "date_dim.year_quarter"
                        ),

                        Arrays.asList(
                                "store_dim.store_country='USA'",
                                "date_dim.the_year='1998'"
                        )
                );


        addPair(
                experiments,

                "pair_06",

                "What are the total unit sales by store state and quarter for stores in the USA during 1998?",

                "For stores in the USA in 1998, how many units were sold in each state during each quarter?",

                expected06,

                LLMExperimentCase.DIFFICULTY_MEDIUM
        );


        /*
         * ======================================================
         * PAIR 07
         *
         * gamma = 1
         * sigma = 2
         * total = 3
         * ======================================================
         */

        ExpectedCubeQuery expected07 =
                new ExpectedCubeQuery(
                        "sales_cube",
                        "Avg",
                        "store_cost",

                        Arrays.asList(
                                "product_dim.product_family"
                        ),

                        Arrays.asList(
                                "store_dim.store_country='USA'",
                                "date_dim.the_year='1997'"
                        )
                );


        addPair(
                experiments,

                "pair_07",

                "What is the average store cost by product family for stores in the USA during 1997?",

                "For stores in the USA in 1997, how did the average store cost compare across product families?",

                expected07,

                LLMExperimentCase.DIFFICULTY_MEDIUM
        );


        /*
         * ======================================================
         * PAIR 08
         *
         * gamma = 2
         * sigma = 1
         * total = 3
         * ======================================================
         */

        ExpectedCubeQuery expected08 =
                new ExpectedCubeQuery(
                        "sales_cube",
                        "Sum",
                        "store_sales",

                        Arrays.asList(
                                "promotion_dim.media_type",
                                "product_dim.product_category"
                        ),

                        Arrays.asList(
                                "date_dim.the_year='1997'"
                        )
                );


        addPair(
                experiments,

                "pair_08",

                "What are the total store sales by promotion media type and product category during 1997?",

                "In 1997, how much sales revenue came from each product category under each type of promotional media?",

                expected08,

                LLMExperimentCase.DIFFICULTY_MEDIUM
        );


        /*
         * ======================================================
         * PAIR 09
         *
         * gamma = 1
         * sigma = 2
         * total = 3
         * ======================================================
         */

        ExpectedCubeQuery expected09 =
                new ExpectedCubeQuery(
                        "sales_cube",
                        "Sum",
                        "unit_sales",

                        Arrays.asList(
                                "customer_dim.gender"
                        ),

                        Arrays.asList(
                                "customer_dim.member_card='Golden'",
                                "customer_dim.country='USA'"
                        )
                );


        addPair(
                experiments,

                "pair_09",

                "What are the total unit sales by customer gender for Golden-card customers in the USA?",

                "Among Golden-card customers in the USA, how many units did men and women buy?",

                expected09,

                LLMExperimentCase.DIFFICULTY_MEDIUM
        );


        /*
         * ======================================================
         * PAIR 10
         *
         * gamma = 1
         * sigma = 2
         * total = 3
         * ======================================================
         */

        ExpectedCubeQuery expected10 =
                new ExpectedCubeQuery(
                        "sales_cube",
                        "Sum",
                        "store_sales",

                        Arrays.asList(
                                "date_dim.year_and_month"
                        ),

                        Arrays.asList(
                                "product_dim.low_fat='1'",
                                "date_dim.the_year='1997'"
                        )
                );


        addPair(
                experiments,

                "pair_10",

                "What are the total store sales by month during 1997 for low-fat products?",

                "During 1997, how did the sales revenue from low-fat products change from month to month?",

                expected10,

                LLMExperimentCase.DIFFICULTY_MEDIUM
        );


        /*
         * ==========================================================
         * COMPLEX PAIRS
         * ==========================================================
         */


        /*
         * ======================================================
         * PAIR 11
         *
         * gamma = 2
         * sigma = 3
         * total = 5
         * ======================================================
         */

        ExpectedCubeQuery expected11 =
                new ExpectedCubeQuery(
                        "sales_cube",
                        "Sum",
                        "store_sales",

                        Arrays.asList(
                                "date_dim.year_and_month",
                                "customer_dim.state_province"
                        ),

                        Arrays.asList(
                                "date_dim.the_year='1997'",
                                "customer_dim.country='USA'",
                                "product_dim.product_subcategory='Milk'"
                        )
                );


        addPair(
                experiments,

                "pair_11",

                "What are the total store sales by month and customer state during 1997 for customers in the USA who purchased products from the Milk subcategory?",

                "For customers in the USA who bought Milk products in 1997, how much sales revenue came from each state in each month?",

                expected11,

                LLMExperimentCase.DIFFICULTY_COMPLEX
        );


        /*
         * ======================================================
         * PAIR 12
         *
         * gamma = 2
         * sigma = 3
         * total = 5
         * ======================================================
         */

        ExpectedCubeQuery expected12 =
                new ExpectedCubeQuery(
                        "sales_cube",
                        "Sum",
                        "store_sales",

                        Arrays.asList(
                                "product_dim.product_subcategory",
                                "date_dim.year_quarter"
                        ),

                        Arrays.asList(
                                "date_dim.the_year='1997'",
                                "product_dim.product_category='Breakfast Foods'",
                                "store_dim.store_country='USA'"
                        )
                );


        addPair(
                experiments,

                "pair_12",

                "What are the total store sales by product subcategory and quarter during 1997 for products in the Breakfast Foods category sold by stores in the USA?",

                "For Breakfast Foods sold by stores in the USA in 1997, how much sales revenue came from each product subcategory in each quarter?",

                expected12,

                LLMExperimentCase.DIFFICULTY_COMPLEX
        );


        /*
         * ======================================================
         * PAIR 13
         *
         * gamma = 2
         * sigma = 3
         * total = 5
         * ======================================================
         */

        ExpectedCubeQuery expected13 =
                new ExpectedCubeQuery(
                        "sales_cube",
                        "Sum",
                        "store_sales",

                        Arrays.asList(
                                "date_dim.year_and_month",
                                "customer_dim.city"
                        ),

                        Arrays.asList(
                                "date_dim.year_quarter='1997-Q3'",
                                "customer_dim.state_province='CA'",
                                "promotion_dim.media_type='Daily Paper'"
                        )
                );


        addPair(
                experiments,

                "pair_13",

                "What are the total store sales by month and customer city during Q3 1997 for customers in California when the promotion media type was Daily Paper?",

                "During Q3 1997, when Daily Paper promotions were used for customers in California, how much sales revenue came from each customer city in each month?",

                expected13,

                LLMExperimentCase.DIFFICULTY_COMPLEX
        );


        /*
         * ======================================================
         * PAIR 14
         *
         * gamma = 2
         * sigma = 4
         * total = 6
         * ======================================================
         */

        ExpectedCubeQuery expected14 =
                new ExpectedCubeQuery(
                        "sales_cube",
                        "Sum",
                        "store_sales",

                        Arrays.asList(
                                "date_dim.year_quarter",
                                "product_dim.brand_name"
                        ),

                        Arrays.asList(
                                "product_dim.product_family='Food'",
                                "customer_dim.state_province='WA'",
                                "customer_dim.country='USA'",
                                "date_dim.the_year='1997'"
                        )
                );


        addPair(
                experiments,

                "pair_14",

                "What are the total store sales by quarter and brand during 1997 for products in the Food product family purchased by customers from Washington state in the USA?",

                "For Food purchases made by customers from Washington in the USA during 1997, how did sales revenue vary across quarters and brands?",

                expected14,

                LLMExperimentCase.DIFFICULTY_COMPLEX
        );


        /*
         * ======================================================
         * PAIR 15
         *
         * gamma = 2
         * sigma = 3
         * total = 5
         * ======================================================
         */

        ExpectedCubeQuery expected15 =
                new ExpectedCubeQuery(
                        "sales_cube",
                        "Avg",
                        "store_sales",

                        Arrays.asList(
                                "product_dim.product_category",
                                "store_dim.store_type"
                        ),

                        Arrays.asList(
                                "promotion_dim.media_type='Daily Paper'",
                                "store_dim.store_state='CA'",
                                "date_dim.year_quarter='1997-Q3'"
                        )
                );


        addPair(
                experiments,

                "pair_15",

                "What is the average store sales by product category and store type in California during Q3 1997 when the promotion media type was Daily Paper?",

                "When Daily Paper promotions were used in California during Q3 1997, how did the average sales revenue differ across product categories and store types?",

                expected15,

                LLMExperimentCase.DIFFICULTY_COMPLEX
        );


        /*
         * ==========================================================
         * FINAL WORKLOAD
         * ==========================================================
         *
         * 15 semantic pairs
         *
         * 15 structured cases
         * 15 natural cases
         *
         * 30 cases total.
         *
         * Execution order is randomized later by
         * ExperimentCaseOrderer using seed 42.
         * ==========================================================
         */

        return experiments;
    }


    /*
     * ==========================================================
     * ADD ONE CONTROLLED PAIR
     * ==========================================================
     *
     * The SAME ExpectedCubeQuery object is deliberately supplied
     * to both wording variants.
     *
     * description is currently stored as an empty string.
     * ==========================================================
     */

    private static void addPair(
            List<LLMExperimentCase> experiments,
            String pairId,
            String structuredQuestion,
            String naturalQuestion,
            ExpectedCubeQuery expectedQuery,
            String difficulty
    ) {

        if (experiments == null) {

            throw new IllegalArgumentException(
                    "experiments cannot be null."
            );
        }


        if (pairId == null
                || pairId.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "pairId cannot be null or empty."
            );
        }


        if (structuredQuestion == null
                || structuredQuestion.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "structuredQuestion cannot be null or empty."
            );
        }


        if (naturalQuestion == null
                || naturalQuestion.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "naturalQuestion cannot be null or empty."
            );
        }


        if (expectedQuery == null) {

            throw new IllegalArgumentException(
                    "expectedQuery cannot be null."
            );
        }


        /*
         * ======================================================
         * STRUCTURED VERSION
         * ======================================================
         */

        experiments.add(
                new LLMExperimentCase(
                        pairId + "_structured",

                        structuredQuestion,

                        expectedQuery,

                        "",

                        difficulty,

                        LLMExperimentCase
                                .CATEGORY_STRUCTURED_WORDING,

                        pairId,

                        LLMExperimentCase
                                .WORDING_TYPE_STRUCTURED
                )
        );


        /*
         * ======================================================
         * NATURAL VERSION
         * ======================================================
         */

        experiments.add(
                new LLMExperimentCase(
                        pairId + "_natural",

                        naturalQuestion,

                        expectedQuery,

                        "",

                        difficulty,

                        LLMExperimentCase
                                .CATEGORY_NATURAL_WORDING,

                        pairId,

                        LLMExperimentCase
                                .WORDING_TYPE_NATURAL
                )
        );
    }
}