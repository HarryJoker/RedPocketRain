package com.joker.red.rain;

import android.view.View;
import android.view.ViewGroup;

/**
 * Author: harryjoker
 * Created on: 2019-11-15 20:11
 * Description:
 */
public abstract class RainPocketAdapter {

    public abstract int getCount();

    public abstract View onCreateOpenRedPocketView(View convertView, int position, ViewGroup parent);

    public abstract View onBindOpenRedPocketView(View convertView, int position, ViewGroup parent);

    public abstract View onCreateRedPocketView(View convertView, int position, ViewGroup parent);

    public abstract View onBindRedPocketView(View convertView, int position, ViewGroup parent);
}
