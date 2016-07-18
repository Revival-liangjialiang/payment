package com.mykj.qupingfang.wxpay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

/**
 * Created by xht on 2016/5/5.
 */
public class AppRegister extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final IWXAPI msgApi = WXAPIFactory.createWXAPI(context, null);
        // 将该app注册到微信
        msgApi.registerApp(WXPayConfig.APP_ID);
        Log.i("TAG","微信支付广播接收器---AppRegister");
    }
}
