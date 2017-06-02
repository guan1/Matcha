package at.caseapps.matcha;

import java.util.ArrayList;
import java.util.List;

public class Scenario {
    public String name;
    public List<Action> preconditions = new ArrayList<>();
    public List<Action> steps = new ArrayList<>();

    public Scenario(String name) {
        this.name = name;
    }
}
