package com.hand.template;

import android.os.Handler;
import android.os.Looper;

import com.hand.plugin.HMPrinter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class TemplatePrinterFactory {
    public enum TEMPLATE {
        BOX, MATERIAL, ORDER,SHB_GOODS
    }

    private boolean active = false;
    private Handler mHandler;
    private HMPrinter mHMPrinter;

    public TemplatePrinterFactory(HMPrinter hmPrinter) {
        mHMPrinter = hmPrinter;
        mHandler = new Handler(Looper.getMainLooper());
    }

    private ArrayList<TemplatePrinter> templatePrinters = new ArrayList<>();

    private Queue<JSONObject> printQueue = new LinkedList<>();

    public void addTask(JSONArray array) {
        for (int i = 0; i < array.length(); i++) {
            try {
                printQueue.offer(array.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private Runnable mPrinterRunnable = new Runnable() {
        @Override
        public void run() {
            if (printQueue.size() > 0) {
                try {
                    JSONObject object = printQueue.poll();
                    String type = object.getString("type");
                    TemplatePrinter templatePrinter = getTemplatePrinter(type);
                    if (templatePrinter != null) {
                        templatePrinter.print(object);
                        mHandler.postDelayed(this, 300);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                active = false;
            }
        }
    };

    public TemplatePrinter getTemplatePrinter(String type) {
        if (TEMPLATE.BOX.name().equals(type)) {
            return getTemplatePrinter(BoxTemplatePrinter.class);
        } else if (TEMPLATE.MATERIAL.name().equals(type)) {
            return getTemplatePrinter(MaterialTemplatePrinter.class);
        } else if (TEMPLATE.ORDER.name().equals(type)) {
            return getTemplatePrinter(OrderTemplatePrinter.class);
        } else if (TEMPLATE.SHB_GOODS.name().equals(type)){
            return getTemplatePrinter(SHBGoodsTemplatePrinter.class);
        } else {
            return null;
        }
    }

    public void execute() {
        if (!active) {
            active = true;
            mHandler.postDelayed(mPrinterRunnable, 10);
        }
    }

    public TemplatePrinter getTemplatePrinter(Class clasz) {
        for (TemplatePrinter templatePrinter : templatePrinters) {
            if (templatePrinter.getClass().getName().equals(clasz.getName())) {
                return templatePrinter;
            }
        }
        try {
            TemplatePrinter templatePrinter = (TemplatePrinter) clasz.newInstance();
            templatePrinter.setHMPrinter(mHMPrinter);
            return templatePrinter;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }

}
