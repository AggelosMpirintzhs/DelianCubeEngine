package LLM.experiments;

public class LLMExperimentCase {

    private final String testId;
    private final String question;
    private final ExpectedCubeQuery expectedQuery;
    private final String description;
    private final String difficulty;

    public LLMExperimentCase(
            String testId,
            String question,
            ExpectedCubeQuery expectedQuery
    ) {
        this(testId, question, expectedQuery, "", "");
    }

    public LLMExperimentCase(
            String testId,
            String question,
            ExpectedCubeQuery expectedQuery,
            String description,
            String difficulty
    ) {
        this.testId = safeString(testId);
        this.question = safeString(question);
        this.expectedQuery = expectedQuery;
        this.description = safeString(description);
        this.difficulty = safeString(difficulty);
    }

    public String getTestId() {
        return testId;
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

    private static String safeString(String value) {
        if (value == null) {
            return "";
        }

        return value.trim();
    }

    @Override
    public String toString() {
        return "LLMExperimentCase{" +
                "testId='" + testId + '\'' +
                ", question='" + question + '\'' +
                ", expectedQuery=" + expectedQuery +
                ", description='" + description + '\'' +
                ", difficulty='" + difficulty + '\'' +
                '}';
    }
}