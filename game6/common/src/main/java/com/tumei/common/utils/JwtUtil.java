package com.tumei.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

/**
 * Created by Leon on 2017/11/20 0020.
 */
public class JwtUtil {
	static final long EXPIRATIONTIME = 432_000_000; // 5天
	static final String SECRET = "XcbGame!sVeryG0od";
	static final String TOKEN_PREFIX = "Bearer";

	/**
	 * 生成jwt token.
	 * @return
	 */
	public static String generate(long uid, String account, String privs) {
		return Jwts.builder()
			.claim("authorities", privs)
			.setSubject(account)
			.setId("" + uid)
			.setExpiration(new Date(System.currentTimeMillis() + EXPIRATIONTIME))
			.signWith(SignatureAlgorithm.HS512, SECRET)
			.compact();
	}

	public static String generate30Days(long uid, String account, String privs) {
		return Jwts.builder()
				   .claim("authorities", privs)
				   .setSubject(account)
				   .setId("" + uid)
				   .setExpiration(new Date(System.currentTimeMillis() + 20_432_000_000L))
				   .signWith(SignatureAlgorithm.HS512, SECRET)
				   .compact();
	}

	/**
	 * 验证jwt token, 并获取用户id
	 * @param token
	 * @return
	 */
	public static Claims verify(String token) {
		return Jwts.parser()
			.setSigningKey(SECRET)
			.parseClaimsJws(token.replace(TOKEN_PREFIX, ""))
			.getBody();
	}
}
