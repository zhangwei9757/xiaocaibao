package com.tumei.common.utils;

/**
 * Created by Administrator on 2017/1/12 0012.
 */
public class SignHelper
{
	// 字符编码格式 ，目前支持  utf-8
	public static String input_charset = "utf-8";

	/***
	 * rsa验证签名
	 *
	 *
	 * @param content
	 * @param sign
	 * @param pubKey
	 * @param mode 1 是md5Rsa算法   2 是 sha1rsa算法
	 * @return
	 */
	public static boolean verify(String content, String sign, String pubKey, int mode)
	{
		// 目前版本，只支持RSA
		return Rsa.verify(content, sign, pubKey, input_charset, mode);
	}

	public static String sign(String content, String privateKey, int mode)
	{
		return Rsa.sign(content, privateKey, input_charset, mode);
	}

	public static byte[] decrypt(byte[] content, String pubKey)
	{
		return Rsa.decrypt(content, pubKey);
	}


	public static String md5(String s){
		return Rsa.md5s(s);
	}
}
