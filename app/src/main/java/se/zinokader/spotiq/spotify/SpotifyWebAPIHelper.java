package se.zinokader.spotiq.spotify;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.PlaylistTrack;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.UserPrivate;
import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;
import se.zinokader.spotiq.model.Stickynotification;

public class SpotifyWebAPIHelper {

    SpotifyApi spotifyapi = new SpotifyApi();
    SpotifyService spotifyservice = spotifyapi.getService();

    public SpotifyWebAPIHelper() {
    }

    public SpotifyWebAPIHelper(String accesstoken) {
        this.spotifyapi.setAccessToken(accesstoken);
    }

    //behöver accesstoken
    public Observable<Pager<PlaylistSimple>> getUserPlaylists() {
        return Observable.defer(new Func0<Observable<Pager<PlaylistSimple>>>() {
            @Override
            public Observable<Pager<PlaylistSimple>> call() {
                return Observable.just(spotifyservice.getMyPlaylists());
            }
        });
    }

    public Observable<List<PlaylistTrack>> getSongsFromPlaylist(final String userid, final String playlisturi, final int offset) {
        final Map<String, Object> searchoptions = new HashMap<>();
        searchoptions.put(SpotifyService.MARKET, "from_token");
        searchoptions.put(SpotifyService.OFFSET, offset);

        return Observable.defer(new Func0<Observable<List<PlaylistTrack>>>() {
            @Override
            public Observable<List<PlaylistTrack>> call() {
                return Observable.just(spotifyservice.getPlaylistTracks(userid, playlisturi, searchoptions))
                        .concatMap(new Func1<Pager<PlaylistTrack>, Observable<? extends List<PlaylistTrack>>>() {
                            @Override
                            public Observable<? extends List<PlaylistTrack>> call(Pager<PlaylistTrack> playlistTrackPager) {
                                return Observable.just(playlistTrackPager.items) //om vi inte är vid sista sidan, fortsätt concatta med alla items
                                        .concatWith(getSongsFromPlaylist(userid, playlisturi, offset + 100));
                            }
                        })
                        .takeWhile(new Func1<List<PlaylistTrack>, Boolean>() {
                            @Override
                            public Boolean call(List<PlaylistTrack> playlistTracks) {
                                return playlistTracks.size() != 0; //fortsätt tills det inte finns fler tracks
                            }
                        });
            }
        });

    }

    //behöver accesstoken
    public Observable<UserPrivate> getUser() {
        return Observable.defer(new Func0<Observable<UserPrivate>>() {
            @Override
            public Observable<UserPrivate> call() {
                return Observable.just(spotifyservice.getMe());
            }
        });
    }

    public Observable<Stickynotification> getStickyNotification(final String uri) {
        return Observable.defer(new Func0<Observable<Stickynotification>>() {
            @Override
            public Observable<Stickynotification> call() {
                Stickynotification stickynotification = new Stickynotification();

                stickynotification.setSongname(spotifyservice.getTrack(uri.replace("spotify:track:", "")).name);
                stickynotification.setArtist(spotifyservice.getTrack(uri.replace("spotify:track:", "")).artists.get(0).name);
                stickynotification.setAlbumCoverUrl(spotifyservice.getTrack(uri.replace("spotify:track:", "")).album.images.get(0).url);

                return Observable.just(stickynotification);
            }
        });
    }

    public Observable<List<Track>> getSongList(final String searchquery, final int searchlimit) {
        final Map<String, Object> searchoptions = new HashMap<>();
        searchoptions.put(SpotifyService.LIMIT, searchlimit);
        searchoptions.put(SpotifyService.MARKET, "from_token");

        return Observable.defer(new Func0<Observable<List<Track>>>() {
            @Override
            public Observable<List<Track>> call() {
                return Observable.just(spotifyservice.searchTracks(searchquery, searchoptions).tracks.items);
            }
        });
    }

}
