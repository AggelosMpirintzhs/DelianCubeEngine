package LLM.schema;

public class CubeReference {

    private String dimensionName;
    private String cubeField;

    public CubeReference() {
    }

    public CubeReference(String dimensionName, String cubeField) {
        this.dimensionName = dimensionName;
        this.cubeField = cubeField;
    }

    public String getDimensionName() {
        return dimensionName;
    }

    public void setDimensionName(String dimensionName) {
        this.dimensionName = dimensionName;
    }

    public String getCubeField() {
        return cubeField;
    }

    public void setCubeField(String cubeField) {
        this.cubeField = cubeField;
    }
}