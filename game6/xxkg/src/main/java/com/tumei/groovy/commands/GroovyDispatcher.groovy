package com.tumei.groovy.commands

import com.tumei.common.utils.JsonUtil
import com.tumei.game.GameServer
import com.tumei.groovy.contract.IProtocolDispatcher
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.ErrorInfo
import com.tumei.websocket.SessionUser
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct

@Component
@Scope(value = "singleton")
class GroovyDispatcher implements IProtocolDispatcher {

    @Autowired
    private ApplicationContext ctx

    @Autowired
    private GameServer server;

    @PostConstruct
    void init() {
        println("GroovyDispatcher init ok.")
    }

    public Class<? extends BaseProtocol> getProtoClass(String name) {
        byte[] items = name.getBytes()

        // springboot 默认注册的实体协议第一个字母是小写, 烦，转成groovy的可以随意定义。
        int i = (int) items[0]
        if (i < 97) {
            i = i - 65 + 97
            items[0] = (byte) i
            name = new String(items)
        }

        try {
            return (Class<? extends BaseProtocol>) ctx.getType(name)
        } catch (NoSuchBeanDefinitionException ex) {
            println("--- 没有找到协议:" + name + " 失败:" + ex.getMessage())
        }
        return null
    }

    @Override
    void process(String cmd, String data, SessionUser session) {
        Class<? extends BaseProtocol> bp = getProtoClass(cmd)
        if (bp == null) {
            session.error("协议名(" + cmd + ") 对应的处理组件不存在.")

            // 尽量提取发送来的数据中的seq字段，返回给客户端
            ErrorInfo errorInfo = new ErrorInfo()
            errorInfo.seq = JsonUtil.forceGetSeq(data)
            errorInfo.result = "命令(" + cmd + ")没有对应的处理逻辑"
//            session.send(errorInfo)
            return
        }

//        session.debug("[" + cmd + "] Thread(" + Thread.currentThread().id + ") === data:" + data)
        BaseProtocol protocol = JsonUtil.Unmarshal(data, bp)
        if (protocol != null) {
            switch (cmd) {
                case "RequestMineEnter":
//                    IMineSystem mineSystem = ctx.getBean(IMineSystem.class)
                    server.getMineSystem().enter(session, protocol)
                    break
                case "RequestMineLeave":
//                    IMineSystem mineSystem = ctx.getBean(IMineSystem.class)
                    server.getMineSystem().leave(session, protocol)
                    break
                case "RequestMineMove":
//                    IMineSystem mineSystem = ctx.getBean(IMineSystem.class)
                    server.getMineSystem().move(session, protocol)
                    break
                case "RequestMineLook":
//                    IMineSystem mineSystem = ctx.getBean(IMineSystem.class)
                    server.getMineSystem().look(session, protocol)
                    break
                case "RequestMineAction":
//                    IMineSystem mineSystem = ctx.getBean(IMineSystem.class)
                    server.getMineSystem().action(session, protocol)
                    break
                case "RequestMineAccelerate":
//                    IMineSystem mineSystem = ctx.getBean(IMineSystem.class)
                    server.getMineSystem().accelerate(session, protocol)
                    break
                case "RequestMineEnhance":
//                    IMineSystem mineSystem = ctx.getBean(IMineSystem.class)
                    server.getMineSystem().enhance(session, protocol)
                    break
                case "RequestMineBuyEnergy":
//                    IMineSystem mineSystem = ctx.getBean(IMineSystem.class)
                    server.getMineSystem().buyEnergy(session, protocol)
                    break
                case "RequestMineHarvest":
//                    IMineSystem mineSystem = ctx.getBean(IMineSystem.class)
                    server.getMineSystem().harvest(session, protocol)
                    break
                default:
                    protocol.process(session)
                    break
            }
        }
    }
}
