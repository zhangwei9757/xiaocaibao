package com.tumei.common.utils;

import java.security.MessageDigest;

/**
 * Created by Administrator on 2016/12/28 0028.
 */
public class MD5Util {

    public static String encode(String str) {
        try {
            MessageDigest digester = MessageDigest.getInstance("MD5");
            digester.update(str.getBytes("UTF-8"));
            byte[] hash = digester.digest();
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < hash.length; i++) {
                hexString.append(Integer.toString((hash[i] & 0xff) + 0x100, 16).substring(1));
            }
            return hexString.toString();
        } catch (Exception e) {
        }
        return "";
    }
}
