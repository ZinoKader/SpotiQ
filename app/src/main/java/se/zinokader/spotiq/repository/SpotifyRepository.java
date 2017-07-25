package se.zinokader.spotiq.repository;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.PlaylistTrack;
import kaaes.spotify.webapi.android.models.Track;
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

    public Observable<List<Track>> getMyTopTracks(SpotifyService spotifyService) {
        return Observable.create(subscriber -> spotifyService.getTopTracks(new Callback<Pager<Track>>() {
            @Override
            public void success(Pager<Track> trackPager, Response response) {
                subscriber.onNext(trackPager.items);
                subscriber.onComplete();
            }

            @Override
            public void failure(RetrofitError error) {
                subscriber.onError(error);
            }
        }));
    }

    public Observable<List<PlaylistSimple>> getMyPlaylists(SpotifyService spotifyService) {
        return Observable.create(subscriber -> spotifyService.getMyPlaylists(new Callback<Pager<PlaylistSimple>>() {
            @Override
            public void success(Pager<PlaylistSimple> playlistSimplePager, Response response) {
                subscriber.onNext(playlistSimplePager.items);
                subscriber.onComplete();
            }

            @Override
            public void failure(RetrofitError error) {
                subscriber.onError(error);
            }
        }));
    }

    public Observable<Pager<PlaylistTrack>> getPlaylistTracks(String userId, String playlistId, Map<String, Object> searchOptions, SpotifyService spotifyService) {
        return Observable.create(subscriber -> spotifyService.getPlaylistTracks(userId, playlistId, searchOptions, new Callback<Pager<PlaylistTrack>>() {
            @Override
            public void success(Pager<PlaylistTrack> playlistTrackPager, Response response) {
                subscriber.onNext(playlistTrackPager);
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
