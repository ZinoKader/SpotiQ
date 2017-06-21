package se.zinokader.spotiq.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.util.Arrays;

import se.zinokader.spotiq.R;
import se.zinokader.spotiq.constant.ApplicationConstants;
import se.zinokader.spotiq.feature.search.SearchActivity;

public class ShortcutUtil {

    @RequiresApi(api = Build.VERSION_CODES.N_MR1)
    public static void addSearchShortcut(Context context, String searchWithPartyTitle) {
        ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);

        Intent openSearch = new Intent(context.getApplicationContext(), SearchActivity.class);
        openSearch.putExtra(ApplicationConstants.PARTY_NAME_EXTRA, searchWithPartyTitle);
        openSearch.setAction(Intent.ACTION_VIEW);

        ShortcutInfo shortcut = new ShortcutInfo.Builder(context, ApplicationConstants.SEARCH_SHORTCUT_ID)
            .setShortLabel("Search songs")
            .setLongLabel("Search for songs to add to the queue")
            .setIcon(Icon.createWithResource(context, R.drawable.ic_shortcut_search))
            .setIntent(openSearch)
            .build();

        shortcutManager.setDynamicShortcuts(Arrays.asList(shortcut));
    }


    @RequiresApi(api = Build.VERSION_CODES.N_MR1)
    public static void removeAllShortcuts(Context context) {
        ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
        shortcutManager.disableShortcuts(Arrays.asList(ApplicationConstants.SEARCH_SHORTCUT_ID));
        shortcutManager.removeAllDynamicShortcuts();
    }

}
