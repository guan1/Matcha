package at.caseapps.matcha.utils;

import android.view.View;

/**
 * Created by andreguggenberger on 02/06/2017.
 */

public class MatchaBinder {

    public static void bind(View view, String identifier) {
        view.setTag(R.id.matchaElementId, identifier);
    }

    public static String getElementIdentifier(View view) {
        return (String) view.getTag(R.id.matchaElementId);
    }
}
