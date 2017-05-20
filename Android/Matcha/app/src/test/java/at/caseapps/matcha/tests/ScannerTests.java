package at.caseapps.matcha.tests;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import at.caseapps.matcha.Action;
import at.caseapps.matcha.Scenario;
import at.caseapps.matcha.TestCase;

import static at.caseapps.matcha.Action.EnterAction;
import static at.caseapps.matcha.Action.ExecuteJSAction;
import static at.caseapps.matcha.Action.HttpAction;
import static at.caseapps.matcha.Action.ScrollAction;
import static at.caseapps.matcha.Action.SearchFieldAction;
import static at.caseapps.matcha.Action.VerifyAction;
import static at.caseapps.matcha.Action.VerifyNavigationAction;
import static at.caseapps.matcha.Action.WaitAction;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ScannerTests {

    private int i = 0;

    @Test
    public void testScanner() throws IOException {
        String fileName = "ScannerTests.tc";

        InputStream resourceAsStream = getClass().getResourceAsStream(fileName);
        at.caseapps.matcha.tests.Scanner scanner = new at.caseapps.matcha.tests.Scanner(resourceAsStream, fileName);
        if (scanner.getTestCase() != null) {
            TestCase testCase = scanner.getTestCase();
            assertEquals(fileName, testCase.fileName);
            assertEquals("Scanner Tests", testCase.name);
            assertEquals(2, testCase.scenarios.size());
            Scenario scenario1 = testCase.scenarios.get(0);
            assertEquals("Test Scenario 1", scenario1.name);
            assertTrue(scenario1.steps.size() > 0);
            assertEquals(scenario1.preconditions.get(0).name, "preconditionAction");
            assertEquals(4, scenario1.preconditions.get(0).line);
            
            /*
            @http {
                url='url',
                headers = 'header1,header2',
                params = 'param1,param2'
            }
            */
            HttpAction httpAction1 = (HttpAction) next(scenario1.steps);
            assertEquals("http", httpAction1.name);
            assertEquals("url", httpAction1.url);

            //@verify element.id { value: 'Harald Schmidt'}
            VerifyAction verify1 = (VerifyAction) next(scenario1.steps);
            ;
            assertEquals("verify", verify1.name);
            assertEquals("element.id", verify1.element);
            assertEquals("Harald Schmidt", verify1.value);

            //@verify element.id 'Harald Schmidt'
            VerifyAction verify2 = (VerifyAction) next(scenario1.steps);
            ;
            assertEquals("element.id", verify2.element);
            assertEquals("Harald Schmidt", verify2.value);

            //@verify element.id
            VerifyAction verify3 = (VerifyAction) next(scenario1.steps);
            assertEquals("element.id", verify3.element);
            assertNull(verify3.value);

            //@scroll element.id { to: target.id, direction: right, amount: 500 }
            ScrollAction scroll1 = (ScrollAction) next(scenario1.steps);
            assertEquals("element.id", scroll1.element);
            assertEquals("target.id", scroll1.to);
            assertEquals("right", scroll1.direction);
            assertEquals(1500.0f, scroll1.amount, 0);

            //@scroll element.id       { to: target.id, direction: left }
            ScrollAction scroll2 = (ScrollAction) next(scenario1.steps);
            assertEquals("element.id", scroll2.element);
            assertEquals("target.id", scroll2.to);
            assertEquals("left", scroll2.direction);
            assertEquals(500.0f, scroll2.amount, 0);
            
            /*@scroll element.id {
                to: target.id,
                direction: left
            }*/
            ScrollAction scroll3 = (ScrollAction) next(scenario1.steps);
            assertEquals("scroll", scroll3.name);
            assertEquals("element.id", scroll3.element);
            assertEquals("target.id", scroll3.to);
            assertEquals("left", scroll3.direction);


            //@executeJS element.id "documents.form"
            ExecuteJSAction executeJS1 = (ExecuteJSAction) next(scenario1.steps);
            assertEquals("executeJS", executeJS1.name);
            assertEquals("element.id", executeJS1.element);
            assertEquals("document.forms['credentials'].j_username.value = '309405863';", executeJS1.code);

            //@executeJS element.id { code = "documents.form" }
            ExecuteJSAction executeJS2 = (ExecuteJSAction) next(scenario1.steps);
            assertEquals("element.id", executeJS2.element);
            assertEquals("document.forms['credentials'].j_username.value = '309405863';", executeJS2.code);

            //@customElement
            Action customElement1 = next(scenario1.steps);
            assertEquals("customElement", customElement1.name);
            assertNull(customElement1.parameters);

            //@customElement { param : value}
            Action customElement2 = next(scenario1.steps);
            assertEquals("customElement", customElement2.name);
            assertEquals(1, customElement2.parameters.size());

            //@click element.id
            Action click1 = next(scenario1.steps);
            assertEquals("click", click1.name);
            assertEquals("element.id", click1.element);

            //@enter element.id 'value'
            EnterAction enter1 = (EnterAction) next(scenario1.steps);
            assertEquals("element.id", enter1.element);
            assertEquals("value", enter1.value);

            //@enter element.id  { value = 'value'  }
            EnterAction enter2 = (EnterAction) next(scenario1.steps);
            assertEquals("enter", enter2.name);
            assertEquals("element.id", enter2.element);
            assertEquals("value", enter2.value);

            //@back
            Action back1 = next(scenario1.steps);
            assertEquals("back", back1.name);

            //@verify { navigationTitle : 'Dashboard' }
            VerifyNavigationAction verify4 = (VerifyNavigationAction) next(scenario1.steps);
            assertEquals("Dashboard", verify4.navigationTitle);

            VerifyAction contains1 = (VerifyAction) next(scenario1.steps);
            assertEquals("element.id", contains1.element);
            assertEquals("Harald Schneemann", contains1.value);

            WaitAction wait1 = (WaitAction) next(scenario1.steps);
            assertEquals("wait", wait1.name);
            assertEquals(0.5, wait1.value, 0);

            WaitAction wait2 = (WaitAction) next(scenario1.steps);
            assertEquals("wait", wait2.name);
            assertEquals(2, wait2.value, 0);

            SearchFieldAction searchField1 = (SearchFieldAction) next(scenario1.steps);
            assertEquals("searchField", searchField1.name);
            assertEquals("Test", searchField1.value);

            Scenario scenario2 = testCase.scenarios.get(1);
            assertEquals("Test Scenario 2", scenario2.name);
            assertEquals(scenario2.preconditions.get(0).name, "preconditionAction");
            assertEquals(scenario2.steps.get(0).name, "verify");
            assertEquals(scenario2.steps.get(1).name, "back");
        } else {
            fail("parsing failed - no features found");
            ;
        }
    }

    private Action next(List<Action> actions) {
        Action a = actions.get(i);
        i++;
        return a;
    }
}
