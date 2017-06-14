package se.zinokader.spotiq.util.view;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Pre-loads managed items a set amount of pixels in the direction of scroll
 */
public class PreCachingLayoutManager extends LinearLayoutManager {

    private static final int DEFAULT_EXTRA_LAYOUT_SPACE_PIXELS = 600;
    private int extraLayoutSpace = -1;
    private Context context;

    public PreCachingLayoutManager(Context context) {
        super(context);
        this.context = context;
    }

    public PreCachingLayoutManager(Context context, int extraLayoutSpace) {
        super(context);
        this.context = context;
        this.extraLayoutSpace = extraLayoutSpace;
    }

    public PreCachingLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
        this.context = context;
    }

    public void setExtraLayoutSpace(int extraLayoutSpace) {
        this.extraLayoutSpace = extraLayoutSpace;
    }

    @Override
    protected int getExtraLayoutSpace(RecyclerView.State state) {
        if (extraLayoutSpace > 0) {
            return extraLayoutSpace;
        }
        return DEFAULT_EXTRA_LAYOUT_SPACE_PIXELS;
    }
}
