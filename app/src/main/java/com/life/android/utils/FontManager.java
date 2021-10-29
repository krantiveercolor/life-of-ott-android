package com.life.android.utils;

import android.content.Context;
import android.graphics.Typeface;

import androidx.core.content.res.ResourcesCompat;

import com.life.android.R;

import java.util.HashMap;
import java.util.Map;

public class FontManager {

    private final Map<String, Typeface> fontCache = new HashMap<String, Typeface>();
    private static FontManager instance = null;
    private final Context mContext;

    private FontManager(Context mContext2) {
        mContext = mContext2;
    }

    public synchronized static FontManager getInstance(Context mContext) {

        if (instance == null) {
            instance = new FontManager(mContext);
        }
        return instance;
    }

    public Typeface loadFont(String font) {

        if (!fontCache.containsKey(font)) {
            fontCache.put(font, ResourcesCompat.getFont(mContext, R.font.lato));
        }
        return fontCache.get(font);
    }
}
