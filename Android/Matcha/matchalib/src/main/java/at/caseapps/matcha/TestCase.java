package at.caseapps.matcha;

import java.util.ArrayList;
import java.util.List;

public class TestCase {
    public String fileName;
    public String name;

    public List<Scenario> scenarios = new ArrayList<>();

    public TestCase(String fileName, String name) {
        this.fileName = fileName;
        this.name = name;
    }
}
