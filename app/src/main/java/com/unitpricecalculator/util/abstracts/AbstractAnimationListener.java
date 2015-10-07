package com.unitpricecalculator.util.abstracts;

import android.view.animation.Animation;

public abstract class AbstractAnimationListener implements Animation.AnimationListener {

    @Override
    public void onAnimationStart(Animation animation) {}

    @Override
    public void onAnimationEnd(Animation animation) {}

    @Override
    public void onAnimationRepeat(Animation animation) {}
}
