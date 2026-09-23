package client;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import LLM.extractor.CubeIniSchemaExtractor;

import LLM.schema.CubeReference;
import LLM.schema.CubeSchema;
import LLM.schema.DimensionSchema;
import LLM.schema.LevelAttributeSchema;
import LLM.schema.LevelSchema;
import LLM.schema.MeasureSchema;

public class CubeSchemaInspectionClient {

    public static void main(String[] args)
            throws Exception {

        String inputFolder =
                "foodmart_reduced";

        String cubeFileName =
                "sales";

        Path iniPath =
                Paths.get(
                        "InputFiles",
                        inputFolder,
                        cubeFileName + ".ini"
                );

        if (!Files.exists(iniPath)) {

            throw new RuntimeException(
                    "INI file not found: "
                            + iniPath.toAbsolutePath()
            );
        }

        CubeIniSchemaExtractor extractor =
                new CubeIniSchemaExtractor();

        CubeSchema cubeSchema =
                extractor.extractFromFile(
                        iniPath.toString()
                );

        printCubeSchema(
                cubeSchema
        );
    }

    private static void printCubeSchema(
            CubeSchema cubeSchema
    ) {

        if (cubeSchema == null) {

            System.out.println(
                    "CubeSchema is null."
            );

            return;
        }

        System.out.println();
        System.out.println(
                "========================================"
        );

        System.out.println(
                "CUBE SCHEMA"
        );

        System.out.println(
                "========================================"
        );

        System.out.println();

        /*
         * ======================================================
         * CUBE
         * ======================================================
         */

        System.out.println(
                "Cube name: "
                        + safe(
                        cubeSchema.getCubeName()
                )
        );

        System.out.println(
                "Cube datasource: "
                        + safe(
                        cubeSchema.getCubeDataSource()
                )
        );

        System.out.println(
                "Datasource type: "
                        + safe(
                        cubeSchema.getDataSourceType()
                )
        );

        System.out.println(
                "DBC INI path: "
                        + safe(
                        cubeSchema.getDbcIniPath()
                )
        );

        System.out.println();

        /*
         * ======================================================
         * MEASURES
         * ======================================================
         */

        System.out.println(
                "========================================"
        );

        System.out.println(
                "MEASURES"
        );

        System.out.println(
                "========================================"
        );

        List<MeasureSchema> measures =
                cubeSchema.getMeasures();

        if (measures == null
                || measures.isEmpty()) {

            System.out.println(
                    "No measures."
            );

        } else {

            for (MeasureSchema measure
                    : measures) {

                if (measure == null) {
                    continue;
                }

                System.out.println(
                        "- name: "
                                + safe(
                                measure.getName()
                        )
                );

                System.out.println(
                        "  source: "
                                + safe(
                                measure.getSource()
                        )
                );
            }
        }

        System.out.println();

        /*
         * ======================================================
         * DIMENSIONS
         * ======================================================
         */

        System.out.println(
                "========================================"
        );

        System.out.println(
                "DIMENSIONS"
        );

        System.out.println(
                "========================================"
        );

        List<DimensionSchema> dimensions =
                cubeSchema.getDimensions();

        if (dimensions == null
                || dimensions.isEmpty()) {

            System.out.println(
                    "No dimensions."
            );

        } else {

            for (DimensionSchema dimension
                    : dimensions) {

                printDimension(
                        dimension
                );
            }
        }

        /*
         * ======================================================
         * REFERENCES
         * ======================================================
         */

        System.out.println(
                "========================================"
        );

        System.out.println(
                "REFERENCES"
        );

        System.out.println(
                "========================================"
        );

        List<CubeReference> references =
                cubeSchema.getReferences();

        if (references == null
                || references.isEmpty()) {

            System.out.println(
                    "No references."
            );

        } else {

            for (CubeReference reference
                    : references) {

                if (reference == null) {
                    continue;
                }

                System.out.println(
                        "- dimension: "
                                + safe(
                                reference.getDimensionName()
                        )
                );

                System.out.println(
                        "  cube field: "
                                + safe(
                                reference.getCubeField()
                        )
                );
            }
        }

        System.out.println();

        /*
         * ======================================================
         * SUMMARY
         * ======================================================
         */

        System.out.println(
                "========================================"
        );

        System.out.println(
                "SUMMARY"
        );

        System.out.println(
                "========================================"
        );

        System.out.println(
                "Cube: "
                        + safe(
                        cubeSchema.getCubeName()
                )
        );

        System.out.println(
                "Measures: "
                        + size(
                        cubeSchema.getMeasures()
                )
        );

        System.out.println(
                "Dimensions: "
                        + size(
                        cubeSchema.getDimensions()
                )
        );

        System.out.println(
                "References: "
                        + size(
                        cubeSchema.getReferences()
                )
        );

        System.out.println();
    }

    private static void printDimension(
            DimensionSchema dimension
    ) {

        if (dimension == null) {
            return;
        }

        String dimensionName =
                safe(
                        dimension.getName()
                );

        System.out.println();

        System.out.println(
                "----------------------------------------"
        );

        System.out.println(
                "Dimension: "
                        + dimensionName
        );

        System.out.println(
                "----------------------------------------"
        );

        System.out.println(
                "Datasource: "
                        + safe(
                        dimension.getDataSource()
                )
        );

        System.out.println(
                "Type: "
                        + safe(
                        dimension.getDimensionType()
                )
        );

        /*
         * Hierarchy
         */

        System.out.println();

        System.out.println(
                "Hierarchy:"
        );

        List<String> hierarchy =
                dimension.getHierarchy();

        if (hierarchy == null
                || hierarchy.isEmpty()) {

            System.out.println(
                    "  [none]"
            );

        } else {

            for (int i = 0;
                 i < hierarchy.size();
                 i++) {

                System.out.println(
                        "  "
                                + i
                                + " -> "
                                + safe(
                                hierarchy.get(i)
                        )
                );
            }
        }

        /*
         * Levels
         */

        System.out.println();

        System.out.println(
                "Levels:"
        );

        List<LevelSchema> levels =
                dimension.getLevels();

        if (levels == null
                || levels.isEmpty()) {

            System.out.println(
                    "  [none]"
            );

            return;
        }

        for (LevelSchema level
                : levels) {

            if (level == null) {
                continue;
            }

            System.out.println();

            System.out.println(
                    "  Level: "
                            + safe(
                            level.getLevelName()
                    )
            );

            System.out.println(
                    "    id: "
                            + safe(
                            level.getId()
                    )
            );

            System.out.println(
                    "    description: "
                            + safe(
                            level.getDescription()
                    )
            );

            System.out.println(
                    "    attributes:"
            );

            List<LevelAttributeSchema> attributes =
                    level.getAttributes();

            if (attributes == null
                    || attributes.isEmpty()) {

                System.out.println(
                        "      [none]"
                );

                continue;
            }

            for (LevelAttributeSchema attribute
                    : attributes) {

                if (attribute == null) {
                    continue;
                }

                String fullField =
                        dimensionName
                                + "."
                                + safe(
                                attribute.getName()
                        );

                System.out.println(
                        "      - "
                                + fullField
                );

                System.out.println(
                        "        type: "
                                + safe(
                                attribute.getType()
                        )
                );

                System.out.println(
                        "        datasource: "
                                + safe(
                                attribute.getDataSource()
                        )
                );
            }
        }

        System.out.println();
    }

    private static int size(
            List<?> values
    ) {

        if (values == null) {
            return 0;
        }

        return values.size();
    }

    private static String safe(
            String value
    ) {

        if (value == null) {
            return "";
        }

        return value.trim();
    }
}