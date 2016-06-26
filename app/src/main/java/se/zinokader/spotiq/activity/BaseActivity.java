package se.zinokader.spotiq.activity;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {

    Typeface ROBOTOLIGHT;
    Typeface BACKTOBLACK;
    static final String CLIENT_ID = "5646444c2abc4d8299ee3f2cb274f0b6";
    static final int REQUEST_CODE = 1337;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ROBOTOLIGHT = Typeface.createFromAsset(getAssets(), "fonts/robotolight.ttf");
        BACKTOBLACK = Typeface.createFromAsset(getApplication().getAssets(), "fonts/backtoblack.ttf");
    }

}
