package at.caseapps.matcha;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.PerformException;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.ViewAssertion;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.espresso.util.HumanReadables;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Assert;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;


/**
 * Created by andreguggenberger on 20/05/2017.
 */

public class EspressoRunner {
    private final AtomicBoolean webViewEvaluationFinished = new AtomicBoolean(false);

    private TestCase testCase;

    private Scenario currentRunningScenario;
    private Action currentRunningAction;

    private EarlGreyTestRunnerDelegate delegate;

    public EspressoRunner(TestCase testCase, EarlGreyTestRunnerDelegate delegate) {
        this.testCase = testCase;
        this.delegate = delegate;
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
        if(delegate != null) {
            try {
                delegate.handles(action);
            }  catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                //delegate cannot handle this action
                try {
                    Method m = getClass().getMethod(action.name, Action.class);
                    m.invoke(this, action);
                }catch(Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    @SuppressWarnings("unused")
    public void http(Action action) {
        //TODO:
    }

    @SuppressWarnings("unused")
    public void back(Action action) {
        final Activity activity = delegate.getCurrentActivity();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.onBackPressed();
            }
        });
    }

    @SuppressWarnings("unused")
    public void scroll(Action action) {
        Context context = getCurrentContext();
        Action.ScrollAction scrollAction = (Action.ScrollAction) action;
        int id = context.getResources().getIdentifier(action.element, "id", context.getPackageName());

    //    onView(withId(id)).perform(ViewActions.scrollTo()).check(ViewAssertions.matches(isDisplayed()));

        int x = 0;
        int y = 0;

        if(scrollAction.direction.equals("right")) {
            x = scrollAction.amount;
        } else if(scrollAction.direction.equals("left")) {
            x = scrollAction.amount * -1;
        } else if(scrollAction.direction.equals("up")) {
            y = scrollAction.amount * -1;
        } else if(scrollAction.direction.equals("down")) {
            y = scrollAction.amount;
        }

        onView(withId(id)).perform(new XYScrollByPositionViewAction(x,y));
    }

    @SuppressWarnings("unused")
    public void click(Action action) {
        Context context = getCurrentContext();
        int id = context.getResources().getIdentifier(action.element, "id", context.getPackageName());

        onView(withId(id)).perform(ViewActions.click());
    }
    @SuppressWarnings("unused")
    public void enter(Action action) {
        Context context = getCurrentContext();
        Action.EnterAction enterAction = (Action.EnterAction) action;
        int id = context.getResources().getIdentifier(action.element, "id", context.getPackageName());

        onView(withId(id)).perform(ViewActions.replaceText(enterAction.value));
    }
    @SuppressWarnings("unused")
    public void contains(Action action) {
        Context context = getCurrentContext();
        final Action.VerifyAction verifyAction = (Action.VerifyAction) action;
        int id = context.getResources().getIdentifier(action.element, "id", context.getPackageName());

        onView(withId(id)).check(matches(new BoundedMatcher<View, TextView>(TextView.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("with text: ");
            }

            @Override
            public boolean matchesSafely(TextView textView) {
                if(textView.getText() != null) {
                    return textView.getText().toString().contains(verifyAction.value);
                } else {
                    return textView.getText() == verifyAction.value;
                }
            }
        }));
    }
    @SuppressWarnings("unused")
    public void wait(Action action) {
        Action.WaitAction waitAction = (Action.WaitAction) action;
        onView(isRoot()).perform(waitFor((long)(((Action.WaitAction) action).value)));
    }

    @SuppressWarnings("unused")
    public void searchField(Action action) {
        Context context = getCurrentContext();
        Action.SearchFieldAction searchFieldAction = (Action.SearchFieldAction) action;
        if(delegate==null) {
            Assert.fail("delegateEarlGreyTestRunnerDelegate has to be impelemented!");
            return;
        }

        delegate.getSearchView().setQuery(searchFieldAction.value, true);

        waitFor(searchFieldAction.wait);
        delegate.getSearchView().setIconified(true);
    }

    @SuppressWarnings("unused")
    public void verify(Action action) {
        Context context = getCurrentContext();
        if(action instanceof Action.VerifyNavigationAction) {
            Action.VerifyNavigationAction verifyNavigationAction = (Action.VerifyNavigationAction) action;
            if(delegate.getCurrentActivity() instanceof AppCompatActivity) {
                AppCompatActivity appCompatActivity = (AppCompatActivity) delegate.getCurrentActivity();
                Assert.assertEquals(verifyNavigationAction.navigationTitle, ((AppCompatActivity) delegate.getCurrentActivity()).getSupportActionBar().getTitle());
            } else {
                Assert.assertEquals(verifyNavigationAction.navigationTitle, delegate.getCurrentActivity().getActionBar().getTitle());
            }
        } else {
            Action.VerifyAction verifyAction = (Action.VerifyAction) action;
            int id = context.getResources().getIdentifier(action.element, "id", context.getPackageName());

            onView(withId(id)).check(matches(withText(verifyAction.value)));
        }
    }

    @SuppressWarnings("unused")
    public void executeJS(Action action) {
        Context context = this.getCurrentContext();
        final Action.ExecuteJSAction executeJSAction = (Action.ExecuteJSAction)action;
        int id = context.getResources().getIdentifier(action.element, "id", context.getPackageName());


        Espresso.onView(ViewMatchers.withId(id)).perform(new ViewAction() {
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(WebView.class);
            }

            public String getDescription() {
                return "execute JS";
            }

            public void perform(UiController uiController, View view) {
                uiController.loopMainThreadForAtLeast(3000);
                WebView webView = (WebView)view;
                WebView webView = (WebView) view;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    webView.evaluateJavascript(executeJSAction.code, null);
                } else {
                    webView.loadUrl("javascript:" + executeJSAction.code);
                }
                final long timeOut = System.currentTimeMillis() + 5000;
                while (!webViewEvaluationFinished.get()) {
                    if (timeOut < System.currentTimeMillis()) {
                        throw new PerformException.Builder()
                                .withActionDescription(this.getDescription())
                                .withViewDescription(HumanReadables.describe(view))
                                .withCause(new RuntimeException(String.format(Locale.US,
                                        "Evaluating java script did not finish after %d ms of waiting.", 5000)))
                                .build();
                    }
                    uiController.loopMainThreadForAtLeast(50);
                }
            }
        });
    }


    private Context getCurrentContext() {
        final Context[] context = new Context[1];
        onView(isRoot()).check(new ViewAssertion() {
            @Override
            public void check(View view, NoMatchingViewException noViewFoundException) {
                context[0] = view.getContext();
            }
        });
        return context[0];
    }


    public static interface EarlGreyTestRunnerDelegate {

        void handles(Action action) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException;
        SearchView getSearchView();
        Activity getCurrentActivity();
    }

    public static ViewAction waitFor(final double seconds) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isRoot();
            }

            @Override
            public String getDescription() {
                return "Wait for " + (seconds*1000) + " milliseconds.";
            }

            @Override
            public void perform(UiController uiController, final View view) {
                uiController.loopMainThreadForAtLeast((long) (seconds*1000));
            }
        };
    }

    private static final class XYScrollByPositionViewAction implements ViewAction {
        private final int x;
        private final int y;

        private XYScrollByPositionViewAction(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Matcher<View> getConstraints() {
            return allOf(isAssignableFrom(RecyclerView.class), isDisplayed());
        }

        @Override
        public String getDescription() {
            return "scroll RecyclerView by " + x + "/" + y;
        }

        @Override
        public void perform(UiController uiController, View view) {
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.scrollBy(x,y);
        }
    }
}
