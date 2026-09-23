package LLM.experiments.runner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import LLM.experiments.LLMExperimentCase;

public final class ExperimentCaseOrderer {

    /*
     * ==========================================================
     * DEFAULT RANDOM SEED
     * ==========================================================
     *
     * Fixed seed makes the experiment order reproducible.
     *
     * The same order can therefore be used for every:
     *
     * - model
     * - prompt combination
     *
     * while still avoiding the original fixed workload order.
     * ==========================================================
     */

    public static final long DEFAULT_SEED = 42L;


    /*
     * ==========================================================
     * CONSTRUCTOR
     * ==========================================================
     *
     * Utility class.
     * No instances are needed.
     * ==========================================================
     */

    private ExperimentCaseOrderer() {
    }


    /*
     * ==========================================================
     * DEFAULT PAIRED ORDER
     * ==========================================================
     */

    public static List<LLMExperimentCase> createPairedOrder(
            List<LLMExperimentCase> experimentCases
    ) {

        return createPairedOrder(
                experimentCases,
                DEFAULT_SEED
        );
    }


    /*
     * ==========================================================
     * CREATE DETERMINISTIC PAIRED ORDER
     * ==========================================================
     *
     * Steps:
     *
     * 1. Validate all experiment cases.
     *
     * 2. Group cases by pairId.
     *
     * 3. Verify that every pair contains exactly:
     *
     *      - one structured case
     *      - one natural case
     *
     * 4. Shuffle the semantic pairs using a fixed seed.
     *
     * 5. Randomize which wording appears first while keeping
     *    the ordering balanced:
     *
     *      approximately half:
     *          structured -> natural
     *
     *      approximately half:
     *          natural -> structured
     *
     * 6. Return a NEW list.
     *
     * The input list is never modified.
     * ==========================================================
     */

    public static List<LLMExperimentCase> createPairedOrder(
            List<LLMExperimentCase> experimentCases,
            long seed
    ) {

        validateInput(
                experimentCases
        );


        /*
         * ======================================================
         * GROUP CASES BY PAIR ID
         * ======================================================
         */

        Map<String, PairCases> pairsById =
                new HashMap<String, PairCases>();

        Set<String> testIds =
                new HashSet<String>();


        for (LLMExperimentCase experimentCase
                : experimentCases) {

            validateExperimentCase(
                    experimentCase
            );


            /*
             * Ensure that test IDs are unique.
             */
            String testId =
                    experimentCase.getTestId();

            if (!testIds.add(testId)) {

                throw new IllegalArgumentException(
                        "Duplicate testId found: "
                                + testId
                );
            }


            String pairId =
                    experimentCase.getPairId();

            PairCases pairCases =
                    pairsById.get(
                            pairId
                    );

            if (pairCases == null) {

                pairCases =
                        new PairCases(
                                pairId
                        );

                pairsById.put(
                        pairId,
                        pairCases
                );
            }


            String wordingType =
                    experimentCase.getWordingType();


            if (LLMExperimentCase
                    .WORDING_TYPE_STRUCTURED
                    .equals(wordingType)) {

                if (pairCases.structuredCase != null) {

                    throw new IllegalArgumentException(
                            "Pair '"
                                    + pairId
                                    + "' contains more than one "
                                    + "structured case."
                    );
                }

                pairCases.structuredCase =
                        experimentCase;

            } else if (LLMExperimentCase
                    .WORDING_TYPE_NATURAL
                    .equals(wordingType)) {

                if (pairCases.naturalCase != null) {

                    throw new IllegalArgumentException(
                            "Pair '"
                                    + pairId
                                    + "' contains more than one "
                                    + "natural case."
                    );
                }

                pairCases.naturalCase =
                        experimentCase;

            } else {

                throw new IllegalArgumentException(
                        "Unsupported wordingType '"
                                + wordingType
                                + "' for test '"
                                + testId
                                + "'."
                );
            }
        }


        /*
         * ======================================================
         * VERIFY EVERY PAIR
         * ======================================================
         */

        List<PairCases> pairs =
                new ArrayList<PairCases>(
                        pairsById.values()
                );


        for (PairCases pairCases : pairs) {

            validateCompletePair(
                    pairCases
            );
        }


        /*
         * Every semantic query must contain exactly two cases.
         */
        int expectedCaseCount =
                pairs.size() * 2;

        if (experimentCases.size()
                != expectedCaseCount) {

            throw new IllegalArgumentException(
                    "Invalid paired workload. "
                            + "Expected "
                            + expectedCaseCount
                            + " cases for "
                            + pairs.size()
                            + " pairs, but found "
                            + experimentCases.size()
                            + "."
            );
        }


        /*
         * ======================================================
         * RANDOM GENERATOR
         * ======================================================
         *
         * One Random instance is used so that the complete
         * ordering is reproducible from the supplied seed.
         * ======================================================
         */

        Random random =
                new Random(
                        seed
                );


        /*
         * ======================================================
         * SHUFFLE PAIRS
         * ======================================================
         */

        Collections.shuffle(
                pairs,
                random
        );


        /*
         * ======================================================
         * CREATE BALANCED WORDING ORIENTATIONS
         * ======================================================
         *
         * For 15 pairs, for example:
         *
         * 8 pairs may start with structured
         * 7 pairs may start with natural
         *
         * or vice versa.
         *
         * Which orientation receives the extra pair is itself
         * determined by the fixed random generator.
         * ======================================================
         */

        List<Boolean> structuredFirstFlags =
                createBalancedOrientations(
                        pairs.size(),
                        random
                );


        /*
         * ======================================================
         * BUILD FINAL ORDER
         * ======================================================
         */

        List<LLMExperimentCase> orderedCases =
                new ArrayList<LLMExperimentCase>(
                        experimentCases.size()
                );


        for (int i = 0;
             i < pairs.size();
             i++) {

            PairCases pair =
                    pairs.get(i);

            boolean structuredFirst =
                    structuredFirstFlags.get(i);


            if (structuredFirst) {

                orderedCases.add(
                        pair.structuredCase
                );

                orderedCases.add(
                        pair.naturalCase
                );

            } else {

                orderedCases.add(
                        pair.naturalCase
                );

                orderedCases.add(
                        pair.structuredCase
                );
            }
        }


        return orderedCases;
    }


    /*
     * ==========================================================
     * BALANCED WORDING ORIENTATIONS
     * ==========================================================
     */

    private static List<Boolean> createBalancedOrientations(
            int pairCount,
            Random random
    ) {

        List<Boolean> orientations =
                new ArrayList<Boolean>(
                        pairCount
                );


        int structuredFirstCount =
                pairCount / 2;

        int naturalFirstCount =
                pairCount / 2;


        /*
         * With an odd number of pairs, one wording type must
         * necessarily appear first once more than the other.
         *
         * Decide this reproducibly using the seeded Random.
         */
        if (pairCount % 2 != 0) {

            boolean extraStructuredFirst =
                    random.nextBoolean();

            if (extraStructuredFirst) {

                structuredFirstCount++;

            } else {

                naturalFirstCount++;
            }
        }


        for (int i = 0;
             i < structuredFirstCount;
             i++) {

            orientations.add(
                    Boolean.TRUE
            );
        }


        for (int i = 0;
             i < naturalFirstCount;
             i++) {

            orientations.add(
                    Boolean.FALSE
            );
        }


        /*
         * Randomize which semantic pairs receive which
         * orientation.
         */
        Collections.shuffle(
                orientations,
                random
        );


        return orientations;
    }


    /*
     * ==========================================================
     * INPUT VALIDATION
     * ==========================================================
     */

    private static void validateInput(
            List<LLMExperimentCase> experimentCases
    ) {

        if (experimentCases == null) {

            throw new IllegalArgumentException(
                    "experimentCases cannot be null."
            );
        }


        if (experimentCases.isEmpty()) {

            throw new IllegalArgumentException(
                    "experimentCases cannot be empty."
            );
        }
    }


    /*
     * ==========================================================
     * CASE VALIDATION
     * ==========================================================
     */

    private static void validateExperimentCase(
            LLMExperimentCase experimentCase
    ) {

        if (experimentCase == null) {

            throw new IllegalArgumentException(
                    "experimentCases cannot contain null values."
            );
        }


        validateRequiredString(
                "testId",
                experimentCase.getTestId()
        );


        validateRequiredString(
                "pairId",
                experimentCase.getPairId()
        );


        validateRequiredString(
                "wordingType",
                experimentCase.getWordingType()
        );


        if (experimentCase.getExpectedQuery()
                == null) {

            throw new IllegalArgumentException(
                    "Expected query cannot be null for test '"
                            + experimentCase.getTestId()
                            + "'."
            );
        }
    }


    /*
     * ==========================================================
     * COMPLETE PAIR VALIDATION
     * ==========================================================
     */

    private static void validateCompletePair(
            PairCases pairCases
    ) {

        if (pairCases == null) {

            throw new IllegalArgumentException(
                    "pairCases cannot be null."
            );
        }


        if (pairCases.structuredCase == null) {

            throw new IllegalArgumentException(
                    "Pair '"
                            + pairCases.pairId
                            + "' does not contain a "
                            + "structured case."
            );
        }


        if (pairCases.naturalCase == null) {

            throw new IllegalArgumentException(
                    "Pair '"
                            + pairCases.pairId
                            + "' does not contain a "
                            + "natural case."
            );
        }


        /*
         * ======================================================
         * EXPECTED QUERY CONSISTENCY
         * ======================================================
         *
         * We deliberately require the two members of the pair
         * to reference the SAME ExpectedCubeQuery object.
         *
         * This makes accidental differences between the
         * structured and natural ground truth impossible when
         * constructing the workload as intended.
         * ======================================================
         */

        if (pairCases.structuredCase
                .getExpectedQuery()
                != pairCases.naturalCase
                .getExpectedQuery()) {

            throw new IllegalArgumentException(
                    "Pair '"
                            + pairCases.pairId
                            + "' does not use the same "
                            + "ExpectedCubeQuery object for "
                            + "structured and natural wording."
            );
        }


        /*
         * Difficulty should also be identical because wording
         * is the only experimental variable that should change.
         */
        String structuredDifficulty =
                safeString(
                        pairCases.structuredCase
                                .getDifficulty()
                );

        String naturalDifficulty =
                safeString(
                        pairCases.naturalCase
                                .getDifficulty()
                );


        if (!structuredDifficulty.equals(
                naturalDifficulty
        )) {

            throw new IllegalArgumentException(
                    "Pair '"
                            + pairCases.pairId
                            + "' has different difficulty values: "
                            + structuredDifficulty
                            + " vs "
                            + naturalDifficulty
                            + "."
            );
        }
    }


    /*
     * ==========================================================
     * REQUIRED STRING VALIDATION
     * ==========================================================
     */

    private static void validateRequiredString(
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
     * ==========================================================
     * SAFE STRING
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
     * INTERNAL PAIR HOLDER
     * ==========================================================
     */

    private static final class PairCases {

        private final String pairId;

        private LLMExperimentCase structuredCase;
        private LLMExperimentCase naturalCase;


        private PairCases(
                String pairId
        ) {

            this.pairId =
                    safeString(
                            pairId
                    );
        }
    }
}