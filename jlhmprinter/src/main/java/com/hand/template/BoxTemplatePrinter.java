package com.hand.template;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.hand.baselibrary.config.Hippius;
import com.hand.baselibrary.utils.LogUtils;
import com.hand.jlhmprinter.R;
import com.hand.template.bean.BoxInfo;

import org.json.JSONObject;

import cpcl.PrinterHelper;

/**
 * 集装箱模版打印
 */
public class BoxTemplatePrinter extends TemplatePrinter {

    private BoxInfo mBoxInfo;
    private Bitmap logo;

    public BoxTemplatePrinter() {
        logo = BitmapFactory
                .decodeResource(Hippius.getApplicationContext().getResources(), R.drawable.jl_logo);
    }

    private void initInfo(JSONObject object) {
        try {
            mBoxInfo = getGson().fromJson(object.toString(), BoxInfo.class);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void print(JSONObject object) {
        initInfo(object);
        try {
            PrinterHelper.printAreaSize("0", "200", "200", "800", "1");
            PrinterHelper.printBitmapCPCL(logo, 460, 40, 0, 0, 0);
            PrinterHelper.Line("440", "0", "440", "800", "1");//最长的横线
           // PrinterHelper.Line("540", "400", "440", "400", "1");//中间竖线
            PrinterHelper.SetBold("1");
            PrinterHelper.SetMag("2", "2");
            PrinterHelper.Text(PrinterHelper.TEXT270, "7", "0", "520", "360", "箱号:" + mBoxInfo.getBoxId());
            PrinterHelper.Barcode(PrinterHelper.VBARCODE,PrinterHelper.code128,"3","3","240","150","600",false,"7","0","5",mBoxInfo.getBoxBarCode());
            PrinterHelper.Line("100", "0", "100", "800", "1");//最长的横线
            PrinterHelper.SetBold("1");
            PrinterHelper.SetMag("2", "2");
            PrinterHelper.Text(PrinterHelper.TEXT270, "7", "0", "75", "100", "箱号条码:" + mBoxInfo.getBoxBarCode());
            PrinterHelper.Form();
            PrinterHelper.Print();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
