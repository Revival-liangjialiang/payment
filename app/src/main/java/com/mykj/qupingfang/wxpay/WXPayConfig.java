package com.mykj.qupingfang.wxpay;

/**
 * 微信常量类
 */
public class WXPayConfig {
	
	// 请同时修改 androidmanifest.xml里面，.PayActivityd里的属性<data android:scheme="wxb4ba3c02aa476ea1"/>为新设置的appid
	//第一步在微信开放平台提交应用审核，审核过后将获得APPID，第二步申请移动应用支付，审核通过后获得商户号，使用商户号登录商户平台设置API密匙
	public static final String APP_ID = "";
	// 商户号
	public static final String MCH_ID = "";
	// API密钥，在商户平台设置
	public static final String API_KEY = "";
}
