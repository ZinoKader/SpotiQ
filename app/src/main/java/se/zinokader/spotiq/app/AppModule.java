package se.zinokader.spotiq.app;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import se.zinokader.spotiq.constants.FirebaseConstants;
import se.zinokader.spotiq.repository.PartiesRepository;
import se.zinokader.spotiq.repository.SpotifyRepository;
import se.zinokader.spotiq.service.SpotifyCommunicatorService;

@Module
class AppModule {

    @Provides
    @Singleton
    SpotifyCommunicatorService provideSpotifyCommunicatorService() {
        return new SpotifyCommunicatorService();
    }

    @Provides
    @Singleton
    PartiesRepository providePartiesRepository() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference().child(FirebaseConstants.CHILD_PARTYLIST);
        return new PartiesRepository(databaseReference);
    }

    @Provides
    SpotifyRepository provideSpotifyRepository() {
        return new SpotifyRepository();
    }

}
