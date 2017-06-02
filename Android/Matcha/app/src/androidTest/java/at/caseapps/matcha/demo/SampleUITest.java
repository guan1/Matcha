package at.caseapps.matcha.demo;

import android.app.Activity;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.SearchView;

import junit.framework.Assert;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.InputStream;

import at.caseapps.matcha.Action;
import at.caseapps.matcha.MatchaEspressoTest;
import at.caseapps.matcha.demo.MainActivity;


@RunWith(AndroidJUnit4.class)
public class SampleUITest extends MatchaEspressoTest {

    @Rule
    public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(
            MainActivity.class);

    @Test
    public void test() throws Exception {
        String fileName = "SampleTests.tc";
        InputStream resourceAsStream = getClass().getResourceAsStream(fileName);
        performTests(fileName,resourceAsStream);
    }

    @SuppressWarnings("unused")
    public void customElement(Action action) {
        //do nothing
    }

    @Override
    public Activity getCurrentActivity() {
        return activityRule.getActivity();
    }

    @Override
    public SearchView getSearchView() {
        return activityRule.getActivity().getSearchView();
    }
}
