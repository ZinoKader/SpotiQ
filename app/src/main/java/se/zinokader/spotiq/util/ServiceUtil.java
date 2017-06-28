package se.zinokader.spotiq.util;

import android.app.ActivityManager;
import android.content.Context;

import se.zinokader.spotiq.service.SpotiqPlayerService;

public class ServiceUtil {

    public static boolean isPlayerServiceInForeground(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo runningService : manager.getRunningServices(
            Integer.MAX_VALUE)) {
            if (SpotiqPlayerService.class.getName().equals(runningService.service.getClassName())) {
                if (runningService.foreground) {
                    return true;
                }
            }
        }
        return false;
    }

}
