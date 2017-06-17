package com.fajicskills.fsandroidutils.util;

import android.content.Context;
import android.graphics.Typeface;

import java.util.HashMap;
import java.util.Map;


public class FontCache {

    private static Map<String, Typeface> fontCache = new HashMap<>(8);

    public static Typeface getTypeface(String fontname, Context context) {
        Typeface typeface = fontCache.get(fontname);

        if (typeface == null) {
            try {
                typeface = Typeface.createFromAsset(context.getAssets(), String.format("fonts/%s", fontname));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            fontCache.put(fontname, typeface);
        }

        return typeface;
    }
}
