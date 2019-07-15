package com.example.android.movie.modules;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import static com.example.android.movie.utils.Constant.THREE;
import static com.example.android.movie.utils.Constant.TWO;

/**
 * The TwoThreeImageView class is responsible for making ImageView 2:3 aspect ratio.
 * The TwoThreeImageView is used for movie poster in the movie_list_item.xml.
 */
public class TallerImageView extends AppCompatImageView {

    /**
     * Creates a TwoThreeImageView
     *
     * @param context Used to talk to the UI and app resources
     */
    public TallerImageView(Context context) {
        super(context);
    }

    public TallerImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TallerImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * This method measures the view and its content to determine the measured width and the measured
     * height, which will make 2:3 aspect ratio.
     *
     * @param widthMeasureSpec horizontal space requirements as imposed by the parent
     * @param heightMeasureSpec vertical space requirements as imposed by th parent
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int twoThreeHeight = MeasureSpec.getSize(widthMeasureSpec) * THREE / TWO;
        int twoThreeHeightSpec =
                MeasureSpec.makeMeasureSpec(twoThreeHeight, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, twoThreeHeightSpec);
    }
}
