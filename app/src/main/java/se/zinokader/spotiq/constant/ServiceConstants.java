package se.zinokader.spotiq.constant;

import java.util.concurrent.TimeUnit;

public class ServiceConstants {

    private ServiceConstants() {}

    public static final long TOKEN_EXPIRY_CUTOFF = TimeUnit.MINUTES.toSeconds(20);

}
