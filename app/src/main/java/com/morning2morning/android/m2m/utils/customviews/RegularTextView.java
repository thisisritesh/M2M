package com.morning2morning.android.m2m.utils.customviews;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */
public class RegularTextView extends AppCompatTextView {


    public RegularTextView(Context context) {
        super(context);
        initTypeface(context);
    }

    public RegularTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initTypeface(context);
    }

    public RegularTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initTypeface(context);
    }

    private void initTypeface(Context context){
        Typeface typeface = Typeface.createFromAsset(context.getAssets(),"fonts/poppins_regular.ttf");
        setTypeface(typeface);
    }

}
