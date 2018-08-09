package com.tumei.controller;

import com.tumei.common.DaoGame;
import com.tumei.common.RemoteService;
import com.tumei.controller.group.GroupTextNotifyStruct;
import com.tumei.game.GameServer;
import com.tumei.game.protos.group.GroupTextNotify;
import com.tumei.model.GroupBean;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * Created by Administrator on 2017/1/18 0018.
 *
 * 从公会服务器发送过来的各种通知
 *
 */
@RestController
@RequestMapping(value = "/group")
public class GroupController {
	private static final Log log = LogFactory.getLog(GroupController.class);

	@Autowired
	private DaoGame dao;

	@Autowired
	private GameServer server;

	@Autowired
	private RemoteService rs;

	@ApiOperation(value = "文本通知")
	@RequestMapping(value = "/notifyMessage", method = RequestMethod.POST)
	@ApiImplicitParams({
			@ApiImplicitParam(name = "notify", value = "通知内容", required = true, dataType = "GroupTextNotifyStruct", paramType = "parameter"),
	})
	public boolean notifyMessage(@RequestBody GroupTextNotifyStruct notify) {
		GroupTextNotify proto = new GroupTextNotify();
		proto.text = notify.text.split("\n");

//		for (String msg : proto.text) {
//			log.debug("------- 收到公会文本下发通知:" + msg);
//		}

		server.broadcast(proto, notify.users);
		return true;
	}


	@ApiOperation(value = "审批通过通知")
	@RequestMapping(value = "/notifyApproval", method = RequestMethod.GET)
	@ApiImplicitParams({
	@ApiImplicitParam(name = "role", value = "被通知玩家", required = true, dataType = "long", paramType = "parameter"),
	@ApiImplicitParam(name = "gid", value = "公会", required = true, dataType = "long", paramType = "parameter"),
	})
	public boolean notifyApproval(long role, long gid) {
		GroupBean gb = dao.findGroup(role);
		gb.setGid(gid);
		gb.setLeaveDay(new Date(System.currentTimeMillis() + 3600 * 1000 * 4));
		return true;
	}
}
