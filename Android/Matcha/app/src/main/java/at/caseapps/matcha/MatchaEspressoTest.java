package at.caseapps.matcha;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import org.junit.Before;

import java.io.IOException;
import java.io.InputStream;

import at.caseapps.matcha.tests.Scanner;

/**
 * Created by andreguggenberger on 20/05/2017.
 */

public class MatchaEspressoTest {

    private Context context;

    @Before
    public void setup() {
        context = InstrumentationRegistry.getContext();
    }

    protected void performTests(String fileName, InputStream inputStream) throws IOException {
        Scanner scanner = new Scanner(inputStream, fileName);
        if (scanner.getTestCase() != null) {
            EspressoRunner runner = new EspressoRunner(scanner.getTestCase());
            runner.start();
        } else {
            throw new RuntimeException("No testcases found in file: " + fileName);
        }
    }
}
