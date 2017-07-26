package se.zinokader.spotiq.application;

import javax.inject.Singleton;

import dagger.Component;
import se.zinokader.spotiq.feature.lobby.LobbyPresenter;
import se.zinokader.spotiq.feature.login.SpotifyAuthenticationActivity;
import se.zinokader.spotiq.feature.login.StartupPresenter;
import se.zinokader.spotiq.feature.party.PartyPresenter;
import se.zinokader.spotiq.feature.party.partymember.PartyMemberFragment;
import se.zinokader.spotiq.feature.party.tracklist.TracklistFragment;
import se.zinokader.spotiq.feature.search.playlistsearch.PlaylistSearchPresenter;
import se.zinokader.spotiq.feature.search.songsearch.SongSearchPresenter;
import se.zinokader.spotiq.service.player.SpotiqPlayerService;

@Singleton
@Component(modules = { AppModule.class })
public interface AppComponent {
    void inject(SpotifyAuthenticationActivity target);
    void inject(StartupPresenter target);
    void inject(LobbyPresenter target);
    void inject(PartyPresenter target);
    void inject(TracklistFragment target);
    void inject(PartyMemberFragment target);
    void inject(SongSearchPresenter target);
    void inject(PlaylistSearchPresenter target);
    void inject(SpotiqPlayerService target);
}
