package LLM.schema;

public class LevelAttributeSchema {

    private String name;
    private String type;
    private String dataSource;

    public LevelAttributeSchema() {
    }

    public LevelAttributeSchema(String name, String type, String dataSource) {
        this.name = name;
        this.type = type;
        this.dataSource = dataSource;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }
}