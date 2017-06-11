package se.zinokader.spotiq.util.listener;

import android.os.CountDownTimer;

public class Debouncer {

    private CountDownTimer debounceTimer;
    private Runnable pendingRunnable;

    public Debouncer() {

    }

    public void debounce(Runnable runnable, long delayMs) {
        pendingRunnable = runnable;
        cancelTimer();
        startTimer(delayMs);
    }

    public void cancel() {
        cancelTimer();
        pendingRunnable = null;
    }

    private void startTimer(final long updateIntervalMs) {

        if (updateIntervalMs > 0) {

            // Debounce timer
            debounceTimer = new CountDownTimer(updateIntervalMs, updateIntervalMs) {

                @Override
                public void onTick(long millisUntilFinished) {
                    // Do nothing
                }

                @Override
                public void onFinish() {
                    execute();
                }
            };
            debounceTimer.start();
        }
        else {

            // Do immediately
            execute();
        }
    }

    private void cancelTimer() {
        if (debounceTimer != null) {
            debounceTimer.cancel();
            debounceTimer = null;
        }
    }

    private void execute() {
        if (pendingRunnable != null) {
            pendingRunnable.run();
            pendingRunnable = null;
        }
    }
}