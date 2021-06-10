package xyz.cyanclay.poststudent.entity.iclass;

import java.util.HashMap;
import java.util.Map;

public class IClassActivityFilter {

    private String attribute;
    private String name;
    private boolean isExclusive = false;

    public Map<String, String> choices = new HashMap<>();

    public Map<String, String> getChoices() {
        return choices;
    }

    public String getAttribute() {
        return attribute;
    }

    public String getName() {
        return name;
    }

    public boolean isExclusive() {
        return isExclusive;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public void setChoices(Map<String, String> choices) {
        this.choices = choices;
    }

    public void setExclusive(boolean exclusive) {
        isExclusive = exclusive;
    }

    public void setName(String name) {
        this.name = name;
    }
}
