package se.zinokader.spotiq.app;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import se.zinokader.spotiq.service.SpotifyAuthenticationService;

@Module
class AppModule {

    @Provides
    @Singleton
    SpotifyAuthenticationService provideSpotifyAuthenticationService() {
        return new SpotifyAuthenticationService();
    }

}
