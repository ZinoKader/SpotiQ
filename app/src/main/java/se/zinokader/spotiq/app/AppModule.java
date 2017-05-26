package se.zinokader.spotiq.app;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import se.zinokader.spotiq.service.SpotifyService;

@Module
class AppModule {

    @Provides
    @Singleton
    SpotifyService provideSpotifyService() {
        return new SpotifyService();
    }

}
