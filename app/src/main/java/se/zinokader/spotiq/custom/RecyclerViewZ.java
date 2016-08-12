package se.zinokader.spotiq.custom;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

public class RecyclerViewZ extends RecyclerView {

    public RecyclerViewZ(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RecyclerViewZ(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public RecyclerViewZ(Context context) {
        super(context);
    }

    @Override
    public boolean fling(int velocityX, int velocityY) {

        velocityY = (int) (velocityY * 0.8);

        return super.fling(velocityX, velocityY);
    }


}
