package LLM.prompts;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import LLM.schema.CubeSchema;
import LLM.schema.DimensionSchema;
import LLM.schema.LevelAttributeSchema;
import LLM.schema.LevelSchema;
import LLM.schema.MeasureSchema;

public class CubeSchemaPromptFormatterCompact {

    public String format(CubeSchema cubeSchema) {

        if (cubeSchema == null) {
            throw new IllegalArgumentException(
                    "CubeSchema cannot be null."
            );
        }

        StringBuilder prompt = new StringBuilder();

        appendCube(prompt, cubeSchema);
        appendMeasures(prompt, cubeSchema);
        appendDimensions(prompt, cubeSchema);

        return prompt.toString();
    }

    private void appendCube(
            StringBuilder prompt,
            CubeSchema cubeSchema) {

        prompt.append("Cube:\n");
        prompt.append(safe(cubeSchema.getCubeName()));
        prompt.append("\n\n");
    }

    private void appendMeasures(
            StringBuilder prompt,
            CubeSchema cubeSchema) {

        prompt.append("Measures:\n");

        List<MeasureSchema> measures = cubeSchema.getMeasures();

        if (measures != null) {

            for (MeasureSchema measure : measures) {

                if (measure == null) {
                    continue;
                }

                String measureName = safe(measure.getName());

                if (measureName.isEmpty()) {
                    continue;
                }

                prompt.append("- ");
                prompt.append(measureName);
                prompt.append("\n");
            }
        }

        prompt.append("\n");
    }

    private void appendDimensions(
            StringBuilder prompt,
            CubeSchema cubeSchema) {

        prompt.append("Dimensions:\n\n");

        List<DimensionSchema> dimensions =
                cubeSchema.getDimensions();

        if (dimensions == null) {
            return;
        }

        for (DimensionSchema dimension : dimensions) {

            if (dimension == null) {
                continue;
            }

            appendDimension(prompt, dimension);
        }
    }

    private void appendDimension(
            StringBuilder prompt,
            DimensionSchema dimension) {

        String dimensionName = safe(dimension.getName());

        if (dimensionName.isEmpty()) {
            return;
        }

        prompt.append(dimensionName);
        prompt.append("\n");

        String hierarchy =
                buildCompactHierarchy(dimension);

        if (!hierarchy.isEmpty()) {
            prompt.append("hierarchy: ");
            prompt.append(hierarchy);
            prompt.append("\n");
        }

        prompt.append("levels:\n");

        List<LevelSchema> levels =
                dimension.getLevels();

        if (levels != null) {

            for (LevelSchema level : levels) {

                if (shouldSkipLevel(level)) {
                    continue;
                }

                appendLevel(
                        prompt,
                        dimensionName,
                        level
                );
            }
        }

        prompt.append("\n");
    }

    private void appendLevel(
            StringBuilder prompt,
            String dimensionName,
            LevelSchema level) {

        String levelName =
                safe(level.getLevelName());

        if (levelName.isEmpty()) {
            return;
        }

        List<String> fields =
                collectFieldsForLevel(
                        dimensionName,
                        level
                );

        if (fields.isEmpty()) {
            return;
        }

        prompt.append("- ");
        prompt.append(levelName);
        prompt.append(" -> ");

        for (int i = 0; i < fields.size(); i++) {

            prompt.append(fields.get(i));

            if (i < fields.size() - 1) {
                prompt.append(", ");
            }
        }

        prompt.append("\n");
    }

    private List<String> collectFieldsForLevel(
            String dimensionName,
            LevelSchema level) {

        Set<String> fields =
                new LinkedHashSet<String>();

        List<LevelAttributeSchema> attributes =
                level.getAttributes();

        if (attributes == null) {
            return new ArrayList<String>(fields);
        }

        for (LevelAttributeSchema attribute : attributes) {

            if (attribute == null) {
                continue;
            }

            String attributeName =
                    safe(attribute.getName());

            if (attributeName.isEmpty()) {
                continue;
            }

            fields.add(
                    dimensionName
                            + "."
                            + attributeName
            );
        }

        return new ArrayList<String>(fields);
    }

    private String buildCompactHierarchy(
            DimensionSchema dimension) {

        List<String> hierarchy =
                dimension.getHierarchy();

        if (hierarchy == null ||
                hierarchy.isEmpty()) {

            return "";
        }

        StringBuilder builder =
                new StringBuilder();

        boolean first = true;

        for (String levelName : hierarchy) {

            if (isEmpty(levelName)) {
                continue;
            }

            /*
             * "All" levels are normally implementation-level
             * hierarchy nodes and are not useful for query
             * generation.
             */
            if (isAllLevelName(levelName)) {
                continue;
            }

            if (!first) {
                builder.append(" > ");
            }

            builder.append(levelName.trim());

            first = false;
        }

        return builder.toString();
    }

    private boolean shouldSkipLevel(
            LevelSchema level) {

        if (level == null) {
            return true;
        }

        String levelName =
                safe(level.getLevelName());

        if (levelName.isEmpty()) {
            return true;
        }

        return isAllLevelName(levelName);
    }

    private boolean isAllLevelName(
            String levelName) {

        if (levelName == null) {
            return false;
        }

        String normalized =
                levelName.trim().toLowerCase();

        return normalized.equals("all")
                || normalized.startsWith("all_")
                || normalized.endsWith("_all");
    }

    private boolean isEmpty(String value) {
        return value == null
                || value.trim().isEmpty();
    }

    private String safe(String value) {

        if (value == null) {
            return "";
        }

        return value.trim();
    }
}