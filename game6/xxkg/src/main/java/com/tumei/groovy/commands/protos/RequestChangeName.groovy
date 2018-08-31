package com.tumei.groovy.commands.protos

import com.google.common.base.Strings
import com.tumei.GameConfig
import com.tumei.game.GameUser
import com.tumei.model.PackBean
import com.tumei.model.RoleBean
import com.tumei.model.SceneBean
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.SessionUser
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

import static com.tumei.common.utils.Defs.钻石

/**
 * Created by Administrator on 2017/3/13 0013.
 * 请求充值信息
 */
class RequestChangeName extends BaseProtocol {
    public int seq
    public String name


    @Override
    void onProcess(SessionUser session) {
        GameUser user = (GameUser)session

        ReturnChangeName rl = new ReturnChangeName()
        rl.seq = seq
        println("fuck you change name")

        name = name.trim()
        if (Strings.isNullOrEmpty(name)) {
            rl.result = "昵称不能为空白符"
            user.send(rl)
            return
        }
        if (name.length() < 1) {
            rl.result = "昵称不能长度不足"
            user.send(rl)
            return
        }
        if (name.toLowerCase().startsWith("tm_")) {
            rl.result = "系统使用的名称"
            user.send(rl)
            return
        }

        RoleBean rb = user.getDao().findRole(user.getUid())

        int gem = 0
        if (!rb.getNickname().startsWith("tm_")) {
            gem = GameConfig.getInstance().getUser_chagnename_gem()
        }

        if (gem > 0) {
            PackBean pb = user.getDao().findPack(user.getUid())
            if (!pb.contains(钻石, gem)) {
                rl.result = "钻石不足"
                user.send(rl)
                return
            }
        }

        if (!user.getDao().changeName(user.getUid(), name, rb.getNickname())) {
            rl.result = "重复的昵称"
        } else {
            rb.setNickname(name)
            user.setName(name)

            if (gem > 0) {
                user.payItem(钻石, gem, "改名")
            } else {
                SceneBean sb = user.getDao().findScene(user.getUid())
                sb.setScene(1)
            }
        }


        user.send(rl)
    }
}

class ReturnChangeName extends BaseProtocol {
    public int seq
    public String result = ""
}
