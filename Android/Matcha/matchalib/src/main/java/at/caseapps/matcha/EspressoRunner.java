package at.caseapps.matcha;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.PerformException;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.ViewAssertion;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.espresso.util.HumanReadables;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Assert;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.scrollTo;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withTagKey;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;


/**
 * Created by andreguggenberger on 20/05/2017.
 */

public class EspressoRunner {
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
        Espresso.pressBack();
    }

    @SuppressWarnings("unused")
    public void scroll(Action action) {
        Context context = getCurrentContext();
        Action.ScrollAction scrollAction = (Action.ScrollAction) action;
        Matcher<View> viewMatcher = getViewMatcher(action.element, context);


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

        onView(viewMatcher).perform(new ScrollViewAction(context, scrollAction.to, x,y));
    }

    @SuppressWarnings("unused")
    public void click(final Action action) {
        Context context = getCurrentContext();
        Matcher<View> viewMatcher = getViewMatcher(action.element, context);
        onView(viewMatcher).perform(
                new ViewAction() {
                    @Override
                    public Matcher<View> getConstraints() {
                        return ViewMatchers.isEnabled();
                    }

                    @Override
                    public String getDescription() {
                        return "click button";
                    }

                    @Override
                    public void perform(UiController uiController, View view) {
                        view.performClick();
                    }
                }
        );
    }

    @NonNull
    private Matcher<View> getViewMatcher(final String element, Context context) {
        final int id = context.getResources().getIdentifier(element, "id", context.getPackageName());

        Matcher<View> viewMatcher;
        viewMatcher = new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("with key: " + element);
            }

            @Override
            public boolean matchesSafely(View view) {
                if(view.getTag(at.caseapps.matcha.utils.R.id.matchaElementId) != null) {
                    String v = (String) view.getTag(at.caseapps.matcha.utils.R.id.matchaElementId);
                    if(v.equals(element)) {
                        return true;
                    }
                }
                return id == view.getId();
            }
        };
        return viewMatcher;
    }

    @SuppressWarnings("unused")
    public void enter(Action action) {
        Context context = getCurrentContext();
        Action.EnterAction enterAction = (Action.EnterAction) action;
        Matcher<View> viewMatcher = getViewMatcher(action.element, context);

        onView(viewMatcher).perform(new SetTextAction(enterAction.value));
    }

    @SuppressWarnings("unused")
    public void contains(Action action) {
        Context context = getCurrentContext();
        final Action.VerifyAction verifyAction = (Action.VerifyAction) action;
        Matcher<View> viewMatcher = getViewMatcher(action.element, context);

        onView(viewMatcher).check(matches(new BoundedMatcher<View, TextView>(TextView.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("contains text: ");
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
            Matcher<View> viewMatcher = getViewMatcher(action.element, context);

            if(verifyAction.value==null) {
                onView(viewMatcher).check(matches(new BoundedMatcher<View, TextView>(TextView.class) {
                    @Override
                    public void describeTo(Description description) {
                        description.appendText("verify not null");
                    }

                    @Override
                    public boolean matchesSafely(TextView textView) {
                        return textView.getText() != null && textView.getText().toString().length() > 0;
                    }
                }));
            } else {
                onView(viewMatcher).check(matches(withText(verifyAction.value)));
            }

        }
    }

    @SuppressWarnings("unused")
    public void executeJS(Action action) {
        Context context = this.getCurrentContext();
        final Action.ExecuteJSAction executeJSAction = (Action.ExecuteJSAction)action;
        Matcher<View> viewMatcher = getViewMatcher(action.element, context);

        final AtomicBoolean evaluateFinished = new AtomicBoolean(false);
        Espresso.onView(viewMatcher).perform(new ViewAction() {
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(WebView.class);
            }

            public String getDescription() {
                return "execute JS";
            }

            public void perform(UiController uiController, View view) {
                uiController.loopMainThreadForAtLeast(5000);
                WebView webView = (WebView)view;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    webView.evaluateJavascript(executeJSAction.code, new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {
                            evaluateFinished.set(true);
                        }
                    });
                } else {
                    webView.loadUrl("javascript:" + executeJSAction.code);
                    evaluateFinished.set(true);
                }

                final long timeOut = System.currentTimeMillis() + 5000;
                while (!evaluateFinished.get()) {
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
                uiController.loopMainThreadForAtLeast(50);
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

    public final class SetTextAction implements ViewAction {
        private final String stringToBeSet;

        public SetTextAction(String value) {
            this.stringToBeSet = value;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Matcher<View> getConstraints() {
            return allOf(isAssignableFrom(EditText.class));
        }

        @Override
        public void perform(UiController uiController, View view) {
            ((EditText) view).setText(stringToBeSet);
        }

        @Override
        public String getDescription() {
            return "set text";
        }
    }

    private final class ScrollViewAction implements ViewAction {
        private final Context context;
        private final String element;
        private final int x;
        private final int y;

        private ScrollViewAction(Context context, String element, int x, int y) {
            this.context = context;
            this.element = element;
            this.x = x;
            this.y = y;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Matcher<View> getConstraints() {
            return anyOf(isAssignableFrom(RecyclerView.class), isAssignableFrom(ScrollView.class), isAssignableFrom(ViewPager.class));
        }

        @Override
        public String getDescription() {
            return "scroll by " + x + "/" + y;
        }

        @Override
        public void perform(UiController uiController, View view) {
            if(view instanceof ScrollView) {
                view.scrollBy(x,y);
            } else if(view instanceof RecyclerView){
                scrollTo(getViewMatcher(element, context)).perform(uiController, view);
            } else if(view instanceof ViewPager){
                ViewPager viewPager = (ViewPager) view;
                viewPager.scrollBy(x,y);
            }
        }
    }
}
