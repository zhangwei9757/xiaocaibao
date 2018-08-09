package com.tumei.configs.security;

import com.tumei.centermodel.AccountBean;
import com.tumei.centermodel.AccountBeanRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

/**
 * Created by Administrator on 2017/1/20 0020.
 */
public class MongoUserDetailService implements UserDetailsService {
	private Log log = LogFactory.getLog(MongoUserDetailService.class);

	@Autowired
	private AccountBeanRepository accountBeanRepository;

	@Override
	public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
		AccountBean user = accountBeanRepository.findByAccount(s);

		if (user == null) {
			throw new UsernameNotFoundException("数据库中没有该帐号");
		}

		List<GrantedAuthority> auth = AuthorityUtils.commaSeparatedStringToAuthorityList(user.getRole());
		return new User(s, user.getPasswd(), auth);
	}
}
