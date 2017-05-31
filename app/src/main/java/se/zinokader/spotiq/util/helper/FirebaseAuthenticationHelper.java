package se.zinokader.spotiq.util.helper;

import com.google.firebase.auth.FirebaseAuth;

import io.reactivex.Single;


public class FirebaseAuthenticationHelper {

    public Single<Boolean> authenticateAnonymously() {
        return Single.fromCallable(() -> FirebaseAuth.getInstance().signInAnonymously().isSuccessful());
    }

}
