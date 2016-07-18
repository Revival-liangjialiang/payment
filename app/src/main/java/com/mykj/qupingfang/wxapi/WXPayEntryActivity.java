package com.mykj.qupingfang.wxapi;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.mykj.qupingfang.R;
import com.mykj.qupingfang.wxpay.WXPayConfig;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

/**
 * Created by xht on 2016/5/5.
 * 微信支付回调页面
 */
public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler, View.OnClickListener {

    // 标题栏返回箭头
    private ImageView iv_wx_pay_callback_back;

    private IWXAPI api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wx_pay_finish);

        iv_wx_pay_callback_back = (ImageView) findViewById(R.id.iv_wx_pay_callback_back);
        iv_wx_pay_callback_back.setOnClickListener(this);

        api = WXAPIFactory.createWXAPI(this, WXPayConfig.APP_ID);
        api.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq req) {

    }
//回调方法
    @Override
    public void onResp(BaseResp resp) {
        Log.d("TAG", "onPayFinish, errCode = " + resp.errCode);
        /**
         * 0表示支付成功，-1表示支付中断，-2表示支付失败
         * resp.getType() 返回int型
         */
        if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("提示");
            builder.setMessage("微信支付结果：" + String.valueOf(resp.errCode));
            builder.show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_wx_pay_callback_back:
                finish();
                break;
        }
    }
}
