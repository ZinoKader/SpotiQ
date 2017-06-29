package se.zinokader.spotiq.util;

import se.zinokader.spotiq.BuildConfig;

public class VersionUtil {

    public static int getCurrentAppVersionCode() {
        return BuildConfig.VERSION_CODE;
    }

}
