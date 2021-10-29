package com.life.android.utils;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.DimenRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

public class HorizontalMarginItemDecoration extends RecyclerView.ItemDecoration {
    private Context context;
    @DimenRes
    private int horizontalMarginInDp;

    private final int horizontalMarginInPx;

    public HorizontalMarginItemDecoration(Context context, int horizontalMarginInDp) {
        this.context = context;
        this.horizontalMarginInDp = horizontalMarginInDp;
        horizontalMarginInPx = (int) context.getResources().getDimension(horizontalMarginInDp);
    }

    @Override
    public void getItemOffsets(@NonNull @NotNull Rect outRect, @NonNull @NotNull View view, @NonNull @NotNull RecyclerView parent,
                               @NonNull @NotNull RecyclerView.State state) {
        outRect.right = horizontalMarginInPx;
        outRect.left = horizontalMarginInPx;
    }
}
