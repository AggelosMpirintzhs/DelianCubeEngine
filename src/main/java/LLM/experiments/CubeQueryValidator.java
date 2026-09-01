package LLM.experiments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import java.util.Set;

import LLM.schema.CubeSchema;
import LLM.schema.DimensionSchema;
import LLM.schema.LevelAttributeSchema;
import LLM.schema.LevelSchema;

public class CubeQueryValidator {

    private static final double CUBE_WEIGHT = 10.0;
    private static final double AGGREGATE_WEIGHT = 15.0;
    private static final double MEASURE_WEIGHT = 15.0;
    private static final double GAMMA_WEIGHT = 30.0;
    private static final double SIGMA_WEIGHT = 30.0;

    private CubeQueryValidator() {
        // Utility class
    }

    /*
     * Strict validation.
     *
     * Ελέγχει:
     * - cubeName
     * - aggregateFunction
     * - measure
     * - gamma fields
     * - sigma fields
     * - sigma values
     *
     * Άρα εδώ customer_dim.marital_status='Married'
     * ΔΕΝ θεωρείται ίδιο με customer_dim.marital_status='M'.
     */
    public static ValidationResult validate(
            String actualAnswer,
            ExpectedCubeQuery expectedQuery,
            CubeSchema cubeSchema
    ) {
        return validateInternal(
                actualAnswer,
                expectedQuery,
                cubeSchema,
                false
        );
    }

    /*
     * Structure-focused validation.
     *
     * Ελέγχει κανονικά:
     * - cubeName
     * - aggregateFunction
     * - measure
     * - gamma fields
     * - sigma fields
     * - unknown fields
     * - missing / extra sigma fields
     *
     * Αλλά αγνοεί λάθη μόνο στο value του sigma.
     *
     * Δηλαδή θεωρεί σωστά:
     * customer_dim.marital_status='Married'
     * αντί για:
     * customer_dim.marital_status='M'
     *
     * Αρκεί το field customer_dim.marital_status να είναι σωστό.
     */
    public static ValidationResult validateIgnoringSigmaValues(
            String actualAnswer,
            ExpectedCubeQuery expectedQuery,
            CubeSchema cubeSchema
    ) {
        return validateInternal(
                actualAnswer,
                expectedQuery,
                cubeSchema,
                true
        );
    }

    /*
     * Overload χωρίς schema.
     * Δεν μπορεί να κάνει unknown-field validation.
     */
    public static ValidationResult validate(
            String actualAnswer,
            ExpectedCubeQuery expectedQuery
    ) {
        return validateInternal(
                actualAnswer,
                expectedQuery,
                null,
                false
        );
    }

    /*
     * Overload χωρίς schema, αλλά με ignored sigma values.
     */
    public static ValidationResult validateIgnoringSigmaValues(
            String actualAnswer,
            ExpectedCubeQuery expectedQuery
    ) {
        return validateInternal(
                actualAnswer,
                expectedQuery,
                null,
                true
        );
    }

    private static ValidationResult validateInternal(
            String actualAnswer,
            ExpectedCubeQuery expectedQuery,
            CubeSchema cubeSchema,
            boolean ignoreSigmaValueErrors
    ) {
        List<String> errors = new ArrayList<String>();

        if (expectedQuery == null) {
            errors.add("Expected query is null.");

            return new ValidationResult(
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0.0,
                    errors
            );
        }

        Map<String, String> actualFields = parseAnswerFields(actualAnswer);
        ParsedCubeQuery actualQuery = buildParsedCubeQuery(actualFields);

        Set<String> allowedFields = collectAllowedFields(cubeSchema);

        boolean formatValid = isFormatValid(actualFields);

        if (!formatValid) {
            errors.add("Format validation failed. Expected fields: cubeName, aggregateFunction, measure, gamma, sigma.");
            errors.add("Actual parsed fields: " + actualFields.keySet());
        }

        boolean cubeCorrect = compareSimpleField(
                "cubeName",
                expectedQuery.getCubeName(),
                actualQuery.getCubeName(),
                errors
        );

        boolean aggregateCorrect = compareSimpleField(
                "aggregateFunction",
                expectedQuery.getAggregateFunction(),
                actualQuery.getAggregateFunction(),
                errors
        );

        boolean measureCorrect = compareSimpleField(
                "measure",
                expectedQuery.getMeasure(),
                actualQuery.getMeasure(),
                errors
        );

        FieldSetComparison gammaComparison = compareGammaFields(
                expectedQuery.getGammaFields(),
                actualQuery.getGammaFields(),
                allowedFields,
                errors
        );

        SigmaComparison sigmaComparison = compareSigmaConditions(
                expectedQuery.getSigmaConditions(),
                actualQuery.getSigmaConditions(),
                allowedFields,
                errors,
                ignoreSigmaValueErrors
        );

        int unknownFieldsCount =
                gammaComparison.getUnknownFieldsCount()
                        + sigmaComparison.getUnknownFieldsCount();

        boolean gammaCorrect = gammaComparison.isCorrect();
        boolean sigmaCorrect = sigmaComparison.isCorrect();

        double weightedScore = calculateWeightedScore(
                cubeCorrect,
                aggregateCorrect,
                measureCorrect,
                gammaComparison,
                sigmaComparison
        );

        boolean valid =
                formatValid
                        && cubeCorrect
                        && aggregateCorrect
                        && measureCorrect
                        && gammaCorrect
                        && sigmaCorrect
                        && unknownFieldsCount == 0;

        return new ValidationResult(
                valid,
                formatValid,
                cubeCorrect,
                aggregateCorrect,
                measureCorrect,
                gammaCorrect,
                sigmaCorrect,
                unknownFieldsCount,
                gammaComparison.getMissingCount(),
                gammaComparison.getExtraCount(),
                sigmaComparison.getMissingCount(),
                sigmaComparison.getExtraCount(),
                sigmaComparison.getWrongValueCount(),
                weightedScore,
                errors
        );
    }

    private static boolean isFormatValid(Map<String, String> fields) {
        /*
         * Δεν απαιτούμε queryName για correctness.
         * Το queryName μπορεί να είναι κενό ή αυθαίρετο.
         */
        return fields.containsKey("cubeName")
                && fields.containsKey("aggregateFunction")
                && fields.containsKey("measure")
                && fields.containsKey("gamma")
                && fields.containsKey("sigma");
    }

    private static boolean compareSimpleField(
            String fieldName,
            String expected,
            String actual,
            List<String> errors
    ) {
        String normalizedExpected = normalizeSimpleValue(expected);
        String normalizedActual = normalizeSimpleValue(actual);

        boolean correct = normalizedExpected.equals(normalizedActual);

        if (!correct) {
            errors.add(fieldName + " validation failed.");
            errors.add("Expected " + fieldName + ": " + safe(expected));
            errors.add("Actual " + fieldName + ": " + safe(actual));
        }

        return correct;
    }

    private static FieldSetComparison compareGammaFields(
            List<String> expectedGammaFields,
            List<String> actualGammaFields,
            Set<String> allowedFields,
            List<String> errors
    ) {
        Set<String> expectedSet = normalizeFieldSet(expectedGammaFields);
        Set<String> actualSet = normalizeFieldSet(actualGammaFields);

        Set<String> missing = new HashSet<String>(expectedSet);
        missing.removeAll(actualSet);

        Set<String> extra = new HashSet<String>(actualSet);
        extra.removeAll(expectedSet);

        Set<String> unknown = findUnknownFields(actualSet, allowedFields);

        boolean correct = missing.isEmpty()
                && extra.isEmpty()
                && unknown.isEmpty();

        if (!correct) {
            errors.add("Gamma validation failed.");

            errors.add("Expected gamma fields: " + expectedSet);
            errors.add("Actual gamma fields: " + actualSet);

            if (!missing.isEmpty()) {
                errors.add("Missing gamma fields: " + missing);
            }

            if (!extra.isEmpty()) {
                errors.add("Extra gamma fields: " + extra);
            }

            if (!unknown.isEmpty()) {
                errors.add("Unknown gamma fields: " + unknown);
            }
        }

        double partialScoreRatio = calculateSetScoreRatio(expectedSet, actualSet);

        return new FieldSetComparison(
                correct,
                missing.size(),
                extra.size(),
                unknown.size(),
                partialScoreRatio
        );
    }

    private static SigmaComparison compareSigmaConditions(
            List<String> expectedSigmaConditions,
            List<String> actualSigmaConditions,
            Set<String> allowedFields,
            List<String> errors,
            boolean ignoreSigmaValueErrors
    ) {
        Map<String, String> expectedMap = parseSigmaConditions(expectedSigmaConditions);
        Map<String, String> actualMap = parseSigmaConditions(actualSigmaConditions);

        Set<String> expectedFields = expectedMap.keySet();
        Set<String> actualFields = actualMap.keySet();

        Set<String> missingFields = new HashSet<String>();
        Set<String> extraFields = new HashSet<String>();
        Set<String> wrongValueFields = new HashSet<String>();

        for (String expectedField : expectedFields) {
            if (!actualMap.containsKey(expectedField)) {
                missingFields.add(expectedField);
            } else {
                String expectedValue = expectedMap.get(expectedField);
                String actualValue = actualMap.get(expectedField);

                if (!normalizeSigmaValue(expectedValue).equals(normalizeSigmaValue(actualValue))) {
                    wrongValueFields.add(expectedField);
                }
            }
        }

        for (String actualField : actualFields) {
            if (!expectedMap.containsKey(actualField)) {
                extraFields.add(actualField);
            }
        }

        Set<String> unknownFields = findUnknownFields(actualFields, allowedFields);

        boolean correct = missingFields.isEmpty()
                && extraFields.isEmpty()
                && unknownFields.isEmpty()
                && (ignoreSigmaValueErrors || wrongValueFields.isEmpty());

        if (!correct) {
            errors.add("Sigma validation failed.");

            errors.add("Expected sigma conditions: " + formatSigmaMap(expectedMap));
            errors.add("Actual sigma conditions: " + formatSigmaMap(actualMap));

            if (!missingFields.isEmpty()) {
                errors.add("Missing sigma fields: " + missingFields);
            }

            if (!extraFields.isEmpty()) {
                errors.add("Extra sigma fields: " + extraFields);
            }

            if (!wrongValueFields.isEmpty() && !ignoreSigmaValueErrors) {
                for (String field : wrongValueFields) {
                    errors.add("Wrong sigma value for field: " + field);
                    errors.add("Expected condition: " + field + "='" + expectedMap.get(field) + "'");
                    errors.add("Actual condition: " + field + "='" + actualMap.get(field) + "'");
                }
            }

            if (!unknownFields.isEmpty()) {
                errors.add("Unknown sigma fields: " + unknownFields);
            }
        }

        double partialScoreRatio;

        if (ignoreSigmaValueErrors) {
            /*
             * Σε αυτό το mode το sigma score βασίζεται μόνο στο αν το LLM
             * βρήκε τα σωστά sigma fields, όχι στο αν πέτυχε ακριβώς τις encoded τιμές.
             */
            partialScoreRatio = calculateSetScoreRatio(expectedFields, actualFields);
        } else {
            /*
             * Strict mode: το sigma score απαιτεί σωστά fields ΚΑΙ σωστές values.
             */
            partialScoreRatio = calculateSigmaScoreRatio(expectedMap, actualMap);
        }

        return new SigmaComparison(
                correct,
                missingFields.size(),
                extraFields.size(),
                wrongValueFields.size(),
                unknownFields.size(),
                partialScoreRatio
        );
    }

    private static double calculateWeightedScore(
            boolean cubeCorrect,
            boolean aggregateCorrect,
            boolean measureCorrect,
            FieldSetComparison gammaComparison,
            SigmaComparison sigmaComparison
    ) {
        double score = 0.0;

        if (cubeCorrect) {
            score += CUBE_WEIGHT;
        }

        if (aggregateCorrect) {
            score += AGGREGATE_WEIGHT;
        }

        if (measureCorrect) {
            score += MEASURE_WEIGHT;
        }

        score += GAMMA_WEIGHT * gammaComparison.getPartialScoreRatio();
        score += SIGMA_WEIGHT * sigmaComparison.getPartialScoreRatio();

        return roundTwoDecimals(score);
    }

    private static double calculateSetScoreRatio(Set<String> expectedSet, Set<String> actualSet) {
        if (expectedSet == null || actualSet == null) {
            return 0.0;
        }

        if (expectedSet.isEmpty() && actualSet.isEmpty()) {
            return 1.0;
        }

        if (expectedSet.isEmpty() || actualSet.isEmpty()) {
            return 0.0;
        }

        Set<String> intersection = new HashSet<String>(expectedSet);
        intersection.retainAll(actualSet);

        int denominator = Math.max(expectedSet.size(), actualSet.size());

        if (denominator == 0) {
            return 1.0;
        }

        return intersection.size() / (double) denominator;
    }

    private static double calculateSigmaScoreRatio(
            Map<String, String> expectedMap,
            Map<String, String> actualMap
    ) {
        if (expectedMap == null || actualMap == null) {
            return 0.0;
        }

        if (expectedMap.isEmpty() && actualMap.isEmpty()) {
            return 1.0;
        }

        if (expectedMap.isEmpty() || actualMap.isEmpty()) {
            return 0.0;
        }

        int correctConditions = 0;

        for (String expectedField : expectedMap.keySet()) {
            if (!actualMap.containsKey(expectedField)) {
                continue;
            }

            String expectedValue = normalizeSigmaValue(expectedMap.get(expectedField));
            String actualValue = normalizeSigmaValue(actualMap.get(expectedField));

            if (expectedValue.equals(actualValue)) {
                correctConditions++;
            }
        }

        int denominator = Math.max(expectedMap.size(), actualMap.size());

        if (denominator == 0) {
            return 1.0;
        }

        return correctConditions / (double) denominator;
    }

    private static Map<String, String> parseAnswerFields(String answer) {
        Map<String, String> fields = new HashMap<String, String>();

        if (answer == null) {
            return fields;
        }

        String normalizedAnswer = answer
                .replace("\r\n", "\n")
                .replace("\r", "\n");

        String[] lines = normalizedAnswer.split("\n");

        for (String line : lines) {
            String trimmedLine = cleanLine(line);

            if (trimmedLine.isEmpty()) {
                continue;
            }

            if (trimmedLine.startsWith("```")) {
                continue;
            }

            int colonIndex = trimmedLine.indexOf(":");

            if (colonIndex == -1) {
                continue;
            }

            String key = trimmedLine.substring(0, colonIndex).trim();
            String value = trimmedLine.substring(colonIndex + 1).trim();

            if (isKnownOutputField(key)) {
                fields.put(key, removeTrailingSemicolon(value));
            }
        }

        return fields;
    }

    private static ParsedCubeQuery buildParsedCubeQuery(Map<String, String> fields) {
        return new ParsedCubeQuery(
                getOrEmpty(fields, "cubeName"),
                getOrEmpty(fields, "queryName"),
                getOrEmpty(fields, "aggregateFunction"),
                getOrEmpty(fields, "measure"),
                splitCommaSeparated(getOrEmpty(fields, "gamma")),
                splitCommaSeparated(getOrEmpty(fields, "sigma"))
        );
    }

    private static boolean isKnownOutputField(String key) {
        return "cubeName".equals(key)
                || "queryName".equals(key)
                || "aggregateFunction".equals(key)
                || "measure".equals(key)
                || "gamma".equals(key)
                || "sigma".equals(key);
    }

    private static String getOrEmpty(Map<String, String> fields, String key) {
        if (fields == null || !fields.containsKey(key)) {
            return "";
        }

        String value = fields.get(key);

        if (value == null) {
            return "";
        }

        return value.trim();
    }

    private static List<String> splitCommaSeparated(String value) {
        List<String> result = new ArrayList<String>();

        if (value == null || value.trim().isEmpty()) {
            return result;
        }

        String[] parts = value.split(",");

        for (String part : parts) {
            String trimmedPart = removeTrailingSemicolon(part.trim());

            if (!trimmedPart.isEmpty()) {
                result.add(trimmedPart);
            }
        }

        return result;
    }

    private static Set<String> normalizeFieldSet(List<String> fields) {
        Set<String> normalizedSet = new HashSet<String>();

        if (fields == null) {
            return normalizedSet;
        }

        for (String field : fields) {
            String normalized = normalizeFieldName(field);

            if (!normalized.isEmpty()) {
                normalizedSet.add(normalized);
            }
        }

        return normalizedSet;
    }

    private static Map<String, String> parseSigmaConditions(List<String> sigmaConditions) {
        Map<String, String> conditions = new HashMap<String, String>();

        if (sigmaConditions == null) {
            return conditions;
        }

        for (String condition : sigmaConditions) {
            if (condition == null) {
                continue;
            }

            String cleanCondition = removeTrailingSemicolon(condition.trim());

            if (cleanCondition.isEmpty()) {
                continue;
            }

            int equalsIndex = cleanCondition.indexOf("=");

            if (equalsIndex == -1) {
                /*
                 * Malformed condition.
                 * Το κρατάμε σαν field με κενή τιμή, ώστε να φανεί ως extra/unknown.
                 */
                String malformedField = normalizeFieldName(cleanCondition);

                if (!malformedField.isEmpty()) {
                    conditions.put(malformedField, "");
                }

                continue;
            }

            String field = cleanCondition.substring(0, equalsIndex).trim();
            String value = cleanCondition.substring(equalsIndex + 1).trim();

            String normalizedField = normalizeFieldName(field);
            String normalizedValue = normalizeSigmaValue(value);

            if (!normalizedField.isEmpty()) {
                conditions.put(normalizedField, normalizedValue);
            }
        }

        return conditions;
    }

    private static Set<String> collectAllowedFields(CubeSchema cubeSchema) {
        Set<String> allowedFields = new HashSet<String>();

        if (cubeSchema == null || cubeSchema.getDimensions() == null) {
            return allowedFields;
        }

        for (DimensionSchema dimension : cubeSchema.getDimensions()) {
            if (dimension == null || dimension.getLevels() == null) {
                continue;
            }

            String dimensionName = dimension.getName();

            if (dimensionName == null || dimensionName.trim().isEmpty()) {
                continue;
            }

            for (LevelSchema level : dimension.getLevels()) {
                if (level == null || level.getAttributes() == null) {
                    continue;
                }

                for (LevelAttributeSchema attribute : level.getAttributes()) {
                    if (attribute == null || attribute.getName() == null) {
                        continue;
                    }

                    String fullFieldName =
                            dimensionName.trim() + "." + attribute.getName().trim();

                    allowedFields.add(normalizeFieldName(fullFieldName));
                }
            }
        }

        return allowedFields;
    }

    private static Set<String> findUnknownFields(Set<String> actualFields, Set<String> allowedFields) {
        Set<String> unknownFields = new HashSet<String>();

        if (actualFields == null || actualFields.isEmpty()) {
            return unknownFields;
        }

        /*
         * Αν δεν έχουμε schema, δεν κάνουμε unknown field validation.
         */
        if (allowedFields == null || allowedFields.isEmpty()) {
            return unknownFields;
        }

        for (String actualField : actualFields) {
            if (!allowedFields.contains(normalizeFieldName(actualField))) {
                unknownFields.add(actualField);
            }
        }

        return unknownFields;
    }

    private static String formatSigmaMap(Map<String, String> sigmaMap) {
        List<String> formatted = new ArrayList<String>();

        if (sigmaMap == null || sigmaMap.isEmpty()) {
            return "[]";
        }

        for (String field : sigmaMap.keySet()) {
            formatted.add(field + "='" + sigmaMap.get(field) + "'");
        }

        return formatted.toString();
    }

    private static String normalizeSimpleValue(String value) {
        if (value == null) {
            return "";
        }

        return removeTrailingSemicolon(value.trim());
    }

    private static String normalizeFieldName(String fieldName) {
        if (fieldName == null) {
            return "";
        }

        return removeTrailingSemicolon(fieldName)
                .trim()
                .replaceAll("\\s+", "");
    }

    private static String normalizeSigmaValue(String value) {
        if (value == null) {
            return "";
        }

        String normalized = removeTrailingSemicolon(value.trim());

        normalized = normalized.replace("\"", "'");

        if (normalized.startsWith("'") && normalized.endsWith("'") && normalized.length() >= 2) {
            normalized = normalized.substring(1, normalized.length() - 1);
        }

        return normalized.trim();
    }

    private static String removeTrailingSemicolon(String value) {
        if (value == null) {
            return "";
        }

        String result = value.trim();

        while (result.endsWith(";")) {
            result = result.substring(0, result.length() - 1).trim();
        }

        return result;
    }

    private static String cleanLine(String line) {
        if (line == null) {
            return "";
        }

        return line.trim();
    }

    private static String safe(String value) {
        if (value == null) {
            return "";
        }

        return value.trim();
    }

    private static double roundTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private static class FieldSetComparison {

        private final boolean correct;
        private final int missingCount;
        private final int extraCount;
        private final int unknownFieldsCount;
        private final double partialScoreRatio;

        public FieldSetComparison(
                boolean correct,
                int missingCount,
                int extraCount,
                int unknownFieldsCount,
                double partialScoreRatio
        ) {
            this.correct = correct;
            this.missingCount = missingCount;
            this.extraCount = extraCount;
            this.unknownFieldsCount = unknownFieldsCount;
            this.partialScoreRatio = partialScoreRatio;
        }

        public boolean isCorrect() {
            return correct;
        }

        public int getMissingCount() {
            return missingCount;
        }

        public int getExtraCount() {
            return extraCount;
        }

        public int getUnknownFieldsCount() {
            return unknownFieldsCount;
        }

        public double getPartialScoreRatio() {
            return partialScoreRatio;
        }
    }

    private static class SigmaComparison {

        private final boolean correct;
        private final int missingCount;
        private final int extraCount;
        private final int wrongValueCount;
        private final int unknownFieldsCount;
        private final double partialScoreRatio;

        public SigmaComparison(
                boolean correct,
                int missingCount,
                int extraCount,
                int wrongValueCount,
                int unknownFieldsCount,
                double partialScoreRatio
        ) {
            this.correct = correct;
            this.missingCount = missingCount;
            this.extraCount = extraCount;
            this.wrongValueCount = wrongValueCount;
            this.unknownFieldsCount = unknownFieldsCount;
            this.partialScoreRatio = partialScoreRatio;
        }

        public boolean isCorrect() {
            return correct;
        }

        public int getMissingCount() {
            return missingCount;
        }

        public int getExtraCount() {
            return extraCount;
        }

        public int getWrongValueCount() {
            return wrongValueCount;
        }

        public int getUnknownFieldsCount() {
            return unknownFieldsCount;
        }

        public double getPartialScoreRatio() {
            return partialScoreRatio;
        }
    }
}