package LLM.experiments;

public class LLMExperimentCase {

    /*
     * ==========================================================
     * DIFFICULTY VALUES
     * ==========================================================
     *
     * Difficulty is determined by the total number of
     * gamma + sigma components in the expected query.
     *
     * simple:
     *   gamma + sigma <= 2
     *
     * medium:
     *   gamma + sigma = 3-4
     *
     * complex:
     *   gamma + sigma >= 5
     * ==========================================================
     */

    public static final String DIFFICULTY_SIMPLE =
            "simple";

    public static final String DIFFICULTY_MEDIUM =
            "medium";

    public static final String DIFFICULTY_COMPLEX =
            "complex";


    /*
     * ==========================================================
     * WORDING CATEGORIES
     * ==========================================================
     *
     * These values are kept for compatibility with the
     * original workload.
     * ==========================================================
     */

    public static final String CATEGORY_STRUCTURED_WORDING =
            "structured_wording";

    public static final String CATEGORY_NATURAL_WORDING =
            "natural_wording";


    /*
     * ==========================================================
     * WORDING TYPES
     * ==========================================================
     *
     * Used by the paired secondary experiment.
     *
     * Each semantic query will have two versions:
     *
     * pair_01 / structured
     * pair_01 / natural
     *
     * Both versions must have exactly the same
     * ExpectedCubeQuery.
     * ==========================================================
     */

    public static final String WORDING_TYPE_STRUCTURED =
            "structured";

    public static final String WORDING_TYPE_NATURAL =
            "natural";


    /*
     * ==========================================================
     * FIELDS
     * ==========================================================
     */

    private final String testId;

    /*
     * Identifier shared by the structured and natural
     * versions of the same semantic query.
     *
     * Example:
     *
     * pair_01
     */
    private final String pairId;

    /*
     * structured
     * natural
     */
    private final String wordingType;

    private final String question;

    private final ExpectedCubeQuery expectedQuery;

    /*
     * Human-readable description of what this test examines.
     */
    private final String description;

    /*
     * Experimental difficulty:
     *
     * simple
     * medium
     * complex
     */
    private final String difficulty;

    /*
     * Original experiment category.
     *
     * Examples:
     *
     * structured_wording
     * natural_wording
     */
    private final String category;


    /*
     * ==========================================================
     * LEGACY / SIMPLE CONSTRUCTOR
     * ==========================================================
     */

    public LLMExperimentCase(
            String testId,
            String question,
            ExpectedCubeQuery expectedQuery
    ) {

        this(
                testId,
                question,
                expectedQuery,
                "",
                "",
                "",
                "",
                ""
        );
    }


    /*
     * ==========================================================
     * PREVIOUS CONSTRUCTOR
     *
     * Kept for compatibility with older experiments.
     * ==========================================================
     */

    public LLMExperimentCase(
            String testId,
            String question,
            ExpectedCubeQuery expectedQuery,
            String description,
            String difficulty
    ) {

        this(
                testId,
                question,
                expectedQuery,
                description,
                difficulty,
                "",
                "",
                ""
        );
    }


    /*
     * ==========================================================
     * ORIGINAL FULL CONSTRUCTOR
     *
     * Kept for compatibility with the current main workload.
     *
     * If category already identifies the wording style,
     * wordingType is derived automatically.
     * ==========================================================
     */

    public LLMExperimentCase(
            String testId,
            String question,
            ExpectedCubeQuery expectedQuery,
            String description,
            String difficulty,
            String category
    ) {

        this(
                testId,
                question,
                expectedQuery,
                description,
                difficulty,
                category,
                "",
                wordingTypeFromCategory(
                        category
                )
        );
    }


    /*
     * ==========================================================
     * EXTENDED FULL CONSTRUCTOR
     *
     * Used by the new paired structured-vs-natural experiment.
     *
     * Example:
     *
     * testId:
     *   pair_01_structured
     *
     * pairId:
     *   pair_01
     *
     * wordingType:
     *   structured
     * ==========================================================
     */

    public LLMExperimentCase(
            String testId,
            String question,
            ExpectedCubeQuery expectedQuery,
            String description,
            String difficulty,
            String category,
            String pairId,
            String wordingType
    ) {

        validateRequiredField(
                "testId",
                testId
        );

        validateRequiredField(
                "question",
                question
        );

        if (expectedQuery == null) {

            throw new IllegalArgumentException(
                    "expectedQuery cannot be null."
            );
        }

        validateWordingType(
                wordingType
        );

        this.testId =
                safeString(
                        testId
                );

        this.pairId =
                safeString(
                        pairId
                );

        this.wordingType =
                safeString(
                        wordingType
                );

        this.question =
                safeString(
                        question
                );

        this.expectedQuery =
                expectedQuery;

        this.description =
                safeString(
                        description
                );

        this.difficulty =
                safeString(
                        difficulty
                );

        this.category =
                safeString(
                        category
                );
    }


    /*
     * ==========================================================
     * GETTERS
     * ==========================================================
     */

    public String getTestId() {
        return testId;
    }

    public String getPairId() {
        return pairId;
    }

    public String getWordingType() {
        return wordingType;
    }

    public String getQuestion() {
        return question;
    }

    public ExpectedCubeQuery getExpectedQuery() {
        return expectedQuery;
    }

    public String getDescription() {
        return description;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public String getCategory() {
        return category;
    }


    /*
     * ==========================================================
     * VALIDATION
     * ==========================================================
     */

    private static void validateRequiredField(
            String fieldName,
            String value
    ) {

        if (value == null
                || value.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    fieldName
                            + " cannot be null or empty."
            );
        }
    }


    /*
     * wordingType may be empty for old experiments.
     *
     * If present, however, only the two supported values
     * are accepted.
     */
    private static void validateWordingType(
            String wordingType
    ) {

        String normalized =
                safeString(
                        wordingType
                );

        if (normalized.isEmpty()) {
            return;
        }

        if (!WORDING_TYPE_STRUCTURED.equals(
                normalized
        )
                && !WORDING_TYPE_NATURAL.equals(
                normalized
        )) {

            throw new IllegalArgumentException(
                    "Invalid wordingType: "
                            + wordingType
                            + ". Expected '"
                            + WORDING_TYPE_STRUCTURED
                            + "' or '"
                            + WORDING_TYPE_NATURAL
                            + "'."
            );
        }
    }


    /*
     * ==========================================================
     * CATEGORY -> WORDING TYPE
     * ==========================================================
     *
     * Allows the old workload to continue working without
     * explicitly supplying wordingType.
     * ==========================================================
     */

    private static String wordingTypeFromCategory(
            String category
    ) {

        String normalized =
                safeString(
                        category
                );

        if (CATEGORY_STRUCTURED_WORDING.equals(
                normalized
        )) {

            return WORDING_TYPE_STRUCTURED;
        }

        if (CATEGORY_NATURAL_WORDING.equals(
                normalized
        )) {

            return WORDING_TYPE_NATURAL;
        }

        return "";
    }


    /*
     * ==========================================================
     * HELPERS
     * ==========================================================
     */

    private static String safeString(
            String value
    ) {

        if (value == null) {
            return "";
        }

        return value.trim();
    }


    /*
     * ==========================================================
     * TO STRING
     * ==========================================================
     */

    @Override
    public String toString() {

        return "LLMExperimentCase{" +

                "testId='"
                + testId
                + '\'' +

                ", pairId='"
                + pairId
                + '\'' +

                ", wordingType='"
                + wordingType
                + '\'' +

                ", question='"
                + question
                + '\'' +

                ", expectedQuery="
                + expectedQuery +

                ", description='"
                + description
                + '\'' +

                ", difficulty='"
                + difficulty
                + '\'' +

                ", category='"
                + category
                + '\'' +

                '}';
    }
}