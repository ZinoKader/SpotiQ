package se.zinokader.spotiq.constant;

public final class SpotifyConstants {

    private SpotifyConstants() {}

    /* Spotify Android SDK-related */
    public static final String REDIRECT_URI = "spotiq://callback";
    public static final String PRODUCT_PREMIUM = "premium";
    private static final String SCOPE_USER_READ_PRIVATE = "user-read-private";
    private static final String SCOPE_PLAYLIST_READ_PRIVATE = "playlist-read-private";
    private static final String SCOPE_STREAMING = "streaming";
    private static final String SCOPE_USER_TOP_READ = "user-top-read";
    public static final String[] DEFAULT_USER_SCOPES = new String[] {
        SCOPE_USER_READ_PRIVATE,
        SCOPE_STREAMING,
        SCOPE_PLAYLIST_READ_PRIVATE,
        SCOPE_USER_TOP_READ };

    public static final int RESULT_CODE_AUTHENTICATED = 100;
    public static final int RESULT_CODE_ERROR = 200;
    public static final int RESULT_CODE_NO_PREMIUM = 300;

    /* Spotify Web API-related */
    public static final int TOP_TRACKS_QUERY_RESPONSE_LIMIT = 50;
    public static final int TRACK_SEARCH_QUERY_RESPONSE_LIMIT = 50;
    public static final int TRACK_SEARCH_TOTAL_ITEMS_LIMIT = 50;
    public static final int PLAYLIST_SEARCH_QUERY_RESPONSE_LIMIT = 50;
    public static final int PLAYLIST_TRACK_SEARCH_QUERY_RESPONSE_LIMIT = 100;
    public static final int PLAYLIST_SEARCH_TOTAL_ITEMS_LIMIT = 10000;
    public static final int PLAYLIST_TRACK_SEARCH_TOTAL_ITEMS_LIMIT = 10000;

    public static final String TIME_RANGE_SHORT = "short_term";
    public static final String TIME_RANGE_MEDIUM = "medium_term";
    public static final String TIME_RANGE_LONG = "long_term";

}
