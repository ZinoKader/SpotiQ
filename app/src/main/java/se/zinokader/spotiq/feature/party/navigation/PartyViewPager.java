package se.zinokader.spotiq.feature.party.navigation;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class PartyViewPager extends ViewPager {

    private static final boolean canScrollHorizontally = false;

    public PartyViewPager(Context context) {
        super(context);
    }

    public PartyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    //disable horizontal scroll
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return canScrollHorizontally;
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return canScrollHorizontally;
    }

    //Disable animation on item change
    @Override
    public void setCurrentItem(int item) {
        super.setCurrentItem(item, false);
    }
    @Override
    public void setCurrentItem(int item, boolean smoothScroll) {
        super.setCurrentItem(item, false);
    }
}
