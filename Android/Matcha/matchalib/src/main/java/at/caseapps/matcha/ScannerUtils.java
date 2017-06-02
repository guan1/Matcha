package at.caseapps.matcha;

/**
 * Created by andreguggenberger on 20/05/2017.
 */

public class ScannerUtils {

    public static Pair matcha_splitStep(String string) {
        String[] array = matcha_splitToFirstSpace(string);
        String actionName = array[0].substring(1);
        if (array.length > 1) {
            String actionParamsAsString = array[1].trim();
            return new Pair(actionName, actionParamsAsString);
        }
        return new Pair(actionName, null);
    }

    public static String[] matcha_splitToFirstSpace(String string) {
        return string.split(" ", 2);
    }

    //starts/ends with ' or "?
    public static boolean matcha_isValue(String string) {
        String s = string.trim();
        return (s.startsWith("'") && s.endsWith("'")) || (s.startsWith("\"") && s.endsWith("\""));
    }

    //remove leading/trailing spaces and ' or "
    public static String matcha_trimValue(String string) {
        String s = string.trim();
        if (matcha_isValue(s)) {
            return s.trim().substring(1, s.length() - 1);
        } else {
            return s;
        }
    }

    public static class Pair {
        public String first;
        public String second;

        public Pair(String first, String second) {
            this.first = first;
            this.second = second;
        }
    }
}
