package LLM.prompts;

import java.util.LinkedHashSet;
import java.util.Set;

import LLM.schema.CubeSchema;
import LLM.schema.DimensionSchema;
import LLM.schema.LevelAttributeSchema;
import LLM.schema.LevelSchema;
import LLM.schema.MeasureSchema;

public class CubeSchemaPromptFormatterPartiallyFiltered {

    public String format(CubeSchema cubeSchema) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Cube:\n");
        prompt.append(cubeSchema.getCubeName()).append("\n\n");

        prompt.append("Measures:\n");
        appendMeasures(prompt, cubeSchema);
        prompt.append("\n\n");

        prompt.append("Allowed fields:\n");
        appendAllowedFields(prompt, cubeSchema);

        return prompt.toString();
    }

    private void appendMeasures(StringBuilder prompt, CubeSchema cubeSchema) {
        for (int i = 0; i < cubeSchema.getMeasures().size(); i++) {
            MeasureSchema measure = cubeSchema.getMeasures().get(i);

            prompt.append(measure.getName());

            if (i < cubeSchema.getMeasures().size() - 1) {
                prompt.append(", ");
            }
        }
    }

    private void appendAllowedFields(StringBuilder prompt, CubeSchema cubeSchema) {
        for (DimensionSchema dimension : cubeSchema.getDimensions()) {
            String dimensionName = dimension.getName();

            Set<String> fields = collectDimensionFields(dimension);

            if (fields.isEmpty()) {
                continue;
            }

            prompt.append("\n");
            prompt.append(dimensionName).append(":\n");

            for (String field : fields) {
                prompt.append(field).append("\n");
            }
        }
    }

    private Set<String> collectDimensionFields(DimensionSchema dimension) {
        Set<String> fields = new LinkedHashSet<String>();

        for (LevelSchema level : dimension.getLevels()) {
            for (LevelAttributeSchema attribute : level.getAttributes()) {
                String attributeName = attribute.getName();

                if (shouldSkipAttribute(attributeName)) {
                    continue;
                }

                fields.add(dimension.getName() + "." + attributeName);
            }
        }

        return fields;
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
         * Κόβουμε πολύ τεχνικά πεδία που συνήθως δεν βοηθούν το LLM
         * για gamma/sigma.
         *
         * Αν αργότερα θέλεις 100% πλήρες schema, απλώς κάνε return false.
         */
        if ("all".equals(normalized)) {
            return true;
        }

        if (normalized.endsWith("_id")) {
            return true;
        }

        if ("id".equals(normalized)) {
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
}