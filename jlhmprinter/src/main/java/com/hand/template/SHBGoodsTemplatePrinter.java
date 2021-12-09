package com.hand.template;

import android.graphics.Bitmap;

import com.google.gson.Gson;
import com.hand.baselibrary.utils.LogUtils;
import com.hand.template.bean.OrderInfo;
import com.hand.template.bean.SHBGoodsInfo;

import org.json.JSONObject;

import cpcl.PrinterHelper;

//60*40mm   480*320
public class SHBGoodsTemplatePrinter extends TemplatePrinter {
    private static final String TAG = "SHBGoodsTemplatePrinter";
    private SHBGoodsInfo mSHBGoodsInfo;
    private int middle = 195;
    private int divider = 42;

    private void initInfo(JSONObject object) {
        try {
            mSHBGoodsInfo = getGson().fromJson(object.toString(), SHBGoodsInfo.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void print(JSONObject object) {
        initInfo(object);
        LogUtils.e(TAG, new Gson().toJson(mSHBGoodsInfo));
        try {
            Bitmap codeBitMap = null;
            if (mHMPrinter != null) {
                codeBitMap = mHMPrinter.crateBitmap(190, mSHBGoodsInfo.getQrcode());
                LogUtils.e(TAG, codeBitMap.getWidth() + "==========");
            }
            PrinterHelper.printAreaSize("0", "200", "200", "320", "1");
            PrinterHelper.Box("13", "13", "467", "307", "1");//外框
            //使用了BOX但是底部还是少一条线，补一条线
            PrinterHelper.Line("13", "306", "467", "306", "1");

            PrinterHelper.Line("13", String.valueOf(13 + divider), "467", String.valueOf(13 + divider), "1");
            PrinterHelper.Line("13", String.valueOf(13 + divider * 2), "467", String.valueOf(13 + divider * 2), "1");
            PrinterHelper.Line(String.valueOf(middle), "13", String.valueOf(middle), "307", "1");
            //SKU
            PrinterHelper.Line(String.valueOf(middle), String.valueOf(13 + divider * 3), "467", String.valueOf(13 + divider * 3), "1");
            //名称
            PrinterHelper.Line(String.valueOf(middle), String.valueOf(13 + divider * 4), "467", String.valueOf(13 + divider * 4), "1");
            //图号
            PrinterHelper.Line(String.valueOf(middle), String.valueOf(13 + divider * 5), "467", String.valueOf(13 + divider * 5), "1");
            //批次
            PrinterHelper.Line(String.valueOf(middle), String.valueOf(13 + divider * 6), "467", String.valueOf(13 + divider * 6), "1");
            PrinterHelper.SetBold("1");
            PrinterHelper.SetMag("1", "1");
            PrinterHelper.Text(PrinterHelper.TEXT, "7", "0", "15",
                    String.valueOf(13 + 9), mSHBGoodsInfo.getTuo());
            PrinterHelper.Text(PrinterHelper.TEXT, "7", "0", "70",
                    String.valueOf(13 + divider + 9), mSHBGoodsInfo.getDes());

            PrinterHelper.Text(PrinterHelper.TEXT, "7", "0", String.valueOf(middle + 5),
                    String.valueOf(13 + divider + 9), "日期 " + mSHBGoodsInfo.getDate());

            PrinterHelper.Text(PrinterHelper.TEXT, "7", "0", String.valueOf(middle + 5),
                    String.valueOf(13 + divider * 2 + 9), "SKU " + mSHBGoodsInfo.getSku());
            PrinterHelper.Text(PrinterHelper.TEXT, "7", "0", String.valueOf(middle + 5),
                    String.valueOf(13 + divider * 3 + 9), "名称 " + mSHBGoodsInfo.getName());
            PrinterHelper.Text(PrinterHelper.TEXT, "7", "0", String.valueOf(middle + 5),
                    String.valueOf(13 + divider * 4 + 9), "图号 " + mSHBGoodsInfo.getPicNum());
            PrinterHelper.Text(PrinterHelper.TEXT, "7", "0", String.valueOf(middle + 5),
                    String.valueOf(13 + divider * 5 + 9), "批次 " + mSHBGoodsInfo.getBatch());
            PrinterHelper.Text(PrinterHelper.TEXT, "7", "0", String.valueOf(middle + 5),
                    String.valueOf(13 + divider * 6 + 9), "数量 " + mSHBGoodsInfo.getNum());
            PrinterHelper.SetMag("1", "1");
            PrinterHelper.Text(PrinterHelper.TEXT, "7", "0", String.valueOf(middle + 5),
                    String.valueOf(13 + 9), "T:" + mSHBGoodsInfo.getSupplier());
            if (codeBitMap != null) {
                PrinterHelper.printBitmapCPCL(codeBitMap, middle - codeBitMap.getWidth() -3, 26 + divider * 2, 0, 0, 0);
            }
            PrinterHelper.Form();
            PrinterHelper.Print();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
