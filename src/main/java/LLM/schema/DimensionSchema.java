package LLM.schema;

import java.util.ArrayList;
import java.util.List;

public class DimensionSchema {

    private String name;
    private String dataSource;
    private String dimensionType;
    private List<LevelSchema> levels = new ArrayList<LevelSchema>();
    private List<String> hierarchy = new ArrayList<String>();

    public DimensionSchema() {
    }

    public DimensionSchema(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public String getDimensionType() {
        return dimensionType;
    }

    public void setDimensionType(String dimensionType) {
        this.dimensionType = dimensionType;
    }

    public List<LevelSchema> getLevels() {
        return levels;
    }

    public void setLevels(List<LevelSchema> levels) {
        this.levels = levels;
    }

    public List<String> getHierarchy() {
        return hierarchy;
    }

    public void setHierarchy(List<String> hierarchy) {
        this.hierarchy = hierarchy;
    }

    public void addLevel(LevelSchema level) {
        this.levels.add(level);
    }

    public void addHierarchyLevel(String levelName) {
        this.hierarchy.add(levelName);
    }
}