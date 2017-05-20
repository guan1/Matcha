package at.caseapps.matcha;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.*;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.matcher.ViewMatchers;

import java.io.InputStream;

import at.caseapps.matcha.sample.MainActivity;


@RunWith(AndroidJUnit4.class)
public class SampleUITest extends MatchaEspressoTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class);

    @Test
    public void test() throws Exception {
        String fileName = "SampleTests.tc";
        InputStream resourceAsStream = getClass().getResourceAsStream(fileName);
        performTests(fileName,resourceAsStream);
    }
}
