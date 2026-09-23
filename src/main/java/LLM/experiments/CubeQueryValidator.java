package LLM.experiments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import LLM.experiments.parsing.CubeQueryJsonParser;
import LLM.experiments.parsing.CubeQueryParseResult;

import LLM.schema.CubeSchema;
import LLM.schema.DimensionSchema;
import LLM.schema.LevelAttributeSchema;
import LLM.schema.LevelSchema;

public class CubeQueryValidator {

    private CubeQueryValidator() {
        // Utility class
    }

    /*
     * ==========================================================
     * STRING-BASED PUBLIC METHODS
     *
     * Kept for backwards compatibility.
     *
     * These methods parse the answer and then delegate to the
     * CubeQueryParseResult-based validation methods.
     * ==========================================================
     */

    /*
     * Strict validation.
     *
     * Checks:
     * - JSON / output format
     * - cubeName
     * - aggregateFunction
     * - measure
     * - gamma fields
     * - sigma fields
     * - sigma values
     */
    public static ValidationResult validate(
            String actualAnswer,
            ExpectedCubeQuery expectedQuery,
            CubeSchema cubeSchema
    ) {
        CubeQueryParseResult parseResult =
                CubeQueryJsonParser.parse(
                        actualAnswer
                );

        return validate(
                parseResult,
                expectedQuery,
                cubeSchema
        );
    }

    /*
     * Structure-focused validation.
     *
     * Checks everything normally, but ignores only
     * differences in sigma values.
     */
    public static ValidationResult validateIgnoringSigmaValues(
            String actualAnswer,
            ExpectedCubeQuery expectedQuery,
            CubeSchema cubeSchema
    ) {
        CubeQueryParseResult parseResult =
                CubeQueryJsonParser.parse(
                        actualAnswer
                );

        return validateIgnoringSigmaValues(
                parseResult,
                expectedQuery,
                cubeSchema
        );
    }

    /*
     * Overload without schema.
     *
     * Unknown-field validation cannot be performed.
     */
    public static ValidationResult validate(
            String actualAnswer,
            ExpectedCubeQuery expectedQuery
    ) {
        CubeQueryParseResult parseResult =
                CubeQueryJsonParser.parse(
                        actualAnswer
                );

        return validate(
                parseResult,
                expectedQuery,
                null
        );
    }

    /*
     * Overload without schema and ignoring sigma values.
     */
    public static ValidationResult validateIgnoringSigmaValues(
            String actualAnswer,
            ExpectedCubeQuery expectedQuery
    ) {
        CubeQueryParseResult parseResult =
                CubeQueryJsonParser.parse(
                        actualAnswer
                );

        return validateIgnoringSigmaValues(
                parseResult,
                expectedQuery,
                null
        );
    }

    /*
     * ==========================================================
     * PARSE-RESULT-BASED PUBLIC METHODS
     *
     * These are the preferred methods for the experiment runner.
     *
     * The LLM response can now be parsed once and the same
     * CubeQueryParseResult can be reused by:
     *
     * - strict validation
     * - structural validation
     * - query similarity evaluation
     * ==========================================================
     */

    public static ValidationResult validate(
            CubeQueryParseResult parseResult,
            ExpectedCubeQuery expectedQuery,
            CubeSchema cubeSchema
    ) {
        return validateInternal(
                parseResult,
                expectedQuery,
                cubeSchema,
                false
        );
    }

    public static ValidationResult validateIgnoringSigmaValues(
            CubeQueryParseResult parseResult,
            ExpectedCubeQuery expectedQuery,
            CubeSchema cubeSchema
    ) {
        return validateInternal(
                parseResult,
                expectedQuery,
                cubeSchema,
                true
        );
    }

    public static ValidationResult validate(
            CubeQueryParseResult parseResult,
            ExpectedCubeQuery expectedQuery
    ) {
        return validateInternal(
                parseResult,
                expectedQuery,
                null,
                false
        );
    }

    public static ValidationResult validateIgnoringSigmaValues(
            CubeQueryParseResult parseResult,
            ExpectedCubeQuery expectedQuery
    ) {
        return validateInternal(
                parseResult,
                expectedQuery,
                null,
                true
        );
    }

    /*
     * ==========================================================
     * INTERNAL VALIDATION
     * ==========================================================
     */

    private static ValidationResult validateInternal(
            CubeQueryParseResult parseResult,
            ExpectedCubeQuery expectedQuery,
            CubeSchema cubeSchema,
            boolean ignoreSigmaValueErrors
    ) {
        List<String> errors =
                new ArrayList<String>();

        if (expectedQuery == null) {

            errors.add(
                    "Expected query is null."
            );

            return createFailedValidationResult(
                    errors
            );
        }

        /*
         * The parser should normally always return a result.
         *
         * We still protect against null for robustness.
         */
        if (parseResult == null) {

            errors.add(
                    "CubeQueryParseResult is null."
            );

            return createFailedValidationResult(
                    errors
            );
        }

        /*
         * The parser is responsible for JSON syntax and
         * output-contract validation.
         */
        boolean formatValid =
                parseResult.isFormatValid();

        if (!formatValid) {

            errors.add(
                    "Format validation failed."
            );

            List<String> formatErrors =
                    parseResult.getErrors();

            if (formatErrors != null) {

                for (String formatError
                        : formatErrors) {

                    if (formatError == null
                            || formatError.trim().isEmpty()) {

                        continue;
                    }

                    errors.add(
                            "Format error: "
                                    + formatError.trim()
                    );
                }
            }
        }

        ParsedCubeQuery actualQuery =
                parseResult.getParsedQuery();

        if (actualQuery == null) {

            actualQuery =
                    new ParsedCubeQuery(
                            "",
                            "",
                            "",
                            "",
                            new ArrayList<String>(),
                            new ArrayList<String>()
                    );
        }

        Set<String> allowedFields =
                collectAllowedFields(
                        cubeSchema
                );

        /*
         * ======================================================
         * SIMPLE COMPONENTS
         * ======================================================
         */

        boolean cubeCorrect =
                compareSimpleField(
                        "cubeName",
                        expectedQuery.getCubeName(),
                        actualQuery.getCubeName(),
                        errors
                );

        boolean aggregateCorrect =
                compareAggregateFunction(
                        expectedQuery.getAggregateFunction(),
                        actualQuery.getAggregateFunction(),
                        errors
                );

        boolean measureCorrect =
                compareSimpleField(
                        "measure",
                        expectedQuery.getMeasure(),
                        actualQuery.getMeasure(),
                        errors
                );

        /*
         * ======================================================
         * GAMMA
         * ======================================================
         */

        FieldSetComparison gammaComparison =
                compareGammaFields(
                        expectedQuery.getGammaFields(),
                        actualQuery.getGammaFields(),
                        allowedFields,
                        errors
                );

        /*
         * ======================================================
         * SIGMA
         * ======================================================
         */

        SigmaComparison sigmaComparison =
                compareSigmaConditions(
                        expectedQuery.getSigmaConditions(),
                        actualQuery.getSigmaConditions(),
                        allowedFields,
                        errors,
                        ignoreSigmaValueErrors
                );

        int unknownFieldsCount =
                gammaComparison.getUnknownFieldsCount()
                        + sigmaComparison.getUnknownFieldsCount();

        boolean gammaCorrect =
                gammaComparison.isCorrect();

        boolean sigmaCorrect =
                sigmaComparison.isCorrect();

        /*
         * ======================================================
         * WEIGHTED PARTIAL SCORE
         * ======================================================
         */

        double weightedScore =
                calculateWeightedScore(
                        cubeCorrect,
                        aggregateCorrect,
                        measureCorrect,
                        gammaComparison,
                        sigmaComparison
                );

        /*
         * ======================================================
         * FINAL VALIDITY
         * ======================================================
         */

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

    /*
     * ==========================================================
     * SIMPLE FIELD COMPARISON
     * ==========================================================
     */

    private static boolean compareSimpleField(
            String fieldName,
            String expected,
            String actual,
            List<String> errors
    ) {
        String normalizedExpected =
                normalizeSimpleValue(
                        expected
                );

        String normalizedActual =
                normalizeSimpleValue(
                        actual
                );

        boolean correct =
                normalizedExpected.equals(
                        normalizedActual
                );

        if (!correct) {

            errors.add(
                    fieldName
                            + " validation failed."
            );

            errors.add(
                    "Expected "
                            + fieldName
                            + ": "
                            + safe(expected)
            );

            errors.add(
                    "Actual "
                            + fieldName
                            + ": "
                            + safe(actual)
            );
        }

        return correct;
    }

    /*
     * ==========================================================
     * GAMMA COMPARISON
     * ==========================================================
     */

    private static FieldSetComparison compareGammaFields(
            List<String> expectedGammaFields,
            List<String> actualGammaFields,
            Set<String> allowedFields,
            List<String> errors
    ) {
        Set<String> expectedSet =
                normalizeFieldSet(
                        expectedGammaFields
                );

        Set<String> actualSet =
                normalizeFieldSet(
                        actualGammaFields
                );

        Set<String> missing =
                new HashSet<String>(
                        expectedSet
                );

        missing.removeAll(
                actualSet
        );

        Set<String> extra =
                new HashSet<String>(
                        actualSet
                );

        extra.removeAll(
                expectedSet
        );

        Set<String> unknown =
                findUnknownFields(
                        actualSet,
                        allowedFields
                );

        boolean correct =
                missing.isEmpty()
                        && extra.isEmpty()
                        && unknown.isEmpty();

        if (!correct) {

            errors.add(
                    "Gamma validation failed."
            );

            errors.add(
                    "Expected gamma fields: "
                            + expectedSet
            );

            errors.add(
                    "Actual gamma fields: "
                            + actualSet
            );

            if (!missing.isEmpty()) {

                errors.add(
                        "Missing gamma fields: "
                                + missing
                );
            }

            if (!extra.isEmpty()) {

                errors.add(
                        "Extra gamma fields: "
                                + extra
                );
            }

            if (!unknown.isEmpty()) {

                errors.add(
                        "Unknown gamma fields: "
                                + unknown
                );
            }
        }

        double partialScoreRatio =
                calculateSetScoreRatio(
                        expectedSet,
                        actualSet
                );

        return new FieldSetComparison(
                correct,
                missing.size(),
                extra.size(),
                unknown.size(),
                partialScoreRatio
        );
    }

    /*
     * ==========================================================
     * SIGMA COMPARISON
     * ==========================================================
     */

    private static SigmaComparison compareSigmaConditions(
            List<String> expectedSigmaConditions,
            List<String> actualSigmaConditions,
            Set<String> allowedFields,
            List<String> errors,
            boolean ignoreSigmaValueErrors
    ) {
        Map<String, String> expectedMap =
                parseSigmaConditions(
                        expectedSigmaConditions
                );

        Map<String, String> actualMap =
                parseSigmaConditions(
                        actualSigmaConditions
                );

        Set<String> expectedFields =
                expectedMap.keySet();

        Set<String> actualFields =
                actualMap.keySet();

        Set<String> missingFields =
                new HashSet<String>();

        Set<String> extraFields =
                new HashSet<String>();

        Set<String> wrongValueFields =
                new HashSet<String>();

        /*
         * Expected fields:
         *
         * - detect missing fields
         * - detect wrong values
         */
        for (String expectedField
                : expectedFields) {

            if (!actualMap.containsKey(
                    expectedField
            )) {

                missingFields.add(
                        expectedField
                );

            } else {

                String expectedValue =
                        expectedMap.get(
                                expectedField
                        );

                String actualValue =
                        actualMap.get(
                                expectedField
                        );

                if (!normalizeSigmaValue(
                        expectedValue
                ).equals(
                        normalizeSigmaValue(
                                actualValue
                        )
                )) {

                    wrongValueFields.add(
                            expectedField
                    );
                }
            }
        }

        /*
         * Actual fields not expected by the ground truth.
         */
        for (String actualField
                : actualFields) {

            if (!expectedMap.containsKey(
                    actualField
            )) {

                extraFields.add(
                        actualField
                );
            }
        }

        Set<String> unknownFields =
                findUnknownFields(
                        actualFields,
                        allowedFields
                );

        boolean correct =
                missingFields.isEmpty()
                        && extraFields.isEmpty()
                        && unknownFields.isEmpty()
                        && (
                        ignoreSigmaValueErrors
                                || wrongValueFields.isEmpty()
                );

        if (!correct) {

            errors.add(
                    "Sigma validation failed."
            );

            errors.add(
                    "Expected sigma conditions: "
                            + formatSigmaMap(
                            expectedMap
                    )
            );

            errors.add(
                    "Actual sigma conditions: "
                            + formatSigmaMap(
                            actualMap
                    )
            );

            if (!missingFields.isEmpty()) {

                errors.add(
                        "Missing sigma fields: "
                                + missingFields
                );
            }

            if (!extraFields.isEmpty()) {

                errors.add(
                        "Extra sigma fields: "
                                + extraFields
                );
            }

            if (!wrongValueFields.isEmpty()
                    && !ignoreSigmaValueErrors) {

                for (String field
                        : wrongValueFields) {

                    errors.add(
                            "Wrong sigma value for field: "
                                    + field
                    );

                    errors.add(
                            "Expected condition: "
                                    + field
                                    + "='"
                                    + expectedMap.get(field)
                                    + "'"
                    );

                    errors.add(
                            "Actual condition: "
                                    + field
                                    + "='"
                                    + actualMap.get(field)
                                    + "'"
                    );
                }
            }

            if (!unknownFields.isEmpty()) {

                errors.add(
                        "Unknown sigma fields: "
                                + unknownFields
                );
            }
        }

        double partialScoreRatio;

        if (ignoreSigmaValueErrors) {

            partialScoreRatio =
                    calculateSetScoreRatio(
                            expectedFields,
                            actualFields
                    );

        } else {

            partialScoreRatio =
                    calculateSigmaScoreRatio(
                            expectedMap,
                            actualMap
                    );
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

    /*
     * ==========================================================
     * WEIGHTED SCORE
     * ==========================================================
     */

    private static double calculateWeightedScore(
            boolean cubeCorrect,
            boolean aggregateCorrect,
            boolean measureCorrect,
            FieldSetComparison gammaComparison,
            SigmaComparison sigmaComparison
    ) {
        double score =
                0.0;

        if (cubeCorrect) {

            score +=
                    QueryComponentWeights.CUBE;
        }

        if (aggregateCorrect) {

            score +=
                    QueryComponentWeights.AGGREGATE;
        }

        if (measureCorrect) {

            score +=
                    QueryComponentWeights.MEASURE;
        }

        score +=
                QueryComponentWeights.GAMMA
                        * gammaComparison
                        .getPartialScoreRatio();

        score +=
                QueryComponentWeights.SIGMA
                        * sigmaComparison
                        .getPartialScoreRatio();

        return roundTwoDecimals(
                score
        );
    }

    /*
     * ==========================================================
     * PARTIAL SET SCORE
     * ==========================================================
     */

    private static double calculateSetScoreRatio(
            Set<String> expectedSet,
            Set<String> actualSet
    ) {
        if (expectedSet == null
                || actualSet == null) {

            return 0.0;
        }

        if (expectedSet.isEmpty()
                && actualSet.isEmpty()) {

            return 1.0;
        }

        if (expectedSet.isEmpty()
                || actualSet.isEmpty()) {

            return 0.0;
        }

        Set<String> intersection =
                new HashSet<String>(
                        expectedSet
                );

        intersection.retainAll(
                actualSet
        );

        int denominator =
                Math.max(
                        expectedSet.size(),
                        actualSet.size()
                );

        if (denominator == 0) {
            return 1.0;
        }

        return intersection.size()
                / (double) denominator;
    }

    /*
     * ==========================================================
     * PARTIAL SIGMA SCORE
     * ==========================================================
     */

    private static double calculateSigmaScoreRatio(
            Map<String, String> expectedMap,
            Map<String, String> actualMap
    ) {
        if (expectedMap == null
                || actualMap == null) {

            return 0.0;
        }

        if (expectedMap.isEmpty()
                && actualMap.isEmpty()) {

            return 1.0;
        }

        if (expectedMap.isEmpty()
                || actualMap.isEmpty()) {

            return 0.0;
        }

        int correctConditions =
                0;

        for (String expectedField
                : expectedMap.keySet()) {

            if (!actualMap.containsKey(
                    expectedField
            )) {

                continue;
            }

            String expectedValue =
                    normalizeSigmaValue(
                            expectedMap.get(
                                    expectedField
                            )
                    );

            String actualValue =
                    normalizeSigmaValue(
                            actualMap.get(
                                    expectedField
                            )
                    );

            if (expectedValue.equals(
                    actualValue
            )) {

                correctConditions++;
            }
        }

        int denominator =
                Math.max(
                        expectedMap.size(),
                        actualMap.size()
                );

        if (denominator == 0) {
            return 1.0;
        }

        return correctConditions
                / (double) denominator;
    }

    /*
     * ==========================================================
     * NORMALIZATION / VALIDATION HELPERS
     * ==========================================================
     */

    private static Set<String> normalizeFieldSet(
            List<String> fields
    ) {
        Set<String> normalizedSet =
                new HashSet<String>();

        if (fields == null) {
            return normalizedSet;
        }

        for (String field
                : fields) {

            String normalized =
                    normalizeFieldName(
                            field
                    );

            if (!normalized.isEmpty()) {

                normalizedSet.add(
                        normalized
                );
            }
        }

        return normalizedSet;
    }

    private static Map<String, String> parseSigmaConditions(
            List<String> sigmaConditions
    ) {
        Map<String, String> conditions =
                new HashMap<String, String>();

        if (sigmaConditions == null) {
            return conditions;
        }

        for (String condition
                : sigmaConditions) {

            if (condition == null) {
                continue;
            }

            String cleanCondition =
                    removeTrailingSemicolon(
                            condition.trim()
                    );

            if (cleanCondition.isEmpty()) {
                continue;
            }

            int equalsIndex =
                    cleanCondition.indexOf(
                            "="
                    );

            /*
             * Malformed sigma condition.
             *
             * Keep the field-like part so the validator can
             * still detect missing / extra / unknown fields.
             */
            if (equalsIndex == -1) {

                String malformedField =
                        normalizeFieldName(
                                cleanCondition
                        );

                if (!malformedField.isEmpty()) {

                    conditions.put(
                            malformedField,
                            ""
                    );
                }

                continue;
            }

            String field =
                    cleanCondition
                            .substring(
                                    0,
                                    equalsIndex
                            )
                            .trim();

            String value =
                    cleanCondition
                            .substring(
                                    equalsIndex + 1
                            )
                            .trim();

            String normalizedField =
                    normalizeFieldName(
                            field
                    );

            String normalizedValue =
                    normalizeSigmaValue(
                            value
                    );

            if (!normalizedField.isEmpty()) {

                conditions.put(
                        normalizedField,
                        normalizedValue
                );
            }
        }

        return conditions;
    }

    /*
     * ==========================================================
     * SCHEMA FIELD COLLECTION
     * ==========================================================
     */

    private static Set<String> collectAllowedFields(
            CubeSchema cubeSchema
    ) {
        Set<String> allowedFields =
                new HashSet<String>();

        if (cubeSchema == null
                || cubeSchema.getDimensions() == null) {

            return allowedFields;
        }

        for (DimensionSchema dimension
                : cubeSchema.getDimensions()) {

            if (dimension == null
                    || dimension.getLevels() == null) {

                continue;
            }

            String dimensionName =
                    dimension.getName();

            if (dimensionName == null
                    || dimensionName.trim().isEmpty()) {

                continue;
            }

            for (LevelSchema level
                    : dimension.getLevels()) {

                if (level == null
                        || level.getAttributes() == null) {

                    continue;
                }

                for (LevelAttributeSchema attribute
                        : level.getAttributes()) {

                    if (attribute == null
                            || attribute.getName() == null) {

                        continue;
                    }

                    String fullFieldName =
                            dimensionName.trim()
                                    + "."
                                    + attribute
                                    .getName()
                                    .trim();

                    allowedFields.add(
                            normalizeFieldName(
                                    fullFieldName
                            )
                    );
                }
            }
        }

        return allowedFields;
    }

    private static Set<String> findUnknownFields(
            Set<String> actualFields,
            Set<String> allowedFields
    ) {
        Set<String> unknownFields =
                new HashSet<String>();

        if (actualFields == null
                || actualFields.isEmpty()) {

            return unknownFields;
        }

        /*
         * Without schema we cannot validate unknown fields.
         */
        if (allowedFields == null
                || allowedFields.isEmpty()) {

            return unknownFields;
        }

        for (String actualField
                : actualFields) {

            if (!allowedFields.contains(
                    normalizeFieldName(
                            actualField
                    )
            )) {

                unknownFields.add(
                        actualField
                );
            }
        }

        return unknownFields;
    }

    /*
     * ==========================================================
     * TEXT / VALUE HELPERS
     * ==========================================================
     */

    private static String formatSigmaMap(
            Map<String, String> sigmaMap
    ) {
        List<String> formatted =
                new ArrayList<String>();

        if (sigmaMap == null
                || sigmaMap.isEmpty()) {

            return "[]";
        }

        for (String field
                : sigmaMap.keySet()) {

            formatted.add(
                    field
                            + "='"
                            + sigmaMap.get(field)
                            + "'"
            );
        }

        return formatted.toString();
    }

    private static String normalizeSimpleValue(
            String value
    ) {
        if (value == null) {
            return "";
        }

        return removeTrailingSemicolon(
                value.trim()
        );
    }

    private static boolean compareAggregateFunction(
            String expected,
            String actual,
            List<String> errors
    ) {

        String normalizedExpected =
                normalizeSimpleValue(
                        expected
                );

        String normalizedActual =
                normalizeSimpleValue(
                        actual
                );

        boolean correct =
                normalizedExpected.equalsIgnoreCase(
                        normalizedActual
                );

        if (!correct) {

            errors.add(
                    "aggregateFunction validation failed."
            );

            errors.add(
                    "Expected aggregateFunction: "
                            + safe(expected)
            );

            errors.add(
                    "Actual aggregateFunction: "
                            + safe(actual)
            );
        }

        return correct;
    }

    private static String normalizeFieldName(
            String fieldName
    ) {
        if (fieldName == null) {
            return "";
        }

        return removeTrailingSemicolon(
                fieldName
        )
                .trim()
                .replaceAll(
                        "\\s+",
                        ""
                );
    }

    private static String normalizeSigmaValue(
            String value
    ) {
        if (value == null) {
            return "";
        }

        String normalized =
                removeTrailingSemicolon(
                        value.trim()
                );

        normalized =
                normalized.replace(
                        "\"",
                        "'"
                );

        if (normalized.startsWith("'")
                && normalized.endsWith("'")
                && normalized.length() >= 2) {

            normalized =
                    normalized.substring(
                            1,
                            normalized.length() - 1
                    );
        }

        return normalized.trim();
    }

    private static String removeTrailingSemicolon(
            String value
    ) {
        if (value == null) {
            return "";
        }

        String result =
                value.trim();

        while (result.endsWith(";")) {

            result =
                    result.substring(
                            0,
                            result.length() - 1
                    ).trim();
        }

        return result;
    }

    private static String safe(
            String value
    ) {
        if (value == null) {
            return "";
        }

        return value.trim();
    }

    private static double roundTwoDecimals(
            double value
    ) {
        return Math.round(
                value * 100.0
        ) / 100.0;
    }

    /*
     * ==========================================================
     * FAILED RESULT HELPER
     * ==========================================================
     */

    private static ValidationResult createFailedValidationResult(
            List<String> errors
    ) {
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

    /*
     * ==========================================================
     * INTERNAL COMPARISON RESULT CLASSES
     * ==========================================================
     */

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
            this.correct =
                    correct;

            this.missingCount =
                    missingCount;

            this.extraCount =
                    extraCount;

            this.unknownFieldsCount =
                    unknownFieldsCount;

            this.partialScoreRatio =
                    partialScoreRatio;
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
            this.correct =
                    correct;

            this.missingCount =
                    missingCount;

            this.extraCount =
                    extraCount;

            this.wrongValueCount =
                    wrongValueCount;

            this.unknownFieldsCount =
                    unknownFieldsCount;

            this.partialScoreRatio =
                    partialScoreRatio;
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