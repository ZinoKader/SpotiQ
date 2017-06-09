package se.zinokader.spotiq.constant;

public final class SpotifyConstants {

    private SpotifyConstants() {}

    private static final String SCOPE_USER_READ_PRIVATE = "user-read-private";
    private static final String SCOPE_PLAYLIST_READ_PRIVATE = "playlist-read-private";
    private static final String SCOPE_STREAMING = "streaming";

    public static final String[] DEFAULT_USER_SCOPES = new String[] {
            SCOPE_USER_READ_PRIVATE,
            SCOPE_STREAMING,
            SCOPE_PLAYLIST_READ_PRIVATE};

    public static final String CLIENT_ID = "5646444c2abc4d8299ee3f2cb274f0b6";
    public static final String REDIRECT_URI = "spotiq://callback";

}
