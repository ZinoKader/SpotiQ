package se.zinokader.spotiq.util.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;


public class EmptySupportRecyclerView extends RecyclerView {

    private View emptyView;

    public EmptySupportRecyclerView(Context context) {
        super(context);
    }

    public EmptySupportRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public EmptySupportRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setEmptyView(View emptyView) {
        this.emptyView = emptyView;
    }

    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
        adapter.registerAdapterDataObserver(emptyObserver);
        emptyObserver.onChanged();
    }

    private AdapterDataObserver emptyObserver = new AdapterDataObserver() {

        private void setViewVisibility() {
            if (getAdapter() != null && emptyView != null) {
                if (getAdapter().getItemCount() == 0) {
                    emptyView.setVisibility(View.VISIBLE);
                    EmptySupportRecyclerView.this.setVisibility(View.INVISIBLE);
                }
                else {
                    emptyView.setVisibility(View.INVISIBLE);
                    EmptySupportRecyclerView.this.setVisibility(View.VISIBLE);
                }
            }
        }

        @Override
        public void onChanged() {
            setViewVisibility();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            super.onItemRangeChanged(positionStart, itemCount);
            setViewVisibility();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            super.onItemRangeChanged(positionStart, itemCount, payload);
            setViewVisibility();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            super.onItemRangeInserted(positionStart, itemCount);
            setViewVisibility();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            super.onItemRangeRemoved(positionStart, itemCount);
            setViewVisibility();
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            super.onItemRangeMoved(fromPosition, toPosition, itemCount);
            setViewVisibility();
        }
    };

}
