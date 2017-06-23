package se.zinokader.spotiq.constant;

import java.util.concurrent.TimeUnit;

public class ServiceConstants {

    private ServiceConstants() {}

    /* Authentication-service */
    public static final long TOKEN_EXPIRY_CUTOFF = TimeUnit.MINUTES.toSeconds(20);

    /* Player-service */
    public static final String ACTION_INIT = "spotiq.ACTION_INIT";
    public static final String ACTION_PLAY_PAUSE = "spotiq.PLAY_PAUSE";


}
