package com.mykj.qupingfang.alipay;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.alipay.sdk.app.PayTask;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

/**
 * Created by xht on 2016/5/6.
 */
public class AliPay {

    public static final int SUCCESS = 1;
    public static final int FAILE = 2;
    public static final int PAYING = 3;

    // 用户id
    private String uId;
    // 购买的资源id
    private String resourceId;
    // 物品名称
    private String goodsName;
    // 物品描述
    private String goodsDetails;
    // 物品价格
    private String price;

    // 订单id(是一随机数且和时间有关，一个订单只许有一个id)
    private String orderId;
    // 订单信息
    private String orderInfo;

    // 上下文
    private Context context;

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    alipay();
                    break;
                case SUCCESS:
                    Toast.makeText(context, "支付宝支付成功", Toast.LENGTH_SHORT).show();
                    break;
                case PAYING:
                    Toast.makeText(context, "支付宝支付结果确认中", Toast.LENGTH_SHORT).show();
                    break;
                case FAILE:
                    Toast.makeText(context, "支付宝支付失败", Toast.LENGTH_SHORT).show();
                    break;
            }
        }

    };

    /**
     * AliPay构造函数
     *
     * @param uId          用户id
     * @param resourceId   资源id
     * @param goodsName    物品名称
     * @param goodsDetails 物品描述
     * @param price        物品价格
     */
    //-----------------------------------------------------------------------------------------------------------------------------------------------头(1)
    public AliPay(Context context, String uId, String resourceId, String goodsName, String goodsDetails, String price) {

        this.context = context;
        this.uId = uId;
        this.resourceId = resourceId;
        this.goodsName = goodsName;
        this.goodsDetails = goodsDetails;
        this.price = price;

        // 创建订单id
        orderId = getOutTradeNo();//跳（2）
        // 创建订单信息
        orderInfo = getOrderInfo(goodsName, goodsDetails, price);//跳（3）
        // 签名
        String sign = sign(orderInfo);//跳（4）

        try {
            // 仅需对sign 做URL编码
            sign = URLEncoder.encode(sign, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        orderInfo = orderInfo + "&sign=\"" + sign + "\"&" + getSignType();
        //之后在主活动调用pay()方法
    }
//----------------------------------------------------------------------------------------------------------------------------------------------尾（1）
    /**
     * 对外提供的支付方法
     */
    //--------------------------------------------------------------------------------------------------------------------------------------------头（5）
    public void pay() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String url = "http://api.lovek12.com" + uId + "&fee="
                            + price + "&charge_type=ali&order_id=" + orderId + "&resource_id=" + resourceId;
                    final String jsonStr = httpByGet(url, "utf-8");
                    Log.i("TAG", "请求服务器后返回的数据==" + jsonStr);
                    JSONObject obj = new JSONObject(jsonStr);
                    String status = obj.getString("status");
                    //1代表支付成功
                    if (status.equals("1")) {
                        Message msg = new Message();
                        msg.what = 0;
                        handler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    Message msg = new Message();
                    msg.what = FAILE;
                    handler.sendMessage(msg);
                }
            }
        }).start();

    }
//--------------------------------------------------------------------------------------------------------------------------------------------尾（5）
    /**
     * 具体的支付宝支付
     */
    private void alipay() {
        Runnable payRunnable = new Runnable() {

            @Override
            public void run() {
                Message msg = new Message();

                PayTask alipay = new PayTask((Activity) context);
                final String result = alipay.pay(orderInfo,true);

                PayResult payResult = new PayResult(result);
                /**
                 * 同步返回的结果必须放置到服务端进行验证（验证的规则请看https://doc.open.alipay.com/doc2/
                 * detail.htm?spm=0.0.0.0.xdvAU6&treeId=59&articleId=103665&
                 * docType=1) 建议商户依赖异步通知
                 * 支付宝返回此次支付结果及加签，建议对支付宝签名信息拿签约时支付宝提供的公钥做验签
                 */
                String resultInfo = payResult.getResult();

                String resultStatus = payResult.getResultStatus();

                // 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
                if (TextUtils.equals(resultStatus, "9000")) {

                    msg.what = SUCCESS;
                    handler.sendMessage(msg);
                } else {
                    // 判断resultStatus 为非“9000”则代表可能支付失败
                    // “8000”代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
                    if (TextUtils.equals(resultStatus, "8000")) {
                        msg.what = PAYING;
                        handler.sendMessage(msg);
                    } else {
                        // 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
                        msg.what = FAILE;
                        handler.sendMessage(msg);
                    }
                }
            }
        };
        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }
    //--------------------------------------------------------------------------------------------------------------------------------------------头（6）
    // ===================网络请求=====================
    public static String httpByGet(String dataurl, String encoding) throws IOException {
        URL url = new URL(dataurl);
        HttpURLConnection openConnection = (HttpURLConnection) url.openConnection();
        openConnection.setConnectTimeout(5000);
        openConnection.setReadTimeout(5000);
        openConnection.setRequestProperty("charset", encoding);
        openConnection.setRequestMethod("GET");
        if (openConnection.getResponseCode() == 200) {
            InputStream inputStream = openConnection.getInputStream();
            return read(inputStream, encoding);
        }
        return null;
    }
    //--------------------------------------------------------------------------------------------------------------------------------------------尾（6）
//--------------------------------------------------------------------------------------------------------------------------------------------头（7）
    private static String read(InputStream inputStream, String encoding) throws IOException {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        int len = 0;
        byte[] buf = new byte[1024];
        while ((len = inputStream.read(buf)) != -1) {
            bo.write(buf, 0, len);
        }
        bo.flush();
        bo.close();
        return new String(bo.toByteArray(), encoding);
    }
//--------------------------------------------------------------------------------------------------------------------------------------------尾（7）
    // ===================以下是支付宝支付所需的工具方法=========================

    /**
     * 创建订单信息
     */
    //----------------------------------------------------------------------------------------------------------------------------------------------头（3）
    private String getOrderInfo(String subject, String body, String price) {

        // 签约合作者身份ID，即商户PID
        String orderInfo = "partner=" + "\"" + AlipayConfig.PARTNER + "\"";

        // 签约卖家支付宝账号
        orderInfo += "&seller_id=" + "\"" + AlipayConfig.SELLER + "\"";

        // 商户网站唯一订单号
        orderInfo += "&out_trade_no=" + "\"" + orderId + "\"";

        // 商品名称
        orderInfo += "&subject=" + "\"" + subject + "\"";

        // 商品详情
        orderInfo += "&body=" + "\"" + body + "\"";

        // 商品金额
        orderInfo += "&total_fee=" + "\"" + price + "\"";

        // 服务器异步通知页面路径
        // 注意：下面的url为回调接口，请求支付宝后，支付宝会向服务器请求以下的url 所以项目不同，以下的url应不同
        orderInfo += "&notify_url=" + "\"" + "http://api.lovek12.com/api-ali-order-notify.shtml" + "\"";

        // 服务接口名称， 固定值
        orderInfo += "&service=\"mobile.securitypay.pay\"";

        // 支付类型， 固定值
        orderInfo += "&payment_type=\"1\"";

        // 参数编码， 固定值
        orderInfo += "&_input_charset=\"utf-8\"";

        // 设置未付款交易的超时时间
        // 默认30分钟，一旦超时，该笔交易就会自动被关闭。
        // 取值范围：1m～15d。
        // m-分钟，h-小时，d-天，1c-当天（无论交易何时创建，都在0点关闭）。
        // 该参数数值不接受小数点，如1.5h，可转换为90m。
        orderInfo += "&it_b_pay=\"30m\"";

        // extern_token为经过快登授权获取到的alipay_open_id,带上此参数用户将使用授权的账户进行支付
        // orderInfo += "&extern_token=" + "\"" + extern_token + "\"";

        // 支付宝处理完请求后，当前页面跳转到商户指定页面的路径，可空
        orderInfo += "&return_url=\"m.alipay.com\"";

        // 调用银行卡支付，需配置此参数，参与签名， 固定值 （需要签约《无线银行卡快捷支付》才能使用）
        // orderInfo += "&paymethod=\"expressGateway\"";

        return orderInfo;
    }
    //--------------------------------------------------------------------------------------------------------------------------------------------------尾（3）
    /*
     * 生成商户订单号，该值在商户端应保持唯一（可自定义格式规范）
     */
    //--------------------------------------------------------------------------------------------------------------------------------------------头（2）
    private String getOutTradeNo() {
        SimpleDateFormat format = new SimpleDateFormat("MMddHHmmss", Locale.getDefault());
        Date date = new Date();
        String key = format.format(date);

        Random r = new Random();
        key = key + r.nextInt();
        //String.substring(int begin,int end)提取begin和end之间的字符串部分
        key = key.substring(0, 15);
        return key;
    }
//-----------------------------------------------------------------------------------------------------------------------------------------------尾（2）
    /**
     * 对订单信息进行签名
     *
     * @param content 待签名订单信息
     */
    //-------------------------------------------------------------------------------------------------------------------------------------------头（4）
    private String sign(String content) {
        return SignUtils.sign(content, AlipayConfig.RSA_PRIVATE);
    }
    //-------------------------------------------------------------------------------------------------------------------------------------------尾（4）
    /**
     * 获取签名方式
     */
    //-----------------------------------------------------------------------头（5）
    private String getSignType() {
        return "sign_type=\"RSA\"";
    }
    //------------------------------------------------------------------------尾（5）
}
