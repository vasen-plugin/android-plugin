package com.hand;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import com.hand.baselibrary.activity.BaseActivity;
import com.hand.baselibrary.activity.BasePresenter;
import com.hand.baselibrary.activity.IBaseActivity;
import com.hand.jlhmprinter.R;
import com.hand.jlhmprinter.R2;
import com.hand.template.MaterialTemplatePrinter;

import androidx.annotation.Nullable;
import butterknife.BindView;

public class TestActivity extends BaseActivity {
    public static void startActivity(Activity activity){
        Intent intent = new Intent(activity,TestActivity.class);
        activity.startActivity(intent);
    }
    @Override
    protected Object setLayout() {
        return R.layout.actity_test;
    }
    @BindView(R2.id.iv_image)
    ImageView ivImage;
    @BindView(R2.id.iv_image2)
    ImageView ivImage2;
    @Override
    protected void onBindView(@Nullable Bundle savedInstanceState) {
      //  ivImage.setImageBitmap(MaterialTemplatePrinter.bitmap);
       // ivImage2.setImageBitmap(MaterialTemplatePrinter.tempLogo);
    }

    @Override
    protected BasePresenter createPresenter() {
        return null;
    }

    @Override
    protected IBaseActivity createView() {
        return null;
    }
}
