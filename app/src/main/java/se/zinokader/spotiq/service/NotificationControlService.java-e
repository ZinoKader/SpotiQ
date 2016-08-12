package se.zinokader.spotiq.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.NotificationTarget;

import java.util.concurrent.ExecutionException;

import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.CropTransformation;
import se.zinokader.spotiq.R;
import se.zinokader.spotiq.activity.PartyActivity;
import se.zinokader.spotiq.constants.Constants;

import static se.zinokader.spotiq.constants.Constants.NOTIFICATION_PLAYPAUSE_ACTION;
import static se.zinokader.spotiq.constants.Constants.NOTIFICATION_STARTFOREGROUND_ACTION;
import static se.zinokader.spotiq.constants.Constants.NOTIFICATION_STOPFOREGROUND_ACTION;

public class NotificationControlService extends Service {

    private String songname;
    private String artist;
    private String albumcoverurl;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try {
            this.songname = intent.getStringExtra("songname");
            this.artist = intent.getStringExtra("artist");
            this.albumcoverurl = intent.getStringExtra("albumcoverurl");

            switch(intent.getAction()) {
                case NOTIFICATION_STARTFOREGROUND_ACTION:
                    showNotification();
                    break;
                case NOTIFICATION_PLAYPAUSE_ACTION:
                    Intent playpauseintent = new Intent(NOTIFICATION_PLAYPAUSE_ACTION);
                    this.sendBroadcast(playpauseintent);
                    break;
                case NOTIFICATION_STOPFOREGROUND_ACTION:
                    stopForeground(true);
                    stopSelf();
            }
        } catch(NullPointerException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return START_STICKY;
    }

    private void showNotification() throws ExecutionException, InterruptedException {

        final RemoteViews remoteviews = new RemoteViews(getPackageName(),
                R.layout.notification_control_normal);

        //sätt intents för klick av playpauseknappen
        Intent playpauseintent = new Intent(getApplicationContext(), NotificationControlService.class);
        playpauseintent.setAction(NOTIFICATION_PLAYPAUSE_ACTION);
        PendingIntent pendingplaypauseintent = PendingIntent.getService(getApplicationContext(), 0, playpauseintent, 0);

        //sätt intents för klick av notisen, öppna partyactivity
        Intent openactivityintent = new Intent(getApplicationContext(), PartyActivity.class);
        openactivityintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); //resumar activit ist för att ladda om
        openactivityintent.setAction(NOTIFICATION_STOPFOREGROUND_ACTION);
        PendingIntent pendingopenactivityintent = PendingIntent.getActivity(getApplicationContext(), 0,
                openactivityintent, PendingIntent.FLAG_UPDATE_CURRENT);

        //vid klick av playpauseknappen skickas playintent och triggar (NOTIFICATION_PLAYPAUSE_ACTION i onStartCommand())
        remoteviews.setOnClickPendingIntent(R.id.notification_playpause_button, pendingplaypauseintent);

        //manipulering av låtnamn, artist och albumcover
        remoteviews.setTextViewText(R.id.notification_track_name, songname);
        remoteviews.setTextViewText(R.id.notification_artist_name, artist);

        //skapa notification
        Notification notification = new Notification.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.spotiqlogoblackandwhite)
                .setPriority(Notification.PRIORITY_HIGH)
                .setContentIntent(pendingopenactivityintent)
                .build();
        notification.contentView = remoteviews;
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        

        /*
        //Här börjar vi ladda albumcover in i notification
        */
        //skapa notificationtarget för glide
        NotificationTarget notificationTarget = new NotificationTarget(
                getApplicationContext(),
                remoteviews,
                R.id.notification_album_cover,
                notification,
                Constants.NOTIFICATION_ID);

        CropTransformation croptransform = new CropTransformation(getApplicationContext(), 2010, 315, CropTransformation.CropType.CENTER); //width, height (holy fuck rör inte detta, tog 2 timmar)
        BlurTransformation blurtransform = new BlurTransformation(getApplicationContext(), 50, 1); //blurradius, downsampling (scale, 1 == ingen downsampling)

        //ladda albumcover in i notificationtarget
        Glide.with(getApplicationContext())
                .load(albumcoverurl)
                .asBitmap()
                .transform(croptransform, blurtransform)
                .into(notificationTarget);


        //STARTA SERVICE
        startForeground(Constants.NOTIFICATION_ID, notification);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
