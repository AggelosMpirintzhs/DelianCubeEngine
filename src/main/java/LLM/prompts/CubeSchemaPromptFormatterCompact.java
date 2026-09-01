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
        StringBuilder prompt = new StringBuilder();

        prompt.append("Cube:\n");
        prompt.append(safe(cubeSchema.getCubeName())).append("\n\n");

        prompt.append("Measures:\n");
        appendMeasures(prompt, cubeSchema);
        prompt.append("\n\n");

        prompt.append("Dimensions:\n\n");
        appendDimensions(prompt, cubeSchema);

        return prompt.toString();
    }

    private void appendMeasures(StringBuilder prompt, CubeSchema cubeSchema) {
        List<MeasureSchema> measures = cubeSchema.getMeasures();

        for (int i = 0; i < measures.size(); i++) {
            MeasureSchema measure = measures.get(i);

            prompt.append(safe(measure.getName()));

            if (i < measures.size() - 1) {
                prompt.append(", ");
            }
        }
    }

    private void appendDimensions(StringBuilder prompt, CubeSchema cubeSchema) {
        List<DimensionSchema> dimensions = cubeSchema.getDimensions();

        for (DimensionSchema dimension : dimensions) {
            appendDimension(prompt, dimension);
            prompt.append("\n");
        }
    }

    private void appendDimension(StringBuilder prompt, DimensionSchema dimension) {
        String dimensionName = safe(dimension.getName());

        prompt.append(dimensionName).append("\n");

        if (!isEmpty(dimension.getDataSource())) {
            prompt.append("datasource: ").append(safe(dimension.getDataSource())).append("\n");
        }

        if (!isEmpty(dimension.getDimensionType())) {
            prompt.append("type: ").append(safe(dimension.getDimensionType())).append("\n");
        }

        String hierarchyText = buildCompactHierarchy(dimension);

        if (!hierarchyText.isEmpty()) {
            prompt.append("hierarchy: ").append(hierarchyText).append("\n");
        }

        for (LevelSchema level : dimension.getLevels()) {
            if (shouldSkipLevel(level)) {
                continue;
            }

            appendLevel(prompt, dimensionName, level);
        }
    }

    private void appendLevel(StringBuilder prompt, String dimensionName, LevelSchema level) {
        List<String> allowedFields = collectAllowedFieldsForLevel(dimensionName, level);

        if (allowedFields.isEmpty()) {
            return;
        }

        prompt.append("level ");
        prompt.append(safe(level.getLevelName()));
        prompt.append(": ");

        for (int i = 0; i < allowedFields.size(); i++) {
            prompt.append(allowedFields.get(i));

            if (i < allowedFields.size() - 1) {
                prompt.append(", ");
            }
        }

        prompt.append("\n");
    }

    private List<String> collectAllowedFieldsForLevel(String dimensionName, LevelSchema level) {
        Set<String> fields = new LinkedHashSet<String>();

        for (LevelAttributeSchema attribute : level.getAttributes()) {
            if (attribute == null) {
                continue;
            }

            String attributeName = safe(attribute.getName());

            if (shouldSkipAttribute(attributeName)) {
                continue;
            }

            fields.add(dimensionName + "." + attributeName);
        }

        return new ArrayList<String>(fields);
    }

    private String buildCompactHierarchy(DimensionSchema dimension) {
        List<String> hierarchy = dimension.getHierarchy();

        if (hierarchy == null || hierarchy.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();

        boolean first = true;

        for (String levelName : hierarchy) {
            if (isEmpty(levelName)) {
                continue;
            }

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

    private boolean shouldSkipLevel(LevelSchema level) {
        if (level == null) {
            return true;
        }

        String levelName = safe(level.getLevelName());

        return isAllLevelName(levelName);
    }

    private boolean shouldSkipAttribute(String attributeName) {
        if (attributeName == null) {
            return true;
        }

        String normalized = attributeName.trim().toLowerCase();

        if (normalized.isEmpty()) {
            return true;
        }

        /*
         * Κρατάμε το schema μικρό.
         * Αφαιρούμε κυρίως τεχνικά ή επικοινωνιακά πεδία
         * που σπάνια χρειάζονται σε gamma/sigma.
         *
         * Δεν αφαιρούμε semantic fields όπως:
         * category, family, type, city, state, country, year, month,
         * quarter, gender, income, media, status, low_fat κλπ.
         */
        if ("all".equals(normalized)) {
            return true;
        }

        if ("id".equals(normalized)) {
            return true;
        }

        if (normalized.endsWith("_id")) {
            return true;
        }

        if (normalized.contains("phone")) {
            return true;
        }

        if (normalized.contains("fax")) {
            return true;
        }

        if (normalized.contains("address")) {
            return true;
        }

        if (normalized.contains("postal")) {
            return true;
        }

        if (normalized.contains("account")) {
            return true;
        }

        return false;
    }

    private boolean isAllLevelName(String levelName) {
        if (levelName == null) {
            return false;
        }

        String normalized = levelName.trim().toLowerCase();

        return normalized.equals("all")
                || normalized.startsWith("all_")
                || normalized.endsWith("_all");
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String safe(String value) {
        if (value == null) {
            return "";
        }

        return value.trim();
    }
}