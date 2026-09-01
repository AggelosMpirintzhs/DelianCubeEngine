package LLM.prompts;

import LLM.schema.CubeReference;
import LLM.schema.CubeSchema;
import LLM.schema.DimensionSchema;
import LLM.schema.LevelAttributeSchema;
import LLM.schema.LevelSchema;
import LLM.schema.MeasureSchema;

public class CubeSchemaPromptFormatterUnfiltered {

    public String format(CubeSchema cubeSchema) {
        if (cubeSchema == null) {
            throw new IllegalArgumentException("CubeSchema cannot be null.");
        }

        StringBuilder prompt = new StringBuilder();

        prompt.append("Cube schema:\n");
        prompt.append("\n");

        appendCubeBasicInfo(prompt, cubeSchema);
        appendMeasures(prompt, cubeSchema);
        appendDimensions(prompt, cubeSchema);
        appendReferences(prompt, cubeSchema);

        return prompt.toString();
    }

    private void appendCubeBasicInfo(StringBuilder prompt, CubeSchema cubeSchema) {
        prompt.append("cubeName: ").append(nullToEmpty(cubeSchema.getCubeName())).append("\n");
        prompt.append("cubeDataSource: ").append(nullToEmpty(cubeSchema.getCubeDataSource())).append("\n");
        prompt.append("dataSourceType: ").append(nullToEmpty(cubeSchema.getDataSourceType())).append("\n");
        prompt.append("dbcIniPath: ").append(nullToEmpty(cubeSchema.getDbcIniPath())).append("\n");
        prompt.append("\n");
    }

    private void appendMeasures(StringBuilder prompt, CubeSchema cubeSchema) {
        prompt.append("Measures:\n");

        if (cubeSchema.getMeasures() == null || cubeSchema.getMeasures().isEmpty()) {
            prompt.append("- none\n");
            prompt.append("\n");
            return;
        }

        for (MeasureSchema measure : cubeSchema.getMeasures()) {
            prompt.append("- name: ").append(nullToEmpty(measure.getName()));
            prompt.append(", source: ").append(nullToEmpty(measure.getSource()));
            prompt.append("\n");
        }

        prompt.append("\n");
    }

    private void appendDimensions(StringBuilder prompt, CubeSchema cubeSchema) {
        prompt.append("Dimensions:\n");

        if (cubeSchema.getDimensions() == null || cubeSchema.getDimensions().isEmpty()) {
            prompt.append("- none\n");
            prompt.append("\n");
            return;
        }

        for (DimensionSchema dimension : cubeSchema.getDimensions()) {
            prompt.append("- dimensionName: ").append(nullToEmpty(dimension.getName())).append("\n");
            prompt.append("  dataSource: ").append(nullToEmpty(dimension.getDataSource())).append("\n");
            prompt.append("  dimensionType: ").append(nullToEmpty(dimension.getDimensionType())).append("\n");

            appendHierarchy(prompt, dimension);
            appendLevels(prompt, dimension);

            prompt.append("\n");
        }
    }

    private void appendHierarchy(StringBuilder prompt, DimensionSchema dimension) {
        prompt.append("  hierarchy: ");

        if (dimension.getHierarchy() == null || dimension.getHierarchy().isEmpty()) {
            prompt.append("none");
        } else {
            for (int i = 0; i < dimension.getHierarchy().size(); i++) {
                if (i > 0) {
                    prompt.append(" > ");
                }
                prompt.append(dimension.getHierarchy().get(i));
            }
        }

        prompt.append("\n");
    }

    private void appendLevels(StringBuilder prompt, DimensionSchema dimension) {
        prompt.append("  levels:\n");

        if (dimension.getLevels() == null || dimension.getLevels().isEmpty()) {
            prompt.append("  - none\n");
            return;
        }

        for (LevelSchema level : dimension.getLevels()) {
            prompt.append("  - levelName: ").append(nullToEmpty(level.getLevelName())).append("\n");
            prompt.append("    id: ").append(nullToEmpty(level.getId())).append("\n");
            prompt.append("    description: ").append(nullToEmpty(level.getDescription())).append("\n");

            appendLevelAttributes(prompt, level);
        }
    }

    private void appendLevelAttributes(StringBuilder prompt, LevelSchema level) {
        prompt.append("    attributes:\n");

        if (level.getAttributes() == null || level.getAttributes().isEmpty()) {
            prompt.append("    - none\n");
            return;
        }

        for (LevelAttributeSchema attribute : level.getAttributes()) {
            prompt.append("    - name: ").append(nullToEmpty(attribute.getName()));
            prompt.append(", type: ").append(nullToEmpty(attribute.getType()));
            prompt.append(", dataSource: ").append(nullToEmpty(attribute.getDataSource()));
            prompt.append("\n");
        }
    }

    private void appendReferences(StringBuilder prompt, CubeSchema cubeSchema) {
        prompt.append("References:\n");

        if (cubeSchema.getReferences() == null || cubeSchema.getReferences().isEmpty()) {
            prompt.append("- none\n");
            prompt.append("\n");
            return;
        }

        for (CubeReference reference : cubeSchema.getReferences()) {
            prompt.append("- dimensionName: ").append(nullToEmpty(reference.getDimensionName()));
            prompt.append(", cubeField: ").append(nullToEmpty(reference.getCubeField()));
            prompt.append("\n");
        }

        prompt.append("\n");
    }

    private String nullToEmpty(String value) {
        if (value == null) {
            return "";
        }

        return value;
    }
}