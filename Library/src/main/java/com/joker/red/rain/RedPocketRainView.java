package com.joker.red.rain;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Author: harryjoker
 * Created on: 2019-11-13 18:24
 * Description:
 */
public class RedPocketRainView extends FrameLayout{

    private String TAG = "RedPocketRainView";

    private int mScreenHieght = 0;

    private int mScreenWidth = 0;

    private List<View> mCacheViews = new ArrayList<>();

    private RedRainPocketViewAdapter mAdapter;

    private Random mRandom;

    //下雨速度（每个红包进入的时间间隔：毫秒）
    private int rainSeed = 500;

    //周期：红包滑动时间
    private int rainDuration = 6000;

    //红包宽度
    private int mRedPocketWidth = 200;

    //红包高度
    private int mRedPocketHeight = 300;

    private int bigRedPocketWidth = 600;

    private int bigRedPocketHeight = 900;

    //红包最小间距
    private int mRedPocketSpace = 20;

    //当前进入redPocket的position
    private int curRedPocketPostion = 0;

    //上一个红包的offsetX
    private int lastRedPocketOffsetX = 0;

    private static final int ANIMATOR_STATE_RUNNING = 0X00F1;

    private static final int ANIMATOR_STATE_END = 0X00F3;
    //下雨状态
    private int runState = 0;

    private View mOpenRedPocketView;

    private OnRedPocketItemClickListener mOnItemClickListener = new OnRedPocketItemClickListener() {
        @Override
        public boolean onRedPocketItemClick(int position, View redPocketView) {
            return false;
        }
    };


    public RedPocketRainView(@NonNull Context context) {
        super(context);
    }

    public RedPocketRainView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RedPocketRainView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.RedPocketRainView, defStyleAttr, 0);
        rainSeed = Float.valueOf(typedArray.getFloat(R.styleable.RedPocketRainView_rainSeed, 0.5f) * 1000).intValue();
        rainDuration = Float.valueOf(typedArray.getFloat(R.styleable.RedPocketRainView_rainDuration, 6f) * 1000).intValue();
        mRedPocketWidth = sp2px(typedArray.getDimension(R.styleable.RedPocketRainView_redPocketWidth, 100f));
        mRedPocketHeight = sp2px(typedArray.getDimension(R.styleable.RedPocketRainView_redPocketHeight, 150f));
        mRedPocketSpace = sp2px(typedArray.getDimension(R.styleable.RedPocketRainView_redPocketMinSpace, 10f));

        Log.d("RedPocketRainView", "mRedPocketWidth:" + mRedPocketWidth + ", mRedPocketHeight:" + mRedPocketHeight + ", rainDuration:" + rainDuration + ", rainSeed:" + rainSeed + ", mRedPocketSpace:" + mRedPocketSpace);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            fadeInRedPocketView();
        }
    };


    public void start() {
        runState = ANIMATOR_STATE_RUNNING;
        fadeInRedPocketView();
    }

    private void fadeInRedPocketView() {
        View convertView = getNextRedPocketView(curRedPocketPostion);
        int offsetX = getNextRedPocketViewOffsetX(lastRedPocketOffsetX);
        setCurRedPocketViewOffsetX(convertView, offsetX);
        addView(convertView);
        //更新position
        curRedPocketPostion++;
        if (curRedPocketPostion == mAdapter.getCount()) {
            curRedPocketPostion = 0;
        }
        //更新进入屏幕的x位置
        lastRedPocketOffsetX = offsetX;

        startPropertyAnim(convertView);

        //循环添加view：下雨
        if (runState == ANIMATOR_STATE_RUNNING) {
            mHandler.sendEmptyMessageDelayed(0, rainSeed);
        }
    }

    //设置redPocket View的offset
    private void setCurRedPocketViewOffsetX(View view, int offsetX) {
        if (view == null) return;
        FrameLayout.LayoutParams layoutParams;
        if (view.getLayoutParams() == null) {
            layoutParams = new FrameLayout.LayoutParams(mRedPocketWidth, mRedPocketHeight);
        } else {
            layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
        }
        view.setY(0.0f);
        layoutParams.leftMargin = offsetX;
        view.setLayoutParams(layoutParams);
    }

    //获取adapter的redPocket或者缓存使用
    private View getNextRedPocketView(int position) {
        View convertView = null;
        if (mCacheViews.size() > 0) {
            convertView = mAdapter.onBindRedPocketView(mCacheViews.remove(0), position, this);
        } else {
            convertView = mAdapter.onCreateRedPocketView(convertView, position, this);
        }
        convertView.setClickable(true);
        convertView.setOnTouchListener(mOnTouchListener);
        convertView.setTag(position);
        return convertView;
    }

    // 动画实际执行
    private void startPropertyAnim(View view) {
        if (view == null) return;
        ObjectAnimator anim = ObjectAnimator.ofFloat(view, "translationY", view.getTranslationY(), getScreenHieght(), getScreenHieght());
        anim.setDuration(rainDuration);
        anim.addUpdateListener(mAnimatorListener);
        // 正式开始启动执行动画
        anim.start();
    }

    //动画监听
    private ValueAnimator.AnimatorUpdateListener mAnimatorListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float value = (Float) animation.getAnimatedValue();
            ObjectAnimator objectAnimator = (ObjectAnimator) animation;
            if (objectAnimator == null || objectAnimator.getTarget() == null) return;
            View redPocketView = (View) objectAnimator.getTarget();
            if (runState == ANIMATOR_STATE_RUNNING) {
                //动画执行中，发出窗口回收
                if (value >= getScreenHieght()) {
                    animation.cancel();
                    Log.d("", "fade out redPocket view, updateValue: " + value + ", curPosition:" + redPocketView.getTag() + ", view:" + redPocketView);
                    fadeOutRedPocketView(redPocketView);
                }
            } else if (runState == ANIMATOR_STATE_END) {
                //结束动画
                animation.cancel();
                fadeOutRedPocketView(redPocketView);
            }
//            else if (runState == ANIMATOR_STATE_PAUSE) {
//                //暂停动画
//                if (animation != null) {
//                    animation.pause();
//                    collectPauseAnimator(animation);
//                }
//            }
        }
    };

    //收集缓存暂停动画播放器
//    private void collectPauseAnimator(ValueAnimator animator) {
//        if (animator == null) return;
//        if (pauseAnimators == null) pauseAnimators = new ArrayList<>();
//        pauseAnimators.add(animator);
//    }

    //下雨超出window，回收view
    private void fadeOutRedPocketView(View redPocketView) {
        if (redPocketView == null) return;
        removeView(redPocketView);
    }


    //计算生成redPocket view的offsetX
    private int getNextRedPocketViewOffsetX(int lastOffetX) {
        if (mRandom == null) mRandom = new Random();
        if (lastOffetX == 0) return mRandom.nextInt(getScreenWidth() - mRedPocketWidth);

        int left = mRandom.nextInt(getScreenWidth() - mRedPocketWidth);

        if (Math.abs(lastOffetX - left) >= (mRedPocketWidth + mRedPocketSpace) && left + mRedPocketWidth <= getScreenWidth()) {
            return left;
        } else {
            int leftDistance = lastOffetX - mRedPocketWidth - mRedPocketSpace;
            int rightDistance = getScreenWidth() - lastOffetX - 2 * mRedPocketWidth - mRedPocketSpace;
            if (leftDistance > rightDistance) {
                left = mRandom.nextInt(leftDistance);
            } else {
                left = mRandom.nextInt(rightDistance) + lastOffetX + mRedPocketWidth;
            }
            return left;
        }
    }
    //红包click事件
    private OnTouchListener mOnTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            if (event.getAction() == MotionEvent.ACTION_UP) {
                Object object = v.getTag();
                if (object != null && object instanceof Integer) {
                    int position = (Integer)object;
                    stop();
                    if (mOnItemClickListener.onRedPocketItemClick(position, v)) return false;

                    if (mOpenRedPocketView != null) {
                        mOpenRedPocketView = mAdapter.onBindOpenRedPocketView(mOpenRedPocketView, position, RedPocketRainView.this);
                    } else {
                        mOpenRedPocketView = mAdapter.onCreateOpenRedPocketView(mOpenRedPocketView, position, RedPocketRainView.this);
                    }

                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (mOpenRedPocketView != null) {
                                addView(mOpenRedPocketView);
                            }
                        }
                    }, 300);

                }

            }
            return false;
        }
    };

    public void removeBigRedPocketView() {
        removeView(mOpenRedPocketView);
    }


    private AnimatorSet startOpenRedPocketAnim(View small, View big) {
        if (small == null || big == null) return null;

        Point point = getStartPoint(big);


        float sX = big.getMeasuredWidth() * 1f / small.getWidth();
        float sY = big.getMeasuredHeight() * 1f / small.getHeight();

        Log.d("Anmimator", "small x:" + small.getX() + ", y:" + small.getY() +  ", width:" + small.getWidth() + ", height:" + small.getHeight() + "\n" +
                "big x:" + point.x + ", y:" + point.y + ", width:" + big.getMeasuredWidth() + ", heigth:" + big.getMeasuredHeight() + "\n" +
                "anim scaleX:" + sX + ", scaleY:" + sY + "offsetX:" + (sX - small.getX()) + ", offsetY:" + (sY - small.getY())) ;

        AnimatorSet animatorSet = new AnimatorSet();//组合动画
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(big, "scaleX", 1f, sX);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(big, "scaleY", 1f, sY);

        ObjectAnimator translationX = new ObjectAnimator().ofFloat(big,"translationX",small.getTranslationX(), sX - small.getX());
        ObjectAnimator translationY = new ObjectAnimator().ofFloat(big,"translationY",small.getTranslationY(), sY - small.getY());

        animatorSet.setDuration(5000);
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.play(scaleX).with(scaleY);//两个动画同时开始
        animatorSet.playTogether(scaleX, scaleY, translationX, translationY);
//        animatorSet.start();
        return animatorSet;
    }

    private Point getStartPoint(View view) {
        if (view == null) return null;
        int x = (getScreenWidth() - view.getMeasuredWidth()) / 2;
        int y = (getScreenHieght() - view.getMeasuredHeight()) / 2;
        return new Point(x, y);
    }

    private int getScreenHieght() {
        if (mScreenHieght == 0) {
            WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics metrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(metrics);
            mScreenHieght = metrics.heightPixels;
            mScreenWidth = metrics.widthPixels;
        }
        return mScreenHieght;
    }

    public int getScreenWidth() {
        if (mScreenWidth == 0) {
            WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics metrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(metrics);
            mScreenHieght = metrics.heightPixels;
            mScreenWidth = metrics.widthPixels;
        }
        return mScreenWidth;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        destroy();
    }

    //销毁红包雨view
    public void destroy() {
        stop();
        mCacheViews.clear();
        mOnItemClickListener = null;
        mAdapter = null;
        mHandler = null;
        mOnTouchListener = null;
        mOpenRedPocketView = null;
        mRandom = null;
        //...
    }

    public void stop() {
        runState = ANIMATOR_STATE_END;
        removeAllViews();
    }

    @Override
    public void onViewRemoved(View child) {
        super.onViewRemoved(child);
        if (child == mOpenRedPocketView) return;
        child.setTag(null);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) child.getLayoutParams();
//        Logger.d("Fade out View: " + child.getY() + ", " + child.getPivotY() + ", " + child.getRotationY() + ", " + child.getTranslationY() + ", " + layoutParams.leftMargin + ", " + layoutParams.topMargin + ", " + layoutParams.rightMargin + "," + layoutParams.bottomMargin);
//        Logger.d("onViewRemoved:" + child + ", " + "childTag:" + child.getTag() + ", findViewByTag:" + findViewWithTag(child.getTag()));
        mCacheViews.add(child);
    }

    public void setAdapter(RedRainPocketViewAdapter adapter) {
        this.mAdapter = adapter;
    }


    private int sp2px(float spValue) {
        final float fontScale = getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public void setOnItemClickListener(OnRedPocketItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }


    //    private List<ValueAnimator> pauseAnimators;

//    private static final int ANIMATOR_STATE_PAUSE   = 0X00F2;

//    public void pause() {
//        runState = ANIMATOR_STATE_PAUSE;
//    }
//
//    public void resume() {
//        if (runState == ANIMATOR_STATE_PAUSE && pauseAnimators != null) {
//            Iterator<ValueAnimator> iterator = pauseAnimators.iterator();
//            while (iterator.hasNext()) {
//                iterator.next().resume();
//                iterator.remove();
//            }
//        }
//        runState = ANIMATOR_STATE_RUNNING;
//        mHandler.sendEmptyMessage(MESSAGE_FADE_IN_REDPOCKET);
//    }
}
