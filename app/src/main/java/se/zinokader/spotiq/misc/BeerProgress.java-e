package se.zinokader.spotiq.misc;

import android.os.CountDownTimer;
import android.util.Log;

import se.zinokader.spotiq.view.LobbyView;

public class BeerProgress {

    long totaltime = 5000;
    long ticktime = 10;
    int progresspercent = 0;

    private CountDownTimer beertimer;

    public void startPouring(final LobbyView view) {

        beertimer = new CountDownTimer(totaltime, ticktime) {

            @Override
            public void onTick(long millisUntilFinished) {
                progresspercent++;
                view.setBeerProgress(progresspercent);
            }

            @Override
            public void onFinish() {
                try {
                    view.setBeerProgress(100);
                    progresspercent = 0;
                } catch (Exception e) {
                    Log.e("Error", "Error: " + e.toString());
                }
            }

        }
        .start();
    }

    public void stopPouring(LobbyView view) {
        beertimer.cancel();
        view.setBeerProgress(0);
        progresspercent = 0;
    }

}
