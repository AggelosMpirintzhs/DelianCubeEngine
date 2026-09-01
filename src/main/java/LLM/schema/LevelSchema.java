package LLM.schema;

import java.util.ArrayList;
import java.util.List;

public class LevelSchema {

    private String levelName;
    private String id;
    private String description;
    private List<LevelAttributeSchema> attributes = new ArrayList<LevelAttributeSchema>();

    public String getLevelName() {
        return levelName;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<LevelAttributeSchema> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<LevelAttributeSchema> attributes) {
        this.attributes = attributes;
    }

    public void addAttribute(LevelAttributeSchema attribute) {
        this.attributes.add(attribute);
    }
}