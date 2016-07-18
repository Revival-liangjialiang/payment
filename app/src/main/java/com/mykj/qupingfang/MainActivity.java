package com.mykj.qupingfang;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mykj.qupingfang.alipay.AliPay;
import com.mykj.qupingfang.wxpay.WXPay;

/**
 * 微信支付
 * 支付宝支付
 */
public class MainActivity extends Activity implements View.OnClickListener {

    // 微信支付
    private RelativeLayout rl_pay_wx;
    // 支付宝支付
    private RelativeLayout rl_pay_ali;
    // 微信支付选项
    private CheckBox cb_wx_pay;
    // 支付宝支付选项
    private CheckBox cb_ali_pay;
    // 支付的金额
    private TextView tv_pay_money;
    // 支付按钮
    private TextView tv_pay_now;

    // 上下文
    private Context mContext = MainActivity.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    /**
     * 初始化UI
     */
    private void initView() {
        rl_pay_wx = (RelativeLayout) findViewById(R.id.rl_pay_wx);
        rl_pay_wx.setOnClickListener(this);
        rl_pay_ali = (RelativeLayout) findViewById(R.id.rl_pay_ali);
        rl_pay_ali.setOnClickListener(this);

        cb_wx_pay = (CheckBox) findViewById(R.id.cb_wx_pay);
        cb_wx_pay.setChecked(true);
        cb_ali_pay = (CheckBox) findViewById(R.id.cb_ali_pay);

        tv_pay_money = (TextView) findViewById(R.id.tv_pay_money);

        tv_pay_now = (TextView) findViewById(R.id.tv_pay_now);
        tv_pay_now.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_pay_wx:
                cb_wx_pay.setChecked(true);
                cb_ali_pay.setChecked(false);
                break;
            case R.id.rl_pay_ali:
                cb_ali_pay.setChecked(true);
                cb_wx_pay.setChecked(false);
                break;
            case R.id.tv_pay_now:
                if (cb_wx_pay.isChecked()) {
                    wxPay();
                } else if (cb_ali_pay.isChecked()) {
                    aliPay();
                }
                break;
        }
    }

    private void aliPay() {
        Log.i("TAG", "选择支付宝支付------");
        /*
     * 支付宝支付
     *第二个参数：用户id
     * 第三个参数：资源id
     * 第四个参数：物品名称
     * 第五个参数：物品描述
     * 第六个参数  物品价格
     */
        AliPay pay = new AliPay(mContext, "303", "0", "趣平方", "趣平方", "0.01");
        pay.pay();
    }

       /* 微信支付
        *第二个参数为用户id
        * 第三个参数为资源id
        * 第四个参数为Vip月数
        * 第五个为金额
         */
    private void wxPay() {
        Log.i("TAG", "选择微信支付------");
        WXPay wxPay = new WXPay(mContext, "303", "1", "0", "0.01");
    }
}