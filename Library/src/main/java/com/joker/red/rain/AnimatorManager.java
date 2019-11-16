package com.joker.red.rain;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.util.Log;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.FrameLayout;

import java.util.HashSet;
import java.util.Set;

/**
 * Author: harryjoker
 * Created on: 2019-11-16 15:45
 * Description:
 */
public class AnimatorManager {

    private String TAG = "RedPocketAnim";

    public static final int ANIMATOR_STATE_PAUSE   = 0X00F2;

    public static final int ANIMATOR_STATE_RUNNING = 0X00F1;

    public static final int ANIMATOR_STATE_STOP    = 0X00F3;

    public static final int ANIMATOR_STATE_STAY    = 0X00F4;

    public static final int ANIMATOR_STATE_ZERO    = 0X00F5;

    private Set<ObjectAnimator> mActiveObjectAnimators = new HashSet<>();

    //下雨状态
    private int mAnimatorState = 0;

    private static AnimatorManager instance;

    private OnAnimatorActionListener mOnAnimatorActionListener;

    private AnimatorManager() {

    }

    public static AnimatorManager getInstance() {
        if (instance == null) {
            synchronized (AnimatorManager.class) {
                if (instance == null) {
                    instance = new AnimatorManager();
                }
            }
        }
        return instance;
    }


    public void setOnAnimatorActionListener(OnAnimatorActionListener onAnimatorActionListener) {
        mOnAnimatorActionListener = onAnimatorActionListener;
    }

    public int getAnimatorState() {
        return mAnimatorState;
    }

    public void pauseAnimator() {
        mAnimatorState = ANIMATOR_STATE_PAUSE;
    }

    public void stopAnimator() {
        mAnimatorState = ANIMATOR_STATE_STOP;
    }

    //stay状况,activeAnimators没有活动的，给予stayAll通知，进行切换动画
    public void stayAnimator() {
        mAnimatorState = ANIMATOR_STATE_STAY;
    }

    public void zeroAnimator() {
        mAnimatorState = ANIMATOR_STATE_ZERO;
    }

    public void resumelAnimator() {
        if (mAnimatorState != ANIMATOR_STATE_PAUSE) return;
        for (ObjectAnimator objectAnimator : mActiveObjectAnimators) {
            objectAnimator.resume();
            mOnAnimatorActionListener.onAnimatorResume(objectAnimator);
        }
        mAnimatorState = ANIMATOR_STATE_RUNNING;
    }

    public void prepare() {
        if (mAnimatorState == ANIMATOR_STATE_PAUSE) {
            resumelAnimator();
        }
        mAnimatorState = ANIMATOR_STATE_RUNNING;
    }

    //下雨动画
    public void startSliding(View view, int duration, int targetY) {
        if (view == null) return;
        ObjectAnimator anim = ObjectAnimator.ofFloat(view, "translationY", view.getTranslationY(), targetY);
        anim.setDuration(duration);
        anim.addUpdateListener(mAnimatorUpdateListener);
        anim.addListener(mAnimatorListener);
//        anim.setInterpolator(new MyBounceInterpolator());
        anim.start();
    }

    //大红包动画
    public void startScaleOpen(View openRedPocketView, int duration) {
        if (openRedPocketView == null) return;
        AnimatorSet animatorSet = new AnimatorSet();//组合动画
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(openRedPocketView, "scaleX", 0f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(openRedPocketView, "scaleY", 0f, 1.0f);
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.setDuration(duration);
        animatorSet.setInterpolator(new BounceInterpolator());
        animatorSet.start();
    }

    private Animator.AnimatorListener mAnimatorListener = new Animator.AnimatorListener() {

        private int getAnimationViewPosition(Animator animator) {
            if (animator == null || !(animator instanceof ObjectAnimator)) return  -1;
            ObjectAnimator objectAnimator = (ObjectAnimator)animator;
            View animatorView = (View) objectAnimator.getTarget();
            Object tag = animatorView.getTag();
            if (tag != null && tag instanceof Integer) {
                return (Integer)tag;
            }
            return -1;
        }

        @Override
        public void onAnimationStart(Animator animation) {
             if (animation != null && animation instanceof ObjectAnimator) {
                mOnAnimatorActionListener.onAnimatorStart((ObjectAnimator) animation);
                mActiveObjectAnimators.add((ObjectAnimator) animation);
             }
            Log.d(TAG, "position:" + getAnimationViewPosition(animation) + " active size: " + mActiveObjectAnimators.size() + "  onAnimationStart--------------------->");
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            Log.d(TAG, "position:" + getAnimationViewPosition(animation) + " active size: " + mActiveObjectAnimators.size() + "  onAnimationEnd--------------------->");

            if (animation == null || !(animation instanceof ObjectAnimator)) return;

            mActiveObjectAnimators.remove(animation);

            if (mAnimatorState == ANIMATOR_STATE_STAY) {
                mOnAnimatorActionListener.onAnimatorStay((ObjectAnimator) animation);
                if (mActiveObjectAnimators.size() == 0) {
                    mOnAnimatorActionListener.onAnimatorStayAll();
                }
            } else if (mAnimatorState == ANIMATOR_STATE_ZERO) {
                mOnAnimatorActionListener.onAnimatorEnd((ObjectAnimator) animation);
                if (mActiveObjectAnimators.size() == 0) {
                    mOnAnimatorActionListener.onAnimatorZeroAll();
                }
            } else {
                mOnAnimatorActionListener.onAnimatorEnd((ObjectAnimator) animation);
            }
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            Log.d(TAG, "position:" + getAnimationViewPosition(animation) + "  onAnimationCancel--------------------->");
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
            Log.d(TAG, "position:" + getAnimationViewPosition(animation) + "  onAnimationRepeat--------------------->");
        }
    };

    //动画监听
    private ValueAnimator.AnimatorUpdateListener mAnimatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
//            float value = (Float) animation.getAnimatedValue();
//            ObjectAnimator objectAnimator = (ObjectAnimator) animation;
//            if (objectAnimator == null || objectAnimator.getTarget() == null) return;
//            View redPocketView = (View) objectAnimator.getTarget();
            if (mAnimatorState == ANIMATOR_STATE_STOP && animation != null) {
                //结束动画
                animation.cancel();
            } else if (mAnimatorState == ANIMATOR_STATE_PAUSE && animation != null) {
                //暂停动画
                animation.pause();
            } else if (mAnimatorState == ANIMATOR_STATE_STAY && animation != null) {
                //动画停止，停顿在当前动画状态
                animation.cancel();
            }
        }
    };


    //回收红包雨点动画
    public void startCollapse(View redPocketView, int duration, float targetX, float targetY) {
        if (redPocketView == null) return;

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)redPocketView.getLayoutParams();
        float distanceY =  targetY - redPocketView.getY();
        float distanceX = targetX - params.leftMargin;

        AnimatorSet animatorSet = new AnimatorSet();//组合动画
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(redPocketView, "scaleX", 1f, 0.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(redPocketView, "scaleY", 1f, 0.0f);
        //        ObjectAnimator alpha = ObjectAnimator.ofFloat(redPocketView, "alph",1f, 0);
        //X轴动画：终点位置减去view目前位置的x相对位置，计算到偏移量
        ObjectAnimator translationX = new ObjectAnimator().ofFloat(redPocketView,"translationX",redPocketView.getTranslationX(), distanceX);
        //Y轴动画：再次设置动画默认起点还是原来的translationY位置，相当于对第一次的动画的偏移距离的改写：如（第一次偏移1000，第二次设置200，不管view在什么位置，都会偏移到相对与view源位置的200的偏移距离上）
        ObjectAnimator translationY = new ObjectAnimator().ofFloat(redPocketView,"translationY", redPocketView.getTranslationY(), targetY);
        ObjectAnimator translationZ = new ObjectAnimator().ofFloat(redPocketView,"translationZ", redPocketView.getTranslationZ(), 300);
//       float maxDistance = Math.abs(distanceX) > Math.abs(distanceY) ? Math.abs(distanceX) : Math.abs(distanceY);
        animatorSet.setDuration(Float.valueOf(Math.abs(distanceY) / targetY * duration).intValue());
        scaleY.addListener(mAnimatorListener);
        animatorSet.playTogether(translationX, translationY, scaleX, scaleY, translationZ);
        animatorSet.start();
    }
}
