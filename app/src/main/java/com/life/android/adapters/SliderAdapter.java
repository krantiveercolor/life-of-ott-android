package com.life.android.adapters;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.TextView;

import com.life.android.DetailsActivity;
import com.life.android.LoginActivity;
import com.life.android.R;
import com.life.android.WebViewActivity;
import com.life.android.models.home_content.Slide;
import com.life.android.utils.PicassoUtil;
import com.life.android.utils.PreferenceUtils;
import com.github.islamkhsh.CardSliderAdapter;
import com.makeramen.roundedimageview.RoundedImageView;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import static com.life.android.utils.MyAppClass.getContext;

public class SliderAdapter extends CardSliderAdapter<Slide> {

    private RoundedImageView imageView;

    public SliderAdapter(@NotNull ArrayList<Slide> items, Context context) {
        super(items);
        this.context = context;
    }

    private Context context;

    @Override
    public void bindView(int i, @NotNull View view, @Nullable final Slide slide) {
        if (slide != null) {
            TextView textView = view.findViewById(R.id.textView);

            textView.setText(slide.getTitle());
            imageView = view.findViewById(R.id.imageview);
            if (slide.getThumbImageLink() != null) {
                PicassoUtil.with(context).load(slide.getThumbImageLink(),
                        imageView, R.drawable.poster_placeholder);
            } else if (slide.getImageLink() != null) {
                PicassoUtil.with(context).load(slide.getImageLink(),
                        imageView, R.drawable.poster_placeholder);
            }

      /*      Glide.with(context)
                    .load(slide.getThumbImageLink())
                    .apply(new RequestOptions().override(400, 600))
                    .
                    .into(imageView);*/

            View lyt_parent = view.findViewById(R.id.lyt_parent);
            lyt_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new Thread(() -> {
                        if (slide.getActionId() != null && slide.getActionType() != null) {
                            if (slide.getActionType().equalsIgnoreCase("tvseries") || slide.getActionType().equalsIgnoreCase("movie")) {
                                if (PreferenceUtils.isMandatoryLogin(getContext())) {
                                    if (PreferenceUtils.isLoggedIn(getContext())) {
                                        Intent intent = new Intent(getContext(), DetailsActivity.class);
                                        intent.putExtra("vType", slide.getActionType());
                                        intent.putExtra("id", slide.getActionId());
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        getContext().startActivity(intent);
                                        ((Activity) context).overridePendingTransition(R.anim.enter, R.anim.exit);
                                    } else {
                                        getContext().startActivity(new Intent(getContext(), LoginActivity.class));
                                        ((Activity) context).overridePendingTransition(R.anim.enter, R.anim.exit);
                                    }
                                } else {
                                    Intent intent = new Intent(getContext(), DetailsActivity.class);
                                    intent.putExtra("vType", slide.getActionType());
                                    intent.putExtra("id", slide.getActionId());
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    getContext().startActivity(intent);
                                    ((Activity) context).overridePendingTransition(R.anim.enter, R.anim.exit);
                                }

                            } else if (slide.getActionType().equalsIgnoreCase("webview")) {
                                Intent intent = new Intent(getContext(), WebViewActivity.class);
                                intent.putExtra("url", slide.getActionUrl());
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                getContext().startActivity(intent);
                                ((Activity) context).overridePendingTransition(R.anim.enter, R.anim.exit);
                            } else if (slide.getActionType().equalsIgnoreCase("external_browser")) {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(slide.getActionUrl()));
                                browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                getContext().startActivity(browserIntent);
                                ((Activity) context).overridePendingTransition(R.anim.enter, R.anim.exit);

                            } else if (slide.getActionType().equalsIgnoreCase("tv")) {
                                if (PreferenceUtils.isMandatoryLogin(getContext())) {
                                    if (PreferenceUtils.isLoggedIn(getContext())) {
                                        Intent intent = new Intent(getContext(), DetailsActivity.class);
                                        intent.putExtra("vType", slide.getActionType());
                                        intent.putExtra("id", slide.getActionId());
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        getContext().startActivity(intent);
                                        ((Activity) context).overridePendingTransition(R.anim.enter, R.anim.exit);
                                    } else {
                                        getContext().startActivity(new Intent(getContext(), LoginActivity.class));
                                        ((Activity) context).overridePendingTransition(R.anim.enter, R.anim.exit);
                                    }
                                } else {
                                    Intent intent = new Intent(getContext(), DetailsActivity.class);
                                    intent.putExtra("vType", slide.getActionType());
                                    intent.putExtra("id", slide.getActionId());
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    getContext().startActivity(intent);
                                    ((Activity) context).overridePendingTransition(R.anim.enter, R.anim.exit);
                                }
                            }
                        }
                    }).start();
                    //  ((Activity) context).overridePendingTransition(R.anim.enter, R.anim.exit);
                }
            });
        }
    }


    @Override
    public int getItemContentLayout(int i) {
        return R.layout.slider_item;
    }
}
