package LLM.schema;

import java.util.ArrayList;
import java.util.List;

public class CubeSchema {

    private String dataSourceType;
    private String dbcIniPath;
    private String cubeName;
    private String cubeDataSource;

    private List<DimensionSchema> dimensions = new ArrayList<>();
    private List<MeasureSchema> measures = new ArrayList<>();
    private List<CubeReference> references = new ArrayList<>();

    public String getDataSourceType() {
        return dataSourceType;
    }

    public void setDataSourceType(String dataSourceType) {
        this.dataSourceType = dataSourceType;
    }

    public String getDbcIniPath() {
        return dbcIniPath;
    }

    public void setDbcIniPath(String dbcIniPath) {
        this.dbcIniPath = dbcIniPath;
    }

    public String getCubeName() {
        return cubeName;
    }

    public void setCubeName(String cubeName) {
        this.cubeName = cubeName;
    }

    public String getCubeDataSource() {
        return cubeDataSource;
    }

    public void setCubeDataSource(String cubeDataSource) {
        this.cubeDataSource = cubeDataSource;
    }

    public List<DimensionSchema> getDimensions() {
        return dimensions;
    }

    public void setDimensions(List<DimensionSchema> dimensions) {
        this.dimensions = dimensions;
    }

    public List<MeasureSchema> getMeasures() {
        return measures;
    }

    public void setMeasures(List<MeasureSchema> measures) {
        this.measures = measures;
    }

    public List<CubeReference> getReferences() {
        return references;
    }

    public void setReferences(List<CubeReference> references) {
        this.references = references;
    }

    public void addDimension(DimensionSchema dimension) {
        this.dimensions.add(dimension);
    }

    public void addMeasure(MeasureSchema measure) {
        this.measures.add(measure);
    }

    public void addReference(CubeReference reference) {
        this.references.add(reference);
    }
}