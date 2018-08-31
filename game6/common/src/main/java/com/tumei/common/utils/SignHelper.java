package com.tumei.common.utils;

/**
 * Created by Administrator on 2017/1/12 0012.
 */
public class SignHelper
{
	// 字符编码格式 ，目前支持  utf-8
	public static String input_charset = "utf-8";

	public static boolean verify(String content, String sign, String pubKey)
	{
		// 目前版本，只支持RSA
		return Rsa.verify(content, sign, pubKey, input_charset);
	}

	public static String sign(String content, String privateKey)
	{
		return Rsa.sign(content, privateKey, input_charset);
	}

	public static String md5(String s){
		return Rsa.md5s(s);
	}
}
