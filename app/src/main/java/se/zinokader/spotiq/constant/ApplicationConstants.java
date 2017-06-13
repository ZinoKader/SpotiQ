package se.zinokader.spotiq.constant;

public class ApplicationConstants {

    private ApplicationConstants() {}

    /* Request codes */
    public static final int LOGIN_INTENT_REQUEST_CODE = 5473;
    public static final int SEARCH_INTENT_REQUEST_CODE = 3125;

    /* Time and timing-related */
    public static final int SHORT_VIBRATION_DURATION = 30;
    public static final int SHORT_ACTION_DELAY = 1;
    public static final int MEDIUM_ACTION_DELAY = 2;

    /* Image-related */
    public static final String ALBUM_ART_PLACEHOLDER_URL = "https://www.zinokader.se/img/spotiq-album-placeholder.png";
    public static final String PROFILE_IMAGE_PLACEHOLDER_URL = "https://www.zinokader.se/img/spotiq-profile-placeholder.png";

    /* Activity & Lifecycle-related */
    //Party-related
    public static final String PARTY_NAME_EXTRA = "party_name_extra";
    public static final int TAB_TRACKLIST_INDEX = 0;
    public static final int TAB_PARTY_MEMBERS_INDEX = 1;

    /* Search-related */
    public static final String SONG_ADD_EXTRA = "song_add_extra";
    public static final int DEFAULT_DEBOUNCE_MS = 400;

}
