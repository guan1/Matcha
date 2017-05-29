package at.caseapps.matcha;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.test.InstrumentationRegistry;
import android.support.v7.widget.SearchView;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

import at.caseapps.matcha.tests.Scanner;

/**
 * Created by andreguggenberger on 20/05/2017.
 */

public class MatchaEspressoTest implements EspressoRunner.EarlGreyTestRunnerDelegate {

    private SystemAnimations systemAnimations;

    private Context context;

    @Before
    public void setup() {
        context = InstrumentationRegistry.getContext();
        systemAnimations = new SystemAnimations(context);
        systemAnimations.disableAll();
    }

    @After
    public void tearDown() {
        systemAnimations.enableAll();
    }

    protected void performTests(String fileName, InputStream inputStream) throws IOException {
        Scanner scanner = new Scanner(inputStream, fileName);
        if (scanner.getTestCase() != null) {
            EspressoRunner runner = new EspressoRunner(scanner.getTestCase(), this);
            runner.start();
        } else {
            throw new RuntimeException("No testcases found in file: " + fileName);
        }
    }

    @Override
    public SearchView getSearchView() {
        throw new RuntimeException("Subclasses have to implement getSearchView");
    }

    @Override
    public Activity getCurrentActivity() {
        throw new RuntimeException("Subclasses have to implement getCurrentActivity");
    }

    @Override
    public boolean handles(Action action) {
        try {
            Method m = getClass().getMethod(action.name, Action.class);
            m.invoke(this, action);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    class SystemAnimations {

        private static final String ANIMATION_PERMISSION = "android.permission.SET_ANIMATION_SCALE";
        private static final float DISABLED = 0.0f;
        private static final float DEFAULT = 1.0f;

        private final Context context;

        SystemAnimations(Context context) {
            this.context = context;
        }

        void disableAll() {
            setSystemAnimationsScale(DISABLED);
        }

        void enableAll() {
            setSystemAnimationsScale(DEFAULT);
        }

        private void setSystemAnimationsScale(float animationScale) {
            try {
                Class<?> windowManagerStubClazz = Class.forName("android.view.IWindowManager$Stub");
                Method asInterface = windowManagerStubClazz.getDeclaredMethod("asInterface", IBinder.class);
                Class<?> serviceManagerClazz = Class.forName("android.os.ServiceManager");
                Method getService = serviceManagerClazz.getDeclaredMethod("getService", String.class);
                Class<?> windowManagerClazz = Class.forName("android.view.IWindowManager");
                Method setAnimationScales = windowManagerClazz.getDeclaredMethod("setAnimationScales", float[].class);
                Method getAnimationScales = windowManagerClazz.getDeclaredMethod("getAnimationScales");

                IBinder windowManagerBinder = (IBinder) getService.invoke(null, "window");
                Object windowManagerObj = asInterface.invoke(null, windowManagerBinder);
                float[] currentScales = (float[]) getAnimationScales.invoke(windowManagerObj);
                for (int i = 0; i < currentScales.length; i++) {
                    currentScales[i] = animationScale;
                }
                setAnimationScales.invoke(windowManagerObj, new Object[]{currentScales});
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
