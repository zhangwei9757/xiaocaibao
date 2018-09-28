package com.tumei.groovy.commands.protos

import com.tumei.common.DaoService
import com.tumei.common.LocalService
import com.tumei.common.RemoteService
import com.tumei.common.utils.Defs
import com.tumei.common.utils.ErrCode
import com.tumei.common.utils.TimeUtil
import com.tumei.game.GameServer
import com.tumei.game.GameUser
import com.tumei.game.protos.notifys.NotifyMessage
import com.tumei.game.protos.structs.OfflineAwardStruct
import com.tumei.game.protos.structs.RoleStruct
import com.tumei.game.services.FriendService
import com.tumei.game.services.RobService
import com.tumei.model.*
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.SessionUser

/**
 * Created by leon on 2016/12/31.
 */
class RequestLogin extends BaseProtocol {
    public int seq

    @Override
    void onProcess(SessionUser session) {
        GameUser user = (GameUser) session

        long uid = user.getUid()
        long now = System.currentTimeMillis() / 1000

        ReturnLogin rl = new ReturnLogin()
        rl.seq = seq
        rl.timestamp = now
        rl.open = TimeUtil.pastDays(LocalService.getInstance().getOpenDate())
        rl.race = rl.open

        // 读取数据，填充
        RoleBean rb = user.getDao().findRole(user.getUid())
        user.setName(rb.getNickname())
        user.setVip(rb.getVip())
        user.setLevel(rb.getLevel())

        rb.setLogtime(new Date())

        boolean isRec = false;
        int today = TimeUtil.getToday()
        if (today != rb.getLogDay()) {
            rb.setLogDay(today)
            rb.setLogdays(rb.getLogdays() + 1)

            // 记录玩家的基础数据
            isRec = true;
            GameServer.getInstance().info("玩家(" + user.getUid() + ")(" + rb.getNickname() + ")vip:" + rb.getVip() + ",level:" + rb.getLevel());
        }

        long pt = rb.getPlaytime()
        if (pt != 0) {
            rl.result = ErrCode.角色封停禁止登录.name()
        }
        else {
            HerosBean hsb = user.getDao().findHeros(user.getUid())
            hsb.checkSkinsuits()
            rl.heros = hsb

            if (rl.heros.getLineups() == null) {
                rl.heros.setLineups(new int[6])
            }

            user.calcPower(hsb)

            if (isRec) {
                GameServer.getInstance().info(hsb.logInfos());
            }


            PackBean pb = user.getDao().findPack(uid)
            rl.pack = pb
            if (isRec) {
                GameServer.getInstance().info(pb.logInfos());
            }

            // 读取一下矿区的进度
            RoleMineBean rmb = user.getDao().findRoleMap(uid)
            rmb.flushEnergy(now)
            rl.mineEnergy = rmb.getEnergy()

//			user.info("************** 返回的等级:" + rl.role.level + " exp:" + rl.role.exp + " coin:" + rl.pack.getCoin())

            // 处理场景数据
            SceneBean sb = user.getDao().findScene(uid)
            if (sb.getScene() > 0) {
                sb.harvest(user, rl.offawards, 0)
            }

//			user.info("************** before,返回的等级:" + rb.getLevel() + " exp:" + rb.getExp() + " coin:" + rl.pack.getCoin())

            /**
             * 构建角色基本信息，与首次通知信息:
             *
             * 1. 充值状态
             * 2. 分享信息
             * 3. 签到及领取状态
             * 4. 各种卡信息
             *
             */
            rl.role.id = uid
            rl.role.name = rb.getNickname()
            rl.role.level = rb.getLevel()
            rl.role.exp = rb.getExp()
            rl.role.newbie = rb.getNewbie()
            rl.role.icon = rb.getIcon()
            rl.role.sex = rb.getSex()
            rl.role.vip = rb.getVip()
            rl.role.vipexp = rb.getVipexp()
            rl.role.create = rb.getCreatetime().getTime() / 1000
            rl.role.gm = user.isGm() ? 1 : 0

            if (TimeUtil.getDay(rb.getCreatetime()) == today) { // new guy
                user.SetOldUser(false)
            }
            else { // oldguy
                user.SetOldUser(true)
            }

            ChargeBean cb = user.getDao().findCharge(uid)
            cb.checkSendCards()
            if (cb.getTotal() > 0) {
                ActivityBean ab = user.getDao().findActivity(uid)
                if (ab.getFirstAward() != 0) {
                    rl.first = 2
                }
                else {
                    rl.first = 1
                }
            }

            GroupBean gb = user.getDao().findGroup(uid)
            long gid = gb.getGid()
            if (gid > 0) {
                String result = RemoteService.getInstance().askGroupLogon(user.createGroupRole(), gid)
                if (result != null && result.equals("fail")) { // 表示该玩家已经不在这个公会中了
                    gb.setGid(0)
                    gb.setName("")
                }
            }
            rl.gid = gb.getGid()

            FriendsBean fsb = user.getDao().findFriends(uid)
            fsb.notifyAllFriends(user.getPower(), rb)
            FriendService.getInstance().addRecommandFriend(uid, user.getPower(), rb)
        }

        user.send(rl)

        NotifyMessage.broadcast(user)

//		NotifyRedPoint.report(user)

//		user.info("************** after,返回的等级:" + rb.getLevel() + " exp:" + rb.getExp() + " coin:" + rl.pack.getCoin())

        user.setCanHarvest(true)
        user.flushCharge()

        RobService.getInstance().updateFrags(uid)
        // 更新center中的最近登录
        user.updateLastesLogonServer(rb)
//        user.submitArenaInfo()
        user.StaUserLog()

        // 如果是bt刷新个人累计充值状态
        if (Defs.ISBT) {
            ChargeBean cb = DaoService.getInstance().findCharge(uid);
            if (cb != null) {
                cb.flushChargeForMail();
            }
        }
    }
}

class ReturnLogin extends BaseProtocol {
    public int seq

    public String result
    /**
     * 角色基础信息
     */
    public RoleStruct role = new RoleStruct()
    /**
     * 英雄基础信息
     */
    public HerosBean heros
    /**
     * 背包基础信息
     */
    public PackBean pack

    /**
     * 离线奖励
     */
    public OfflineAwardStruct offawards = new OfflineAwardStruct()

    /**
     * 对时
     */
    public long timestamp

    // 服务器开服时间
    public int open

    // 开服竞赛的时间
    public int race;

    // 首充
    public int first

    // 矿区行动力
    public int mineEnergy

    public int gid

    // 今日已经使用钻石进行注灵的次数
    public int relicCount
}
