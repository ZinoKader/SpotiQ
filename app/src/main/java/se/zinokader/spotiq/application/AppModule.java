package se.zinokader.spotiq.application;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import kaaes.spotify.webapi.android.SpotifyApi;
import se.zinokader.spotiq.constant.FirebaseConstants;
import se.zinokader.spotiq.repository.PartiesRepository;
import se.zinokader.spotiq.repository.SpotifyRepository;
import se.zinokader.spotiq.repository.TracklistRepository;
import se.zinokader.spotiq.repository.UserRepository;
import se.zinokader.spotiq.service.authentication.SpotifyAuthenticationService;
import se.zinokader.spotiq.service.authentication.SpotifyAuthenticator;

@Module
class AppModule {

    @Provides
    @Singleton
    SpotifyAuthenticator provideSpotifyAuthenticator() {
        return new SpotifyAuthenticator();
    }

    @Provides
    @Singleton
    SpotifyApi provideSpotifyApi() {
        return new SpotifyApi();
    }

    @Provides
    @Singleton
    SpotifyAuthenticationService provideSpotifyAuthenticationService() {
        return new SpotifyAuthenticationService();
    }

    @Provides
    PartiesRepository providePartiesRepository() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference()
                .child(FirebaseConstants.CHILD_PARTYLIST);
        return new PartiesRepository(databaseReference);
    }

    @Provides
    UserRepository provideUserRepository() {
        return new UserRepository();
    }

    @Provides
    TracklistRepository provideTracklistRepository() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference()
                .child(FirebaseConstants.CHILD_PARTYLIST);
        return new TracklistRepository(databaseReference);
    }

    @Provides
    SpotifyRepository provideSpotifyRepository() {
        return new SpotifyRepository();
    }

}
