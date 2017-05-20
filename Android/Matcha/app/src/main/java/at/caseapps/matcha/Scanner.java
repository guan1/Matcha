package at.caseapps.matcha.tests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.caseapps.matcha.Action;
import at.caseapps.matcha.ScannerUtils;
import at.caseapps.matcha.Scenario;
import at.caseapps.matcha.TestCase;

import static at.caseapps.matcha.ScannerUtils.matcha_isValue;
import static at.caseapps.matcha.ScannerUtils.matcha_splitStep;
import static at.caseapps.matcha.ScannerUtils.matcha_splitToFirstSpace;
import static at.caseapps.matcha.ScannerUtils.matcha_trimValue;

public class Scanner {
    public TestCase testCase;

    public Scanner(InputStream inputStream, String fileName) throws IOException {
        List<Scenario> scenarios = new ArrayList<>();

        Scenario currentScenario = null;
        List<Action> currentPreconditions = null;
        List<Action> currentSteps = null;

        String currentParameterLines = null;
        String currentActionName = null;
        String currentFirstParameter = null;

        Integer lineNumber = null;

        String line;

        InputStreamReader isr = new InputStreamReader(inputStream);
        BufferedReader br = new BufferedReader(isr);

        while ((line = br.readLine()) != null) {
            String trimmedLine = line.trim();
            if (lineNumber == null) {
                lineNumber = 1;
            } else {
                lineNumber = lineNumber + 1;
            }

            if (trimmedLine.isEmpty()) {
                continue;
            }

            if (currentParameterLines != null) {
                currentParameterLines = currentParameterLines + trimmedLine;

                if (trimmedLine.equals("}")) {
                    Map<String, Object> parameters = parseParams(currentParameterLines);
                    Action action = Action.createAction(currentActionName, currentFirstParameter, parameters, lineNumber);
                    if (currentPreconditions != null) {
                        currentPreconditions.add(action);
                    } else if (currentSteps != null) {
                        currentSteps.add(action);
                    }
                    currentParameterLines = null;
                }
                continue;
            }

            String tcString = matches("TestCase:", trimmedLine);
            if (tcString != null) {
                testCase = new TestCase(fileName, tcString);
                continue;
            }

            String scenarioString = matches("Scenario:", trimmedLine);
            if (scenarioString != null) {
                if (currentPreconditions != null) {
                    currentScenario.preconditions = currentPreconditions;
                }

                if (currentSteps != null) {
                    currentScenario.steps = currentSteps;
                }

                currentScenario = new Scenario(scenarioString);
                scenarios.add(currentScenario);
                continue;
            }

            if (trimmedLine.startsWith("Preconditions:")) {
                if (currentSteps != null) {
                    currentScenario.steps = currentSteps;
                }
                currentSteps = null;
                currentPreconditions = new ArrayList<>();
                continue;
            }

            if (trimmedLine.startsWith("Steps:")) {
                if (currentPreconditions != null) {
                    currentScenario.preconditions = currentPreconditions;
                }
                currentSteps = new ArrayList<>();
                currentPreconditions = null;
                continue;
            }

            if (trimmedLine.startsWith("@")) {
                //Sample: @verify element { key : 'value' } -> [verify, "{key:value}"]
                ScannerUtils.Pair actionAndParams = matcha_splitStep(trimmedLine);

                //Sample: verify
                currentActionName = actionAndParams.first;

                currentFirstParameter = null;
                String value = null;

                Map<String, Object> parameters = new HashMap<>();


                if (actionAndParams.second != null) {
                    boolean isMultiLineParams = false;
                    String paramsAsString = actionAndParams.second;
                    if (paramsAsString.startsWith("{") && paramsAsString.endsWith("}") == false) {
                                /*multiline params; Sample: {
                                    key : 'value'
                                 }
                                */
                        currentParameterLines = paramsAsString;
                        continue;
                    } else if (paramsAsString.startsWith("{") && paramsAsString.endsWith("}")) {
                        //Sample: { key : 'value' }
                        parameters = parseParams(paramsAsString);
                    } else if (matcha_isValue(paramsAsString)) {
                        //Sample: 'value'
                        value = matcha_trimValue(paramsAsString);
                    } else {
                        //Sample: element { key : 'value' }
                        String[] paramsArray = matcha_splitToFirstSpace(paramsAsString);
                        if (paramsArray.length > 0) {
                            int i = 0;
                            for (String p1 : paramsArray) {
                                if (i == 0) {
                                    currentFirstParameter = p1;
                                    i = i + 1;
                                    continue;
                                }

                                String trimmedP = p1.trim();
                                if (trimmedP.startsWith("{") && trimmedP.endsWith("}") == false) {
                                            /*multiline params; Sample: {
                                                key : 'value'
                                             }
                                             */
                                    currentParameterLines = trimmedP;
                                    isMultiLineParams = true;
                                    break;
                                } else if (trimmedP.startsWith("{") && trimmedP.endsWith("}")) {
                                    //Sample: element { key : 'value' }
                                    parameters = parseParams(trimmedP);

                                } else if ((trimmedP.startsWith("'") && trimmedP.endsWith("'"))
                                        || (trimmedP.startsWith("\"") && trimmedP.endsWith("\""))) {
                                    //Sample: element 'value'
                                    value = matcha_trimValue(trimmedP);
                                }

                                i = i + 1;
                            }
                        }
                    }

                    if (isMultiLineParams) {
                        continue;
                    }
                }

                if (value != null) {
                    parameters.put("value", value);
                }

                Action action = Action.createAction(currentActionName, currentFirstParameter, parameters, lineNumber);
                if (currentPreconditions != null) {
                    currentPreconditions.add(action);
                } else if (currentSteps != null) {
                    currentSteps.add(action);
                }

            }
        }

        if (currentPreconditions != null) {
            currentScenario.preconditions = currentPreconditions;
        }

        if (currentSteps != null) {
            currentScenario.steps = currentSteps;
        }
        testCase.scenarios = scenarios;
    }

    //checks if line matches match and returns the 'value' -> if line is "Scenario: Test" and match = "Scenario:" this function returns "Test"
    private String matches(String match, String line) {
        if (line.trim().startsWith(match)) {
            return line.substring(match.length()).trim();
        } else {
            return null;
        }
    }

    // parses strings of format { p : v, p1 : 'v', p2 : "v"} and returns a map [ "p" : "v", "p1" : "v", "p2" : "v"]
    private Map<String, Object> parseParams(String string) {
        String key = "";
        String value;

        String currentToken = "";
        Map<String, Object> parameters = new HashMap<>();

        for (char c : string.toCharArray()) {
            if (c == '{') {
                continue;
            } else if (c == ':') {
                key = currentToken.trim();
                currentToken = "";
                continue;
            } else if (c == '}' || c == ',') {
                value = matcha_trimValue(currentToken);
                parameters.put(key, value.trim());
                currentToken = "";
                continue;
            }
            currentToken = currentToken + c;
        }
        return parameters;

    }

    public TestCase getTestCase() {
        return testCase;
    }
}

