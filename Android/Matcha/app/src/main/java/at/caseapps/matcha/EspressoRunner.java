package at.caseapps.matcha;

import android.app.Activity;
import android.content.Context;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.ViewAssertion;
import android.view.View;

import java.lang.reflect.Method;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;


/**
 * Created by andreguggenberger on 20/05/2017.
 */

public class EspressoRunner {
    private TestCase testCase;

    private Scenario currentRunningScenario;
    private Action currentRunningAction;

    public EspressoRunner(TestCase testCase) {
        this.testCase = testCase;
    }


    public void start() {
        for (Scenario scenario : testCase.scenarios) {
            currentRunningScenario = scenario;
            for (Action precondition : scenario.preconditions) {
                currentRunningAction = precondition;
                performAction(precondition);
            }

            for (Action action : scenario.steps) {
                currentRunningAction = action;
                performAction(action);
            }
        }
    }

    private void performAction(Action action) {
        try {
            Method m = getClass().getMethod(action.name, Action.class);
            m.invoke(this, action);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void verify(Action action) {
        Context context = getCurrentActivity();
        Action.VerifyAction verifyAction = (Action.VerifyAction) action;
        int id = context.getResources().getIdentifier("textView", "id", context.getPackageName());

        onView(withId(id)).check(matches(withText(verifyAction.value)));
    }

    private Context getCurrentActivity() {
        final Context[] context = new Context[1];
        onView(isRoot()).check(new ViewAssertion() {
            @Override
            public void check(View view, NoMatchingViewException noViewFoundException) {
                context[0] = view.getContext();
            }
        });
        return context[0];
    }
}
