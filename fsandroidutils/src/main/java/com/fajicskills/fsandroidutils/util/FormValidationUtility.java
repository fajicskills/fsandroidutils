package com.fajicskills.fsandroidutils.util;

import android.text.TextUtils;


public class FormValidationUtility {

    public static boolean validateComment(String comment) {
        return !TextUtils.isEmpty(comment);
    }
}
