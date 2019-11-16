package com.joker.red.rain;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

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

    private RainPocketAdapter mAdapter;

    private Random mRandom;

    //下雨速度（每个红包进入的时间间隔：毫秒）
    private int rainSeed = 500;

    //周期：红包滑动时间
    private int rainDuration = 6000;

    //红包雨收起时间
    private int collapseDuration = 500;

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

    private View mOpenRedPocketView;


    public RedPocketRainView(@NonNull Context context) {
        super(context);
        Log.d("RedPocketRainView1", "mRedPocketWidth:" + mRedPocketWidth + ", mRedPocketHeight:" + mRedPocketHeight + ", rainDuration:" + rainDuration + ", rainSeed:" + rainSeed + ", mRedPocketSpace:" + mRedPocketSpace);

        AnimatorManager.getInstance().setOnAnimatorActionListener(mOnAnimatorActionListener);
    }

    public RedPocketRainView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RedPocketRainView);
        rainSeed = Float.valueOf(typedArray.getFloat(R.styleable.RedPocketRainView_rainSeed, 0.5f) * 1000).intValue();
        rainDuration = Float.valueOf(typedArray.getFloat(R.styleable.RedPocketRainView_rainDuration, 6f) * 1000).intValue();
        collapseDuration = Float.valueOf(typedArray.getFloat(R.styleable.RedPocketRainView_collapseDuration, 6f) * 1000).intValue();
        mRedPocketWidth = sp2px(typedArray.getDimension(R.styleable.RedPocketRainView_redPocketWidth, 100f));
        mRedPocketHeight = sp2px(typedArray.getDimension(R.styleable.RedPocketRainView_redPocketHeight, 150f));
        mRedPocketSpace = sp2px(typedArray.getDimension(R.styleable.RedPocketRainView_redPocketMinSpace, 10f));

        AnimatorManager.getInstance().setOnAnimatorActionListener(mOnAnimatorActionListener);

        Log.d("RedPocketRainView2", "mRedPocketWidth:" + mRedPocketWidth + ", mRedPocketHeight:" + mRedPocketHeight + ", rainDuration:" + rainDuration + ", rainSeed:" + rainSeed + ", collapseDuration:" + collapseDuration + ", mRedPocketSpace:" + mRedPocketSpace);
    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (AnimatorManager.getInstance().getAnimatorState() == AnimatorManager.ANIMATOR_STATE_RUNNING) {
                fadeInRedPocketView();
            }
        }
    };

    private void fadeInRedPocketView() {
        View convertView = getNextRedPocketView(curRedPocketPostion);
        int offsetX = getNextRedPocketViewOffsetX(lastRedPocketOffsetX);
        setCurRedPocketViewOffsetX(convertView, offsetX);
        this.addView(convertView);
        //更新position
        curRedPocketPostion++;
        if (curRedPocketPostion == mAdapter.getCount()) {
            curRedPocketPostion = 0;
        }
        //更新进入屏幕的x位置
        lastRedPocketOffsetX = offsetX;

        //开始滑落动画
        AnimatorManager.getInstance().startSliding(convertView, rainDuration, getScreenHieght());

        //循环添加view
        if (AnimatorManager.getInstance().getAnimatorState() == AnimatorManager.ANIMATOR_STATE_RUNNING) {
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

    private View getOpenRedPocketView(int position) {
        if (mOpenRedPocketView != null) {
            mOpenRedPocketView = mAdapter.onBindOpenRedPocketView(mOpenRedPocketView, position, RedPocketRainView.this);
        } else {
            mOpenRedPocketView = mAdapter.onCreateOpenRedPocketView(mOpenRedPocketView, position, RedPocketRainView.this);
        }
        return mOpenRedPocketView;
    }

    //显示选中的要打开的红包
    private void showOpenRedPocketView(int position) {
        View openView = getOpenRedPocketView(position);
        addView(openView);
        AnimatorManager.getInstance().startScaleOpen(openView, 1000);
    }

    private int selectedRedPocketPosition = -1;

    //红包click事件
    private OnTouchListener mOnTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (v.getTag() == null || !(v.getTag() instanceof Integer)) return false;
                int position = (Integer)v.getTag();
                //未消费click事件
                if (mOnItemClickListener != null && !mOnItemClickListener.onRedPocketItemClick(position, v)) {
                    selectedRedPocketPosition = position;
                    stay();
                }
            }
            return false;
        }
    };

    public void removeBigRedPocketView() {
        removeView(mOpenRedPocketView);
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

    public void start() {
        if (AnimatorManager.getInstance().getAnimatorState() != AnimatorManager.ANIMATOR_STATE_RUNNING) {
            AnimatorManager.getInstance().prepare();
            fadeInRedPocketView();
        }
    }

    public void pause() {
        AnimatorManager.getInstance().pauseAnimator();
    }

    public void resume() {
        if (AnimatorManager.getInstance().getAnimatorState() == AnimatorManager.ANIMATOR_STATE_PAUSE) {
            AnimatorManager.getInstance().resumelAnimator();
            fadeInRedPocketView();
        }
    }

    public void stop() {
        if (AnimatorManager.getInstance().getAnimatorState() == AnimatorManager.ANIMATOR_STATE_PAUSE) {
            AnimatorManager.getInstance().resumelAnimator();
        }
        AnimatorManager.getInstance().stopAnimator();
    }

    public void stay() {
        if (AnimatorManager.getInstance().getAnimatorState() == AnimatorManager.ANIMATOR_STATE_PAUSE) {
            AnimatorManager.getInstance().resumelAnimator();
        }
        AnimatorManager.getInstance().stayAnimator();
    }

    @Override
    public void onViewRemoved(View child) {
        super.onViewRemoved(child);
        if (child == mOpenRedPocketView) {
            selectedRedPocketPosition = -1;
            return;
        }
        child.setTag(null);
        if (mCacheViews != null) {
            mCacheViews.add(child);
        }

        Log.d(TAG,  "onViewRemoved:" + child + ", " + "childTag:" + child.getTag() + ", findViewByTag:" + findViewWithTag(child.getTag()));
    }


    private OnAnimatorActionListener mOnAnimatorActionListener = new OnAnimatorActionListener() {

        private View getAnimatorTargetView(ObjectAnimator animator) {
            if (animator.getTarget() != null  && animator.getTarget() instanceof View){
                return (View) animator.getTarget();
            }
            return null;
        }

        @Override
        public void onAnimatorResume(ObjectAnimator animator) {

        }

        @Override
        public void onAnimatorStart(ObjectAnimator animator) {

        }

        @Override
        public void onAnimatorPause(ObjectAnimator animator) {

        }

        @Override
        public void onAnimatorEnd(ObjectAnimator animator) {
            View view = getAnimatorTargetView(animator);
            Log.i(TAG, "position: " + view.getTag() + " onAnimatorEnd-------------------->");
            if (view != null) {
                removeView(view);
                view.setTranslationY(0f);
                view.setTranslationX(0f);
                view.setScaleX(1.0f);
                view.setScaleY(1.0f);
            }
        }

        @Override
        public void onAnimatorStay(ObjectAnimator animator) {
            View view = getAnimatorTargetView(animator);
            if (view != null) {
                Log.d(TAG,  "Position: " + view.getTag() + "开启回收动画..............");
            }
        }

        @Override
        public void onAnimatorStayAll() {
            Log.e(TAG, "animator stay all............");
            collapseScaleRedPocket();
        }

        @Override
        public void onAnimatorZeroAll() {
            showOpenRedPocketView(selectedRedPocketPosition);
            Log.e(TAG, "animator zero all............");
        }
    };

    private void collapseScaleRedPocket() {
        float targetX = (getScreenWidth() - mRedPocketWidth) / 2;
        float targetY = (getScreenHieght() - mRedPocketHeight) / 2;
        AnimatorManager.getInstance().zeroAnimator();
        for (int index = 0; index < getChildCount(); index++) {
            AnimatorManager.getInstance().startCollapse(getChildAt(index), collapseDuration, targetX, targetY);
        }
    }

    private OnPocketItemClickListener mOnItemClickListener = new OnPocketItemClickListener() {
        @Override
        public boolean onRedPocketItemClick(int position, View redPocketView) {
            return false;
        }
    };

    public void setAdapter(RainPocketAdapter adapter) {
        this.mAdapter = adapter;
    }


    private int sp2px(float spValue) {
        final float fontScale = getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public void setOnItemClickListener(OnPocketItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
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
}
