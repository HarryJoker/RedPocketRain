package com.harry.joker.redpocketrain;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.joker.red.rain.RedPocketRainView;

import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity {

    private RedPocketRainView mRedPocketRainView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        if (v.getId() == R.id.tv_destroy) {
            mRedPocketRainView.destroy();
        }
    }

    private RedPocketRainView.OnItemClickListener mOnItemClickListener = new RedPocketRainView.OnItemClickListener() {
        @Override
        public boolean onItemClick(int position, View redView) {
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



    class MyRedRainPocketAdapter extends RedPocketRainView.RedRainPocketViewAdapter {

        private Drawable mRedPocketDrawable;

        public MyRedRainPocketAdapter(Context context) {
            super(context);
            mRedPocketDrawable = getDrawable(R.mipmap.ic_redpocket);
        }

        @Override
        public int getCount() {
            return 20;
        }

        @Override
        public View getOpenRedPocketView(ViewGroup parent) {
            View view = getLayoutInflater().inflate(R.layout.layout_open_pocket, parent, false);
            view.findViewById(R.id.iv_big_redpocket).setOnClickListener(openClick);
            view.findViewById(R.id.iv_close).setOnClickListener(closeClick);
            return view;
        }

        @Override
        public View getRedPocketView(View redView, int position, ViewGroup parent) {
            if (redView != null) return redView;
            ImageView view = new ImageView(MainActivity.this);
            view.setImageDrawable(mRedPocketDrawable);
            view.setScaleType(ImageView.ScaleType.CENTER);
            return view;
        }
    }


}