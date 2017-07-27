package se.zinokader.spotiq.util;

import android.content.Context;
import android.content.SharedPreferences;

import se.zinokader.spotiq.constant.PreferenceConstants;

public class PreferenceUtil {

    public static boolean isAutoPlayEnabled(Context context) {
        SharedPreferences prefs = getPrefs(context, PreferenceConstants.PLAYER_PREFERENCES);
        if (prefs.contains(PreferenceConstants.PREFERENCE_AUTOPLAY)) {
            return prefs.getBoolean(PreferenceConstants.PREFERENCE_AUTOPLAY, PreferenceConstants.PREFERENCE_AUTOPLAY_DEFAULT);
        }
        else {
            prefs.edit()
                .putBoolean(PreferenceConstants.PREFERENCE_AUTOPLAY, PreferenceConstants.PREFERENCE_AUTOPLAY_DEFAULT)
                .apply();
            return true;
        }
    }

    private static SharedPreferences getPrefs(Context context, String preferenceName) {
        return context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
    }

}
