package com.tumei.common.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Strings;
import org.apache.commons.codec.digest.DigestUtils;

public class IOSSignUtil {

	static final String encode="UTF-8";
	
	public static String sign(String signStr){
		return shuffleSign(DigestUtils.md5Hex(signStr));
	}
	
    public static String createSignString(Map<String, String> params) {
        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);
        
        String key = null;
        String value = null;        
        StringBuilder sign = new StringBuilder();
        for (int i = 0; i < keys.size(); i++) {
            key = keys.get(i);
            value = params.get(key);
            if(Strings.isNullOrEmpty(value)){
            	continue;
            }
            sign.append(key).append("=").append(value).append("&");            
        }
        if(sign.length()>0){
        	sign.delete(sign.length()-1, sign.length());
        }
        
        return sign.toString();
    }
    
    public static String createQueryString(Map<String, String> params) {
        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);
        
        String key = null;
        String value = null;        
        StringBuilder query = new StringBuilder();
        for (int i = 0; i < keys.size(); i++) {
            key = keys.get(i);
            value = params.get(key);
            query.append(key).append("=").append(URLEncode(value)).append("&");            
        }
        if(query.length()>0){
        	query.delete(query.length()-1, query.length());
        }
        
        return query.toString();
    }
    
    public static Map<String, String> queryStringToMap(String queryString, boolean toDecode){
    	String name_val[]=queryString.split("&");
    	Map<String, String> params=new HashMap<String, String>();
        String keyval[]=null;
    	for(int i=0; i<name_val.length; i++){
    		keyval=name_val[i].split("=");
    		if(keyval.length<2){
    			continue;
    		}
    		if(toDecode){
    			keyval[1]=URLDecode(keyval[1]);
    		}
    		params.put(keyval[0], keyval[1]);
    	}
    	return params;
    }
    
    private static String URLEncode(String str){
    	try {
			return URLEncoder.encode(str, encode);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(str);
		}
    }
    
    public static String URLDecode(String str){
    	try {
			return URLDecoder.decode(str, encode);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(str);
		}
    }    
    
    private static byte[][] shufflePos=new byte[][]{{1,13},{5,17},{7,23}};
	private static String shuffleSign(String src){
		if(src == null || src.length() == 0){
			return src;
		}
		try {
			byte[] bytes=src.getBytes("utf-8");
			byte temp;
			for(int i=0; i<shufflePos.length; i++){
				temp=bytes[shufflePos[i][0]];
				bytes[shufflePos[i][0]]=bytes[shufflePos[i][1]];
				bytes[shufflePos[i][1]]=temp;
			}
			return new String(bytes);
		} catch (UnsupportedEncodingException e) {
			return src;
		}
	}
	
}
