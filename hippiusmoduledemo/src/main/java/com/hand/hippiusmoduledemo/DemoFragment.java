package com.hand.hippiusmoduledemo;

import android.graphics.Color;
import android.os.Bundle;

import android.view.View;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;

import com.google.gson.Gson;
import com.hand.baselibrary.config.ConfigKeys;
import com.hand.baselibrary.config.Hippius;
import com.hand.baselibrary.config.SPConfig;
import com.hand.baselibrary.fragment.BaseFragment;
import com.hand.baselibrary.fragment.BasePresenter;
import com.hand.baselibrary.fragment.IBaseFragment;
import com.hand.baselibrary.rxbus.RxBus;
import com.hand.baselibrary.rxbus.UserInfoEvent;
import com.hand.baselibrary.widget.ExpandableTextView;

import org.json.JSONArray;
import org.json.JSONException;

import androidx.annotation.Nullable;
import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

//使用ARouter配置路由 为避免冲突请使用module名+自定义名称
@Route(path = "/hippiusmoduledemo/demofragment")
public class DemoFragment extends BaseFragment {

    @BindView(R2.id.tv_token)
    ExpandableTextView tvToken;
    @BindView(R2.id.tv_userId)
    TextView tvUserId;
    @BindView(R2.id.tv_userInfo)
    ExpandableTextView tvUserInfo;
    @BindView(R2.id.tv_employeeInfo)
    ExpandableTextView tvEmployeeInfo;
    @BindView(R2.id.tv_str1)
    TextView tvStr1;
    @BindView(R2.id.tv_str2)
    TextView tvStr2;
    @BindView(R2.id.v_color1)
    View vColor1;
    @BindView(R2.id.v_color2)
    View vColor2;
    @BindView(R2.id.tv_area_1)
    TextView tvArea1;
    @BindView(R2.id.tv_area_2)
    TextView tvArea2;
    @BindView(R2.id.tv_area_3)
    TextView tvArea3;

    @BindView(R2.id.tv_radio_area_1)
    TextView tvRadioArea1;
    @BindView(R2.id.tv_radio_area_2)
    TextView tvRadioArea2;
    @BindView(R2.id.tv_radio_area_3)
    TextView tvRadioArea3;
    @BindView(R2.id.tv_long_area1)
    TextView tvLongArea1;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    @Override
    protected void onBindView(@Nullable Bundle savedInstanceState) {
        tvToken.setText(Hippius.getAccessToken());
        tvUserId.setText(SPConfig.getString(ConfigKeys.SP_USERID,""));
        tvStr1.setText(BuildConfig.demo_str1);
        tvStr2.setText(BuildConfig.demo_str2);
        vColor1.setBackgroundColor(Color.parseColor(BuildConfig.demo_color1));
        vColor2.setBackgroundColor(Color.parseColor(BuildConfig.demo_color2));
        String demo_checkbox_area = BuildConfig.demo_checkbox_area;
        try {
            JSONArray array = new JSONArray(demo_checkbox_area);
            for(int i = 0;i<array.length();i++){
              if(array.optString(i,"").equals("area1")){
                  tvArea1.setVisibility(View.VISIBLE);
              }else if(array.optString(i,"").equals("area2")){
                  tvArea2.setVisibility(View.VISIBLE);
              }else if(array.optString(i,"").equals("area3")){
                  tvArea3.setVisibility(View.VISIBLE);
              }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String radio_area = BuildConfig.demo_radio_area;
        if("area1".equals(radio_area)){
            tvRadioArea1.setVisibility(View.VISIBLE);
        }else if("area2".equals(radio_area)){
            tvRadioArea2.setVisibility(View.VISIBLE);
        }else if("area3".equals(radio_area)){
            tvRadioArea3.setVisibility(View.VISIBLE);
        }
        tvLongArea1.setText(BuildConfig.demo_textarea1);
        compositeDisposable.add(RxBus.get().registerSticky(UserInfoEvent.class,
                AndroidSchedulers.mainThread(),this::onUserInfoEvent));
    }

    private void onUserInfoEvent(UserInfoEvent userInfoEvent){
        Gson mGson = new Gson();
        if(userInfoEvent!=null&&userInfoEvent.getUserInfo()!=null){
            tvUserInfo.setText(mGson.toJson(userInfoEvent.getUserInfo()));
        }
        if(userInfoEvent!=null&&userInfoEvent.getTenantUserInfos()!=null){
            tvEmployeeInfo.setText(mGson.toJson(userInfoEvent.getTenantUserInfos()));
        }
    }

    @Override
    protected int setStatusBarView() {
        return R.id.status_bar_view;
    }

    @Override
    protected Object setLayout() {
        return R.layout.demo_fragment_test;
    }

    @Override
    protected BasePresenter createPresenter() {
        return null;
    }

    @Override
    protected IBaseFragment createView() {
        return null;
    }

    @Override
    public void onDestroyView() {
        if(compositeDisposable!=null){
            compositeDisposable.clear();
        }
        super.onDestroyView();
    }
}
