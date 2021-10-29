package com.life.android.adapters;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;

import androidx.viewpager.widget.ViewPager;

import java.lang.reflect.Field;

/**
 * Created by krantiveer 02/08/2021 .
 */
public class CustomViewPager extends ViewPager {

        private boolean isPagingEnabled = true;

    public CustomViewPager(Context context) {
        super(context);
        setMyScroller();
    }

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        setMyScroller();
    }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
        /*return this.isPagingEnabled && super.onTouchEvent(event);*/
        return false;
    }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent event) {
        return false;
        /*return this.isPagingEnabled && super.onInterceptTouchEvent(event);*/
    }

        public void setPagingEnabled(boolean b) {
        this.isPagingEnabled = b;
    }



/*
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        // Never allow swiping to switch between pages
        return false;
    }*/

   /* @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Never allow swiping to switch between pages
        return false;
    }
*/
        //down one is added for smooth scrolling

        private void setMyScroller() {
        try {
            Class<?> viewpager = ViewPager.class;
            Field scroller = viewpager.getDeclaredField("mScroller");
            scroller.setAccessible(true);
            scroller.set(this, new MyScroller(getContext()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

        public class MyScroller extends Scroller {
            public MyScroller(Context context) {
                super(context, new DecelerateInterpolator());
            }

            @Override
            public void startScroll(int startX, int startY, int dx, int dy, int duration) {
                super.startScroll(startX, startY, dx, dy, 350 /*1 secs*/);
            }
        }
    }