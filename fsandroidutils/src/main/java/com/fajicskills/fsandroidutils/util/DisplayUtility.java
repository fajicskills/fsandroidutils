package com.fajicskills.fsandroidutils.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.fajicskills.fsandroidutils.R;
import com.fajicskills.fsandroidutils.models.User;


/**
 * Created by etiennelawlor on 12/19/15.
 */
public class DisplayUtility {

    // region Utility Methods
    public static int dp2px(Context context, int dp) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        DisplayMetrics displaymetrics = new DisplayMetrics();
        display.getMetrics(displaymetrics);

        return (int) (dp * displaymetrics.density + 0.5f);
    }

    public static int px2dp(Context context, int px) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        DisplayMetrics displaymetrics = new DisplayMetrics();
        display.getMetrics(displaymetrics);

        return (int) (px / displaymetrics.density + 0.5f);
    }

    public static int getScreenWidth(Context context){
        Point size = new Point();
        ((Activity)context).getWindowManager().getDefaultDisplay().getSize(size);
        return size.x;
    }

    public static boolean isInLandscapeMode(Context context){
        boolean isLandscape = false;
        if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            isLandscape =  true;
        }
        return isLandscape;
    }

    public static void hideKeyboard(Context context, View view) {
        if (context != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) context.getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                if (view != null) {
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        }
    }

    public static void showKeyboard(Context context, View view) {
        view.requestFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public static int getPressedAvatarBackgroundColor(User user, Context context){
        int avatarBackgroundColor = 0;
        String name = user.getName();
        int hash = 7;
        int firstNameLength = name.length();
        for (int i = 0; i < firstNameLength; i++) {
            hash = hash*31 + name.charAt(i);
        }

        int modulo = Math.abs(hash) % 17;
        String[] avatarBgColors = context.getResources().getStringArray(R.array.pressed_avatar_bg_colors);
        avatarBackgroundColor = Color.parseColor(avatarBgColors[modulo]);
        return avatarBackgroundColor;
    }

    public static int getDefaultAvatarBackgroundColor(User user, Context context){
        int avatarBackgroundColor = 0;
        String name = user.getName();
        int hash = 7;
        int firstNameLength = name.length();
        for (int i = 0; i < firstNameLength; i++) {
            hash = hash*31 + name.charAt(i);
        }

        int modulo = Math.abs(hash) % 17;
        String[] avatarBgColors = context.getResources().getStringArray(R.array.default_avatar_bg_colors);
        avatarBackgroundColor = Color.parseColor(avatarBgColors[modulo]);
        return avatarBackgroundColor;
    }
    // endregion
}
