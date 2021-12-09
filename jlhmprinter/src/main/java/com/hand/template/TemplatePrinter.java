package com.hand.template;

import com.google.gson.Gson;
import com.hand.plugin.HMPrinter;

import org.json.JSONObject;

public abstract class TemplatePrinter {
    private Gson mGson;
    public HMPrinter mHMPrinter;

    abstract public void print(JSONObject object);

    public void setHMPrinter(HMPrinter hMPrinter) {
        this.mHMPrinter = hMPrinter;
    }

    public Gson getGson() {
        if(mGson == null){
            mGson = new Gson();
        }
        return mGson;
    }
}
