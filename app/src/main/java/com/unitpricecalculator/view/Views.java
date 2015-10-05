package com.unitpricecalculator.view;

import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public final class Views {

    public static void expandView(final View v) {
        expandView(v, null);
    }

    public static void expandView(final View v, @Nullable Animation.AnimationListener listener) {
        v.measure(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? LayoutParams.WRAP_CONTENT
                        : (int) (targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        a.setFillAfter(true);
        // 1dp/ms
        a.setDuration((int) (5 * targetHeight / v.getContext().getResources().getDisplayMetrics().density));

        if (listener != null) {
            a.setAnimationListener(listener);
        }

        v.startAnimation(a);
    }

    public static void collapseView(final View v) {
        collapseView(v, null);
    }

    public static void collapseView(final View v, @Nullable Animation.AnimationListener listener) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    v.setVisibility(View.GONE);
                } else {
                    v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        a.setFillAfter(true);
        // 1dp/ms
        a.setDuration(5 * (int) (initialHeight / v.getContext().getResources().getDisplayMetrics().density));

        if (listener != null) {
            a.setAnimationListener(listener);
        }

        v.startAnimation(a);
    }
}
