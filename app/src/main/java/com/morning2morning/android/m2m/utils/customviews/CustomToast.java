package com.morning2morning.android.m2m.utils.customviews;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.morning2morning.android.m2m.R;

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */
public class CustomToast {
    public void showToastMessage(final Activity context, String message) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.custom_toast, null);
        Typeface tf = Typeface.createFromAsset(context.getAssets(), "fonts/poppins_regular.ttf");

        RegularTextView text = (RegularTextView) layout.findViewById(R.id.text);
        text.setText(message);
        text.setTypeface(tf);
        context.runOnUiThread(new Runnable() {
            public void run() {
                Toast toast = new Toast(context);
                toast.setGravity(Gravity.BOTTOM, 0, 30);
                /* toast.setMargin(0,30);*/
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.setView(layout);
                toast.show();
            }
        });


    }
}