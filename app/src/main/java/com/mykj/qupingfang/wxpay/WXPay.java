package com.mykj.qupingfang.wxpay;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Created by xht on 2016/5/5.
 * 微信支付的逻辑处理
 */
public class WXPay {

    //用户id
    private String uid;
    //资源id
    private String resource_id;
    //vip月数
    private String vip_mons;
    //金额
    private String price;
    // 请求自己服务器返回的数据 UserOrder：用户订单
    private UserOrder userOrder;

    //支付请求类
    private PayReq req;
    //IWXAPI对象
    private IWXAPI msgApi;

    // 上下文
    private Context context;
//—————————————————————————————————————————————————————————————头（5）
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                genPayReq(userOrder);//跳（6）
            }
        }
    };
//—————————————————————————————————————————————————————————————尾（5）
//——————————————————————————————————————————————————————————————头（1）
    public WXPay(Context context, String uid, String resource_id, String vip_mons, String price) {
        this.context = context;
        //用户id
        this.uid = uid;
        this.resource_id = resource_id;
        this.vip_mons = vip_mons;
        this.price = price;
        //Pay Req：支付请求
        //创建PayReq对象
        req = new PayReq();
        //创建IWXAPI对象
        msgApi = WXAPIFactory.createWXAPI(context, null);
        //使用app_id注册app
        msgApi.registerApp(WXPayConfig.APP_ID);
        //从服务器得到用户订单
        getUserOrderFromServer();
    }
       //—————————————————————————————————————————————————————————————尾（1）
    /**
     * 向自己服务器请求
     * 获得UserOrder对象
     * 该对象中封装了prepayId等信息
     */
    //—————————————————————————————————————————————————————————————头（2）
    private void getUserOrderFromServer() {
        final Map<String, String> params = new HashMap<String, String>();
        params.put("uid", uid);
        params.put("resource_id", resource_id);
        params.put("vip_mons", vip_mons);
        params.put("charge_type", "wx");
        params.put("fee", price);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    /**
                     * 服务器生成订单   这里发的是post请求
                     */
                    String reusult = httpByPost("http://api.lovek12.com", params, "utf-8");//跳（3）
                    Log.i("TAG", "微信支付请求服务器返回的数据------result==" + reusult);
                    Gson gson = new Gson();
                    userOrder = gson.fromJson(reusult, UserOrder.class);
                    Message msg = new Message();
                    msg.what = 1;
                    handler.sendMessage(msg);//跳（5）

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }
//—————————————————————————————————————————————————————————————尾（2）
//—————————————————————————————————————————————————————————————头（3）
    public static String httpByPost(String dataurl, Map<String, String> params,
                                    String encoding) throws IOException {
        URL url = new URL(dataurl);
        HttpURLConnection openConnection = (HttpURLConnection) url
                .openConnection();
        openConnection.setConnectTimeout(5000);
        openConnection.setReadTimeout(5000);
        //设置请求属性
        openConnection.setRequestProperty("charset", encoding);
        openConnection.setRequestMethod("POST");
        if (params != null) {
            openConnection.setDoOutput(true);
            StringBuffer stringbuffer = new StringBuffer();
            // 遍历Map集合拼接实体参数
            Set<Map.Entry<String, String>> entrySet = params.entrySet();

            for (Map.Entry<String, String> entry : entrySet) {
                stringbuffer.append(entry.getKey()).append("=")
                        .append(entry.getValue()).append("&");
            }
            stringbuffer.deleteCharAt(stringbuffer.length() - 1);
            OutputStream outputStream = openConnection.getOutputStream();
            outputStream.write(stringbuffer.toString().getBytes());
        }
        //Response：响应
        //判断请求是否成功
        if (openConnection.getResponseCode() == 200) {
            InputStream inputStream = openConnection.getInputStream();
            return read(inputStream, encoding);
        }
        return null;
    }
//—————————————————————————————————————————————————————————————尾（3）
//—————————————————————————————————————————————————————————————头（4）
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
//—————————————————————————————————————————————————————————————尾（4）

    /**
     * 获取签名
     *
     * @param order
     */
//—————————————————————————————————————————————————————————————头（6）
    private void genPayReq(UserOrder order) {
        //APPID
        req.appId = WXPayConfig.APP_ID;
        //合作伙伴ID，即商户号
        req.partnerId = WXPayConfig.MCH_ID;
        //预付ID，需要支付的金额
        req.prepayId = order.data.prepay_id;
        //包值
        req.packageValue = "Sign=WXPay";
        //随机字符串
        req.nonceStr = genNonceStr();//跳（7）
        //时间邮票
        req.timeStamp = String.valueOf(genTimeStamp());//跳（8）

        @SuppressWarnings("deprecation")
        List<NameValuePair> signParams = new LinkedList<NameValuePair>();
        signParams.add(new BasicNameValuePair("appid", req.appId));
        signParams.add(new BasicNameValuePair("noncestr", req.nonceStr));
        signParams.add(new BasicNameValuePair("package", req.packageValue));
        signParams.add(new BasicNameValuePair("partnerid", req.partnerId));
        signParams.add(new BasicNameValuePair("prepayid", req.prepayId));
        signParams.add(new BasicNameValuePair("timestamp", req.timeStamp));
        req.sign = genAppSign(signParams);//跳（9）
        Log.e("orion", signParams.toString());
        //发送请求
        msgApi.sendReq(req);
    }
//—————————————————————————————————————————————————————————————尾（6）
    /**
     * 在app端生成签名
     *
     * @param params
     * @return
     */
//—————————————————————————————————————————————————————————————头(9)
    private String genAppSign(List<NameValuePair> params) {
        StringBuilder str = new StringBuilder();

        for (int i = 0; i < params.size(); i++) {
            str.append(params.get(i).getName());
            str.append('=');
            str.append(params.get(i).getValue());
            str.append('&');
        }
        str.append("key=");
        str.append(WXPayConfig.API_KEY);

        String appSign = MD5.getMessageDigest(str.toString().getBytes()).toUpperCase();
        Log.e("orion", appSign);
        return appSign;
    }
//—————————————————————————————————————————————————————————————尾(9)
//—————————————————————————————————————————————————————————————头(7)
    private String genNonceStr() {
        Random random = new Random();
        return MD5.getMessageDigest(String.valueOf(random.nextInt(10000)).getBytes());
    }
//—————————————————————————————————————————————————————————————尾(7)
//—————————————————————————————————————————————————————————————头(8)
    // 生成当前时间
    private long genTimeStamp() {
        //返回以毫秒为单位的当前时间，返回的是当前时间与协调世界时 1970 年 1 月 1 日午夜之间的时间差
        return System.currentTimeMillis() / 1000;
    }
//—————————————————————————————————————————————————————————————尾(8)
}
