package com.joker.red.rain;

import android.animation.ObjectAnimator;

/**
 * Author: harryjoker
 * Created on: 2019-11-16 18:07
 * Description:
 */
public interface OnAnimatorActionListener {
    void onAnimatorResume(ObjectAnimator animator);
    void onAnimatorStart(ObjectAnimator animator);
    void onAnimatorPause(ObjectAnimator animator);
    void onAnimatorEnd(ObjectAnimator animator);
    void onAnimatorStay(ObjectAnimator animator);
    void onAnimatorStayAll();
    void onAnimatorZeroAll();
}
