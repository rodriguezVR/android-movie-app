package com.example.android.movie.modules;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import static com.example.android.movie.utils.Constant.ONE;

/**
 * Set column spacing to make each column have the same spacing.
 *
 * Reference: @see "https://stackoverflow.com/questions/28531996/android-recyclerview-gridlayoutmanager-column-spacing"
 */
public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

    private int spanCount;
    private int spacing;
    private boolean includeEdge;

    /**
     * Constructor
     *
     * @param spanCount The number of columns
     * @param spacing The spacing between each grid item
     * @param includeEdge Whether to include left and right margins
     */
    public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
        this.spanCount = spanCount;
        this.spacing = spacing;
        this.includeEdge = includeEdge;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        int column = position % spanCount;

        if (includeEdge) {
            outRect.left = spacing - column * spacing / spanCount;
            outRect.right = (column + ONE) * spacing / spanCount;

            if (position < spanCount) {
                outRect.top = spacing;
            }
            outRect.bottom = spacing;
        } else {
            outRect.left = column * spacing / spanCount;
            outRect.right = spacing - (column + ONE) * spacing / spanCount;
            if (position >= spanCount) {
                outRect.top = spacing;
            }
        }
    }
}
