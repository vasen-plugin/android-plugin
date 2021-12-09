package com.hand.template;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;

import com.hand.baselibrary.config.Hippius;
import com.hand.baselibrary.utils.LogUtils;
import com.hand.baselibrary.utils.StringUtils;
import com.hand.jlhmprinter.R;
import com.hand.template.bean.MaterialInfo;

import org.json.JSONObject;

import cpcl.PrinterHelper;

/**
 * 物料模版打印
 */
public class MaterialTemplatePrinter extends TemplatePrinter {
    private static final String TAG = "MaterialTemplatePrinter";
    private MaterialInfo materialInfo;
    private Bitmap logo;
    private int interval = 53;
    private int base = 420;
    private int boldSize = 45;
    //public static Bitmap tempLogo;
    public MaterialTemplatePrinter() {
        logo = BitmapFactory
                .decodeResource(Hippius.getApplicationContext().getResources(), R.drawable.jl_logo);
      //  tempLogo = logo;
        LogUtils.e(TAG,logo.getWidth()+"====="+logo.getHeight());
    }

    private void initInfo(JSONObject object) {
        try {
            materialInfo = getGson().fromJson(object.toString(), MaterialInfo.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Bitmap replaceBitmapColor(Bitmap oldBitmap)
    {
        //相关说明可参考 http://xys289187120.blog.51cto.com/3361352/657590/
        Bitmap mBitmap = oldBitmap.copy(Bitmap.Config.ARGB_8888, true);
        //循环获得bitmap所有像素点
        int mBitmapWidth = mBitmap.getWidth();
        int mBitmapHeight = mBitmap.getHeight();
        int mArrayColorLengh = mBitmapWidth * mBitmapHeight;
        int[] mArrayColor = new int[mArrayColorLengh];
        int count = 0;
        for (int i = 0; i < mBitmapHeight; i++) {
            for (int j = 0; j < mBitmapWidth; j++) {
                //获得Bitmap 图片中每一个点的color颜色值
                //将需要填充的颜色值如果不是
                //在这说明一下 如果color 是全透明 或者全黑 返回值为 0
                //getPixel()不带透明通道 getPixel32()才带透明部分 所以全透明是0x00000000
                //而不透明黑色是0xFF000000 如果不计算透明部分就都是0了
                int color = mBitmap.getPixel(j, i);
                LogUtils.e(TAG,color+"===========");
                //将颜色值存在一个数组中 方便后面修改
                if (color == Color.WHITE) {
                    mBitmap.setPixel(j, i, Color.BLACK);  //将白色替换成透明色
                }else if(color == 0xFF000000){
                    mBitmap.setPixel(j,i,0x00FFFFFF);
                }

            }
        }
        return mBitmap;
    }

    private Bitmap adjustPhotoRotation(Bitmap bm, final int orientationDegree) {
        Matrix m = new Matrix();
        m.setRotate(orientationDegree, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        try {
            Bitmap bm1 = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
            return bm1;
        } catch (OutOfMemoryError ex) {
        }
        return null;

    }
   // public static Bitmap bitmap;
    @Override
    public void print(JSONObject object) {
        initInfo(object);
        LogUtils.e("PRINT_Material", getGson().toJson(materialInfo));
        Bitmap codeBitMap = null;
        if(mHMPrinter!=null){
            codeBitMap = mHMPrinter.crateBitmap(300,materialInfo.getQrcode());
            codeBitMap = adjustPhotoRotation(codeBitMap,90);
         //   bitmap = codeBitMap;
           // LogUtils.e(TAG,Color.WHITE+"==================");
           // bitmap = replaceBitmapColor(bitmap);
        }
        try {
            PrinterHelper.printAreaSize("0", "200", "200", "800", "1");
            PrinterHelper.printBitmapCPCL(logo, 460, 40, 0, 0, 0);
            PrinterHelper.SetBold("1");
            PrinterHelper.SetMag("2", "2");
            PrinterHelper.Text(PrinterHelper.TEXT270, "7", "0", "520", "300", "物料条码");
            
            PrinterHelper.Text(PrinterHelper.TEXT270, "3", "0", String.valueOf(base), "30", "物料编码：");
            PrinterHelper.Text(PrinterHelper.TEXT270, "3", "0", String.valueOf(base - interval), "30", "材料/型号：");
            PrinterHelper.Text(PrinterHelper.TEXT270, "3", "0", String.valueOf(base - interval * 2), "30", "焊丝批次：");
            PrinterHelper.Text(PrinterHelper.TEXT270, "3", "0", String.valueOf(base - interval * 3), "30", "焊丝炉号：");
            PrinterHelper.Text(PrinterHelper.TEXT270, "3", "0", String.valueOf(base - interval * 4), "30", "采购订单：");
            PrinterHelper.Text(PrinterHelper.TEXT270, "3", "0", String.valueOf(base - interval * 5), "30", "供应商：");
            PrinterHelper.Text(PrinterHelper.TEXT270, "3", "0", String.valueOf(base - interval * 6), "30", "物料描述：");
            PrinterHelper.SetBold("0");
            PrinterHelper.Text(PrinterHelper.TEXT270, "3", "0", String.valueOf(base), String.valueOf(boldSize * 5), materialInfo.getMaterialId());
            PrinterHelper.Text(PrinterHelper.TEXT270, "3", "0", String.valueOf(base - interval), String.valueOf((int) (boldSize * 5.5)), materialInfo.getModel());
            PrinterHelper.Text(PrinterHelper.TEXT270, "3", "0", String.valueOf(base - interval * 2), String.valueOf((int) (boldSize * 5)), materialInfo.getWeldingWirePatch());
            PrinterHelper.Text(PrinterHelper.TEXT270, "3", "0", String.valueOf(base - interval * 3), String.valueOf((int) (boldSize * 5)), materialInfo.getWeldingWireHeatNum());
            PrinterHelper.Text(PrinterHelper.TEXT270, "3", "0", String.valueOf(base - interval * 4), String.valueOf((int) (boldSize * 5)), materialInfo.getPurchaseOrderId());
            String supplier = null;
            if (materialInfo.getSupplier().length() > 14) {
                supplier = materialInfo.getSupplier().substring(0, 14) + "...";
            } else {
                supplier = materialInfo.getSupplier();
            }
            PrinterHelper.Text(PrinterHelper.TEXT270, "3", "0", String.valueOf(base - interval * 5), String.valueOf((int) (boldSize * 4)), supplier);
            PrinterHelper.Text(PrinterHelper.TEXT270, "3", "0", String.valueOf(base - interval * 6), String.valueOf(boldSize * 5), materialInfo.getMaterialDes());
           // PrinterHelper.PrintQR(PrinterHelper.BARCODE, "340", "550", "1", "8", materialInfo.getQrcode());
            if(codeBitMap!=null){
                PrinterHelper.printBitmapCPCL(codeBitMap, 340, 550, 0, 0, 0);
            }
            String qrcode0,qrcode1=null;
            if (materialInfo.getQrcode().length() > 9){
                qrcode0 = materialInfo.getQrcode().substring(0,9);
                qrcode1 = materialInfo.getQrcode().substring(9);
            }else{
                qrcode0 = materialInfo.getQrcode();
            }
            PrinterHelper.SetMag("1", "1");
            PrinterHelper.Text(PrinterHelper.TEXT270, "7", "0", "330", "580", qrcode0);
            if(!StringUtils.isEmpty(qrcode1)){
                PrinterHelper.Text(PrinterHelper.TEXT270, "7", "0", "300", "600", qrcode1);
            }
            PrinterHelper.Form();
            PrinterHelper.Print();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
