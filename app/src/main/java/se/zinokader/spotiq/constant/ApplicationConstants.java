package se.zinokader.spotiq.constant;

public class ApplicationConstants {

    private ApplicationConstants() {}

    /* Request codes */
    public static final int LOGIN_INTENT_REQUEST_CODE = 5473;
    public static final int SEARCH_INTENT_REQUEST_CODE = 3125;

    /* Time and timing-related */
    public static final int DEFER_SNACKBAR_DELAY = 1000;
    public static final int REQUEST_RETRY_DELAY_SEC = 3;
    public static final int SHORT_VIBRATION_DURATION_MS = 30;
    public static final int SHORT_ACTION_DELAY_SEC = 1;
    public static final int MEDIUM_ACTION_DELAY_SEC = 2;
    public static final int LONG_ACTION_DELAY_SEC = 3;
    public static final int LONG_TOAST_DURATION_SEC = 5;

    /* Image-related */
    //Placeholders
    public static final String ALBUM_ART_PLACEHOLDER_URL = "https://www.zinokader.se/img/spotiq-album-placeholder.png";
    public static final String PLAYLIST_IMAGE_PLACEHOLDER_URL = "https://www.zinokader.se/img/spotiq-playlist-placeholder.png";
    public static final String PROFILE_IMAGE_PLACEHOLDER_URL = "https://www.zinokader.se/img/spotiq-profile-placeholder.png";
    //Dimensions
    public static final int DEFAULT_TRACKLIST_CROP_WIDTH = 600;
    public static final int DEFAULT_TRACKLIST_CROP_HEIGHT = 200;
    public static final int DEFAULT_TRACKLIST_BLUR_RADIUS = 100;
    public static final int LOW_QUALITY_ALBUM_ART_DIMENSION = 100;
    public static final int MEDIUM_QUALITY_ALBUM_ART_DIMENSION = 200;

    /* Notification-related */
    public static final String MEDIA_NOTIFICATION_CHANNEL_ID = "media_notification_channel";
    public static final String MEDIA_SESSSION_TAG = "spotiq_media";

    /* App shortcut-related */
    public static final String SEARCH_SHORTCUT_ID = "spotiq_search_shortcut";

    /* Activity & Lifecycle-related */

    //Splash-related
    public static final float ANIMATION_LOGO_SCALE_DOWN = 0.7f;
    public static final float ANIMATION_LOGO_SCALE_UP = 100f;
    public static final int ANIMATION_LOGO_SCALE_DOWN_DURATION_MS = 750;
    public static final int ANIMATION_LOGO_SCALE_UP_DURATION_MS = 200;

    //Lobby-related
    public static final int DIALOG_ANIMATION_DURATION = 650;

    //Party-related
    public static final String PARTY_NAME_EXTRA = "party_name_extra";
    public static final int PLAY_PAUSE_BUTTON_SYNCHRONIZATION_DELAY_MS = 800;
    public static final int DEFAULT_NEW_ITEM_DELAY_MS = 0;
    public static final int DEFAULT_LIST_ANIMATION_DURATION_MS = 400;
    public static final int DEFAULT_LIST_ANIMATION_ITEM_POSITION_START = 6;
    public static final int DEFAULT_ITEM_ADD_DURATION_MS = 1200;
    public static final int DEFAULT_ITEM_REMOVE_DURATION_MS = 350;
    public static final int DEFAULT_ITEM_MOVE_DURATION_MS = 900;

    /* Search-related */
    public static final String SONG_REQUESTS_EXTRA = "song_requests_extra";
    public static final int DEFAULT_DEBOUNCE_MS = 600;
    public static final int MAX_SONG_SUGGESTIONS = 50;

}
