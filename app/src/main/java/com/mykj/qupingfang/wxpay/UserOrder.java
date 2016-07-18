package com.mykj.qupingfang.wxpay;

/**
 * 请求自己服务器返回数据的实体类
 */
public class UserOrder {

    public String status;

    public Data data = new Data();

    public class Data {

        public String appid;

        public String mch_ip;

        public String nonce_str;
        //预付
        public String prepay_id;

        public String result_code;

        public String return_code;

        public String return_msg;
        //签名
        public String sign;

        public String timestamp;

        public String trade_type;

        public String order_id;

        @Override
        public String toString() {
            return "Data [appid=" + appid + ", mch_ip=" + mch_ip
                    + ", nonce_str=" + nonce_str + ", prepay_id=" + prepay_id
                    + ", result_code=" + result_code + ", return_code="
                    + return_code + ", return_msg=" + return_msg + ", sign="
                    + sign + ", timestamp=" + timestamp + ", trade_type="
                    + trade_type + "]";
        }
    }
    public String note;
    @Override
    public String toString() {
        return "UserOrder [status=" + status + ", data=" + data + ", note="
                + note + "]";
    }
}
