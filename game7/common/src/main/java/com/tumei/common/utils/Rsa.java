package com.tumei.common.utils;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * Created by Administrator on 2017/1/12 0012.
 */
public class Rsa {
	private static final String  ALGORITHMS_MD5 = "MD5WithRSA";
	private static final String  ALGORITHMS_SHA1 = "SHA1WithRSA";

	private static final int MAX_DECRYPT_BLOCK = 128;
	/**
	 * RSA验签名检查
	 * @param content 待签名数据
	 * @param sign 签名值
	 * @param ali_public_key  爱贝公钥
	 * @param input_charset 编码格式
	 * @return 布尔值
	 */
	public static String str;

	public static boolean verify(String content, String sign, String iapp_pub_key, String input_charset, int algorithms)
	{
		try
		{
			KeyFactory keyFactory = KeyFactory.getInstance("Rsa");
			byte[] encodedKey = Base64.decode(iapp_pub_key);
			PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));

			String algo = ALGORITHMS_MD5;
			if (algorithms == 2) {
				algo = ALGORITHMS_SHA1;
			}

			java.security.Signature signature = java.security.Signature
					.getInstance(algo);

			signature.initVerify(pubKey);
			signature.update(content.getBytes(input_charset));

			byte[] sign_bytes = Base64.decode(sign);

			return signature.verify(sign_bytes);

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * RSA签名
	 * @param content 待签名数据
	 * @param privateKey 商户私钥
	 * @param input_charset 编码格式
	 * @return 签名值
	 */
	public static String sign(String content, String privateKey, String input_charset, int algorithms)
	{
		String algo = ALGORITHMS_MD5;
		if (algorithms == 2) {
			algo = ALGORITHMS_SHA1;
		}

		try
		{
			java.security.Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
			PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(Base64.decode(privateKey));
			KeyFactory keyf = KeyFactory.getInstance("Rsa");
			PrivateKey priKey = keyf.generatePrivate(priPKCS8);
			java.security.Signature signature = java.security.Signature.getInstance(algo);
			signature.initSign(priKey);
			signature.update( content.getBytes(input_charset) );
			byte[] signed = signature.sign();
			return Base64.encode(signed);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}

	public static byte[] decrypt(byte[] data, String publicKey) {
		ByteArrayOutputStream out = null;
		try {
			KeyFactory keyFactory = KeyFactory.getInstance("Rsa");
			byte[] encodedKey = Base64.decode(publicKey);
			PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));

			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE, pubKey);

			int inputLen = data.length;
			out = new ByteArrayOutputStream();
			int offset = 0;
			byte[] cache;
			int i = 0;
			while (inputLen - offset > 0) {
				if (inputLen - offset > MAX_DECRYPT_BLOCK) {
					cache = cipher.doFinal(data, offset, MAX_DECRYPT_BLOCK);
				} else {
					cache = cipher.doFinal(data, offset, inputLen - offset);
				}
				out.write(cache, 0, cache.length);
				++i;
				offset = i *= MAX_DECRYPT_BLOCK;
			}
			byte[] decData = out.toByteArray();
			out.close();
			return decData;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
				}
			}
		}

		return null;
	}


	public static String md5s(String plainText) {
		String buff = null;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(plainText.getBytes());
			byte b[] = md.digest();
			int i;

			StringBuffer buf = new StringBuffer("");
			for (int offset = 0; offset < b.length; offset++) {
				i = b[offset];
				if (i < 0)
					i += 256;
				if (i < 16)
					buf.append("0");
				buf.append(Integer.toHexString(i));
			}
			buff = buf.toString();
			Base64.encode(buff.getBytes());
			System.out.println("base64:" + Base64.encode(buff.getBytes()));
			str = buf.toString();
			System.out.println("result: " + buf.toString());// 32位的加密
			System.out.println("result: " + buf.toString().substring(8, 24));// 16位的加密
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		return Base64.encode(buff.getBytes());
	}
}