package LLM.extractor;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import LLM.schema.CubeReference;
import LLM.schema.CubeSchema;
import LLM.schema.DimensionSchema;
import LLM.schema.LevelAttributeSchema;
import LLM.schema.LevelSchema;
import LLM.schema.MeasureSchema;

public class CubeIniSchemaExtractor {

    public CubeSchema extractFromFile(String filePath) throws IOException {
        CubeSchemaFileLoader loader = new CubeSchemaFileLoader();
        String content = loader.loadFileContent(filePath);
        return extractFromContent(content);
    }

    public CubeSchema extractFromContent(String content) {
        if (content == null) {
            throw new IllegalArgumentException("INI content cannot be null.");
        }

        CubeSchema schema = new CubeSchema();

        extractHeader(content, schema);
        extractDimensions(content, schema);
        extractCube(content, schema);

        return schema;
    }

    private void extractHeader(String content, CubeSchema schema) {
        Pattern headerPattern = Pattern.compile(
                "DATASOURCE\\s+TYPE:\\s*(.*?)\\s+WITH\\s+INI\\s+FILE:\\s*(.*?);",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );

        Matcher matcher = headerPattern.matcher(content);
        if (matcher.find()) {
            schema.setDataSourceType(safeTrim(matcher.group(1)));
            schema.setDbcIniPath(safeTrim(matcher.group(2)));
        }
    }

    private void extractDimensions(String content, CubeSchema schema) {
        Pattern dimensionPattern = Pattern.compile(
                "CREATE\\s+DIMENSION\\s+(\\w+)\\s+"
                        + "LIST\\s+OF\\s+LEVELS\\s*\\{(.*?)\\}\\s*"
                        + "HIERARCHY\\s+(.*?)\\s*"
                        + "(?:DIMENSION_TYPE\\s+(\\w+)\\s*)?"
                        + "DATASOURCE\\s+(\\w+)\\s*;",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );

        Matcher matcher = dimensionPattern.matcher(content);

        while (matcher.find()) {
            String dimensionName = safeTrim(matcher.group(1));
            String levelsBlock = matcher.group(2);
            String hierarchyBlock = safeTrim(matcher.group(3));
            String dimensionType = matcher.group(4);
            String dataSource = safeTrim(matcher.group(5));

            DimensionSchema dimension = new DimensionSchema();
            dimension.setName(dimensionName);
            dimension.setDataSource(dataSource);

            if (dimensionType != null) {
                dimension.setDimensionType(safeTrim(dimensionType));
            } else {
                dimension.setDimensionType("other");
            }

            extractLevels(levelsBlock, dimension);
            extractHierarchy(hierarchyBlock, dimension);

            schema.addDimension(dimension);
        }
    }

    private void extractLevels(String levelsBlock, DimensionSchema dimension) {
        Pattern levelPattern = Pattern.compile(
                "CREATE\\s+LEVEL\\s+(\\w+(?:\\.\\w+)?)\\s+"
                        + "WITH\\s+ATTRIBUTES\\s*\\{(.*?)\\}\\s*"
                        + "WITH\\s+ID:\\s*(\\w+(?:\\.\\w+)?)\\s+"
                        + "AND\\s+DESCRIPTION:\\s*(\\w+(?:\\.\\w+)?)",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );

        Matcher matcher = levelPattern.matcher(levelsBlock);

        while (matcher.find()) {
            LevelSchema level = new LevelSchema();

            level.setLevelName(safeTrim(matcher.group(1)));
            level.setId(safeTrim(matcher.group(3)));
            level.setDescription(safeTrim(matcher.group(4)));

            String attributesBlock = matcher.group(2);
            extractLevelAttributes(attributesBlock, level);

            dimension.addLevel(level);
        }
    }

    private void extractLevelAttributes(String attributesBlock, LevelSchema level) {
        Pattern attributePattern = Pattern.compile(
                "(\\w+(?:\\.\\w+)?)\\s*:\\s*(\\w+)\\s+DATASOURCE\\s+(\\w+(?:\\.\\w+)?)",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );

        Matcher matcher = attributePattern.matcher(attributesBlock);

        while (matcher.find()) {
            LevelAttributeSchema attribute = new LevelAttributeSchema();
            attribute.setName(safeTrim(matcher.group(1)));
            attribute.setType(safeTrim(matcher.group(2)));
            attribute.setDataSource(safeTrim(matcher.group(3)));

            level.addAttribute(attribute);
        }
    }

    private void extractHierarchy(String hierarchyBlock, DimensionSchema dimension) {
        if (hierarchyBlock == null || hierarchyBlock.trim().isEmpty()) {
            return;
        }

        String normalizedHierarchy = hierarchyBlock.replaceAll("\\s+", "");
        String[] parts = normalizedHierarchy.split(">");

        for (int i = 0; i < parts.length; i++) {
            if (parts[i] != null && parts[i].trim().length() > 0) {
                dimension.addHierarchyLevel(parts[i].trim());
            }
        }
    }

    private void extractCube(String content, CubeSchema schema) {
        Pattern cubePattern = Pattern.compile(
                "CREATE\\s+CUBE\\s+(\\w+)\\s+"
                        + "DATASOURCE\\s+(\\w+)\\s+"
                        + "MEASURES\\s+(.*?)\\s+"
                        + "REFERENCES\\s+DIMENSION\\s+(.*?)(?:;|$)",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );

        Matcher matcher = cubePattern.matcher(content);

        if (matcher.find()) {
            schema.setCubeName(safeTrim(matcher.group(1)));
            schema.setCubeDataSource(safeTrim(matcher.group(2)));

            String measuresBlock = matcher.group(3);
            String referencesBlock = matcher.group(4);

            extractMeasures(measuresBlock, schema);
            extractReferences(referencesBlock, schema);
        }
    }

    private void extractMeasures(String measuresBlock, CubeSchema schema) {
        Pattern measurePattern = Pattern.compile(
                "(\\w+)\\s+AT\\s+([\\w\\.]+)",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = measurePattern.matcher(measuresBlock);

        while (matcher.find()) {
            MeasureSchema measure = new MeasureSchema();
            measure.setName(safeTrim(matcher.group(1)));
            measure.setSource(safeTrim(matcher.group(2)));

            schema.addMeasure(measure);
        }
    }

    private void extractReferences(String referencesBlock, CubeSchema schema) {
        Pattern refPattern = Pattern.compile(
                "(\\w+)\\s+AT\\s+([\\w\\.]+)",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = refPattern.matcher(referencesBlock);

        while (matcher.find()) {
            CubeReference reference = new CubeReference();
            reference.setDimensionName(safeTrim(matcher.group(1)));
            reference.setCubeField(safeTrim(matcher.group(2)));

            schema.addReference(reference);
        }
    }

    private String safeTrim(String value) {
        if (value == null) {
            return null;
        }
        return value.trim();
    }
}