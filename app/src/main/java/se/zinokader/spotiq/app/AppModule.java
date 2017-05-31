package se.zinokader.spotiq.app;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import se.zinokader.spotiq.constants.FirebaseConstants;
import se.zinokader.spotiq.repository.PartiesRepository;
import se.zinokader.spotiq.service.SpotifyCommunicatorService;
import se.zinokader.spotiq.util.helper.FirebaseAuthenticationHelper;

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
        DatabaseReference databaseReference = firebaseDatabase.getReference().child(FirebaseConstants.CHILD_PARTIES);
        return new PartiesRepository(databaseReference);
    }

    @Provides
    FirebaseAuthenticationHelper provideFirebaseAuthenticationHelper() {
        return new FirebaseAuthenticationHelper();
    }

}
