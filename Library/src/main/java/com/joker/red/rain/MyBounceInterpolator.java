package com.joker.red.rain;

import android.util.Log;

/**
 * Author: harryjoker
 * Created on: 2019-11-16 20:22
 * Description:
 */
public class MyBounceInterpolator implements android.view.animation.Interpolator {
    double mAmplitude = 1;
    double mFrequency = 10;

    MyBounceInterpolator() {

    }

    private static float bounce(float t) {
        return t * t * 8.0f;
    }

    MyBounceInterpolator(double amp, double freq) {
        mAmplitude = amp;
        mFrequency = freq;
    }

    public float getInterpolation(float t) {
        t *= 1.1226f;
        if (t < 0.3535f) return bounce(t);
        else if (t < 0.7408f) return bounce(t - 0.54719f) + 0.7f;
        else if (t < 0.9644f) return bounce(t - 0.8526f) + 0.9f;
        else return bounce(t - 1.0435f) + 0.95f;
    }
}
