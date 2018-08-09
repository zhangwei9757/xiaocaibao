package com.tumei.groovy.commands.protos

import com.tumei.common.utils.MD5Util
import com.tumei.game.GameServer
import com.tumei.game.GameUser
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.WebSocketUser

/**
 * 根据客户端发送的字段内容求签名
 */
class RequestX7Sign extends BaseProtocol {
    public int seq
    // 支付的时候需要的私有透传参数，和quick支付的一样使用-分隔的那个
    public String extends_info_data
    // 登录返回的x7标识的玩家id
    public String game_guid
    // 当前玩家的等级，这里只是做一个签名，具体玩家等级，前后台有很小的可能不一致，以客户端为准
    public String game_level
    // 昵称
    public String game_role_name
    // 客户端自己根据时间和玩家id制作的一个订单
    public String game_orderid
    // 价格，单位元的价格,x7要求元，以前我们的价格是分
    public String game_price

    class Return extends BaseProtocol {
        public int seq
        // 根据以上参数，和服务器知道的一些参数制作的一个签名，需要在调用x7 sdk的支付api时，填写这个参数，api需要的其他参数客户端都
        // 有，部分参数要和这个协议传入的一致，有些服务器知道的比如角色id,角色名字没有要求传入
        public String sign
    }

    @Override
    void onProcess(WebSocketUser session) {
        GameUser user = (GameUser) session

        Return rci = new Return()
        rci.seq = seq

        StringBuilder sb = new StringBuilder()
        sb.append("extends_info_data=" + extends_info_data)
        sb.append("&game_area=" + GameServer.instance.zone)
        sb.append("&game_guid=" + game_guid)
        sb.append("&game_level=" + game_level)
        sb.append("&game_orderid=" + game_orderid)
        sb.append("&game_price=" + game_price)
        sb.append("&game_role_id=" + user.uid)
        sb.append("&game_role_name=" + game_role_name)
        sb.append("&notify_id=-1")
        sb.append("&subject=钻石")
        sb.append("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC5H1knaFD2JR0LJfRhik+E+eKBUlTcGIQPsacmCCppw0E/qoynVVWPauxCfNEss143GJRELixSDdrASC2WsbVKV+UmrMEHax6uF7avp35cexLNnNhQ9GVit8l+NMOnoBbsOTLlsNwUTjFf47i7IBM+tnrCbOsEGbXDrtPHUXtezQIDAQAB")

        String msg = sb.toString()
        rci.sign = MD5Util.encode(msg)

        user.send(rci)
    }
}

