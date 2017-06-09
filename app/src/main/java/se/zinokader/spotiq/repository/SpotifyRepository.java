package se.zinokader.spotiq.repository;

import java.util.Map;

import io.reactivex.Observable;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.TracksPager;
import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SpotifyRepository {

    public Observable<UserPrivate> getMe(SpotifyService spotifyService) {
        return Observable.create(subscriber -> spotifyService.getMe(new Callback<UserPrivate>() {
            @Override
            public void success(UserPrivate userPrivate, Response response) {
                subscriber.onNext(userPrivate);
                subscriber.onComplete();
            }

            @Override
            public void failure(RetrofitError error) {
                subscriber.onError(error);
            }
        }));
    }

    public Observable<TracksPager> searchTracks(String query, Map<String, Object> searchOptions, SpotifyService spotifyService) {
        return Observable.create(subscriber -> spotifyService.searchTracks(query, searchOptions, new Callback<TracksPager>() {
            @Override
            public void success(TracksPager tracksPager, Response response) {
                subscriber.onNext(tracksPager);
                subscriber.onComplete();
            }

            @Override
            public void failure(RetrofitError error) {
                subscriber.onError(error);
            }
        }));
    }


}
