package com.tumei.configs.security;

import com.google.common.base.Strings;
import com.tumei.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

/**
 * Created by Leon on 2017/11/20 0020.
 *
 * Jwt 认证过滤器
 *
 */
public class JwtAuthenticationFilter extends GenericFilterBean {
	private Log log = LogFactory.getLog(JwtAuthenticationFilter.class);

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest)servletRequest;
		String token = request.getHeader("Authorization");
		if (!Strings.isNullOrEmpty(token)) {
//			log.info("连接地址:" + request.getRemoteAddr() + " 附带token:" + token);
			try {
				Claims claims = JwtUtil.verify(token);
				if (claims != null) {
					long uid = Long.parseLong(claims.getId());
					request.setAttribute("uid", uid);
					String user = claims.getSubject();
					List<GrantedAuthority> authorities = AuthorityUtils.commaSeparatedStringToAuthorityList((String)claims.get("authorities"));
					if (user != null) {
						Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, authorities);
						SecurityContextHolder.getContext().setAuthentication(authentication);
					}
				}
			} catch (Exception ex) {
				log.error("JwtToken error:" + ex.getMessage());
			}
		} else {
//			log.info("连接地址:" + request.getRemoteAddr() + " 没有附带token");
		}

		filterChain.doFilter(servletRequest, servletResponse);
	}
}
