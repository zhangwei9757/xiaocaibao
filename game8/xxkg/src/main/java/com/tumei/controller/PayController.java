package com.tumei.controller;

import com.tumei.common.DaoGame;
import com.tumei.game.GameServer;
import com.tumei.game.GameUser;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Administrator on 2017/1/18 0018.
 */
@RestController
public class PayController {
	static final Log log = LogFactory.getLog(PayController.class);

	@Autowired
	private DaoGame dao;

	@Autowired
	private GameServer server;

	@ApiOperation(value = "支付通知刷新接口")
	@RequestMapping(value = "/notifyPay", method = RequestMethod.GET)
	@ApiImplicitParams({@ApiImplicitParam(name = "id", value = "玩家id", required = true, dataType = "long", paramType = "query")})
	public String notifyPay(long id) {
		log.warn("+++ 收到玩家(" + id + ")充值提示，检查增加充值.");
		GameUser user = GameServer.getInstance().find(id);
		if (user != null) {
			user.flushCharge();
		}
		return "ok";
	}
}
