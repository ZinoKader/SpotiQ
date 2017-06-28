package se.zinokader.spotiq.constant;

import java.util.concurrent.TimeUnit;

public class ServiceConstants {

    private ServiceConstants() {}

    /* Authentication-service */
    public static final long TOKEN_EXPIRY_CUTOFF = TimeUnit.MINUTES.toSeconds(20);

    /* Player-service */
    public static final String ACTION_INIT = "spotiq.ACTION_INIT";
    public static final String PLAYING_STATUS_BROADCAST_NAME = "spotiq.PLAYING_STATUS_BROADCAST";
    public static final String PLAYING_STATUS_BROADCAST_KEY = "playing_status_key";


}
