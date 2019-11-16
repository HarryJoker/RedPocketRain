package com.harry.joker.redpocketrain;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.joker.red.rain.OnPocketItemClickListener;
import com.joker.red.rain.RedPocketRainView;
import com.joker.red.rain.RainPocketAdapter;

public class MainActivity extends AppCompatActivity {

    private RedPocketRainView mRedPocketRainView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStatusBarTranprent(this);

        setContentView(R.layout.activity_main);

        mRedPocketRainView = findViewById(R.id.redpocketrain);

        mRedPocketRainView.setOnItemClickListener(mOnItemClickListener);

        mRedPocketRainView.setAdapter(new MyRedRainPocketAdapter(this));
    }

    public void click(View v) {
        if (v.getId() == R.id.tv_start) {
            mRedPocketRainView.start();
        }

        if (v.getId() == R.id.tv_stop) {
            mRedPocketRainView.stop();
        }

        if (v.getId() == R.id.tv_resume) {
            mRedPocketRainView.resume();
        }

        if (v.getId() == R.id.tv_stay) {
//            mRedPocketRainView.destroy();
            mRedPocketRainView.stay();
        }

        if (v.getId() == R.id.tv_pause) {
            mRedPocketRainView.pause();

        }
    }

    private OnPocketItemClickListener mOnItemClickListener = new OnPocketItemClickListener() {
        @Override
        public boolean onRedPocketItemClick(int position, View redView) {
//          startActivity(new Intent(RedRainActivity.this, RedDetailActivity.class));
            return false;
        }
    };


    private View.OnClickListener closeClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mRedPocketRainView.removeBigRedPocketView();
        }
    };

    private View.OnClickListener openClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(MainActivity.this, "开红包，跳走自己业务", Toast.LENGTH_SHORT).show();
        }
    };


    class MyRedRainPocketAdapter extends RainPocketAdapter {

        private Drawable mRedPocketDrawable;

        public MyRedRainPocketAdapter(Context context) {
            mRedPocketDrawable = getDrawable(R.mipmap.ic_redpocket);
        }

        @Override
        public int getCount() {
            return 20;
        }

        @Override
        public View onCreateOpenRedPocketView(View convertView, int position, ViewGroup parent) {
            View view = getLayoutInflater().inflate(R.layout.layout_open_pocket, parent, false);
            view.findViewById(R.id.iv_big_redpocket).setOnClickListener(openClick);
            view.findViewById(R.id.iv_close).setOnClickListener(closeClick);
            return view;
        }

        @Override
        public View onBindOpenRedPocketView(View convertView, int position, ViewGroup parent) {
            return convertView;
        }

        @Override
        public View onCreateRedPocketView(View convertView, int position, ViewGroup parent) {
            ImageView view = new ImageView(MainActivity.this);
            view.setImageDrawable(mRedPocketDrawable);
            view.setScaleType(ImageView.ScaleType.CENTER);
            return view;
        }

        @Override
        public View onBindRedPocketView(View convertView, int position, ViewGroup parent) {
            return convertView;
        }

    }




    private void setStatusBarTranprent(Activity activity) {
        Window window = getWindow();
        View decorView = window.getDecorView();
        //两个 flag 要结合使用，表示让应用的主体内容占用系统状态栏的空间
        int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        decorView.setSystemUiVisibility(option);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        //设置状态栏为透明，否则在部分手机上会呈现系统默认的浅灰色
        window.setStatusBarColor(Color.TRANSPARENT);
        //导航栏颜色也可以考虑设置为透明色
        window.setNavigationBarColor(Color.TRANSPARENT);
    }

}