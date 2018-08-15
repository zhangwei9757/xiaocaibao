package com.tumei.groovy.commands.protos

import com.tumei.common.RemoteService
import com.tumei.common.utils.ErrCode
import com.tumei.common.utils.TimeUtil
import com.tumei.game.GameServer
import com.tumei.game.GameUser
import com.tumei.game.protos.notifys.NotifyMessage
import com.tumei.game.protos.structs.OfflineAwardStruct
import com.tumei.game.protos.structs.RoleStruct
import com.tumei.game.services.FriendService
import com.tumei.game.services.LocalService
import com.tumei.game.services.RobService
import com.tumei.model.*
import com.tumei.model.beans.EquipBean
import com.tumei.model.beans.HeroBean
import com.tumei.websocket.BaseProtocol
import com.tumei.websocket.WebSocketUser
/**
 * Created by leon on 2016/12/31.
 */
class RequestLogin extends BaseProtocol {
    public int seq

    RequestLogin() {}

    class Return extends BaseProtocol {
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
        public int race

        // 首充
        public int first

        // 矿区行动力
        public int mineEnergy

        public long gid

        // 今日已经使用钻石进行注灵的次数
        public int relicCount

        // 限时奖励是否已领取 0：未领取 1：已领取
        public int receive
    }


    @Override
    void onProcess(WebSocketUser session) {
        GameUser user = (GameUser) session

        long uid = user.getUid()
        long now = System.currentTimeMillis() / 1000

        Return rl = new Return()
        rl.seq = seq
        rl.timestamp = now
        rl.open = TimeUtil.pastDays(LocalService.getInstance().getOpenDate())
        rl.race = rl.open

        // 读取数据，填充
        RoleBean rb = user.getDao().findRole(user.getUid())
        user.setName(rb.getNickname())
        user.setVip(rb.getVip(), rb.getVipexp())
        user.setLevel(rb.getLevel())

        rb.setLogtime(new Date())

        boolean isRec = false
        int today = TimeUtil.getToday()
        if (today != rb.getLogDay()) {
            rb.setLogDay(today)
            rb.setLogdays(rb.getLogdays() + 1)

            // 记录玩家的基础数据
            isRec = true
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
                GameServer.getInstance().info(hsb.logInfos())
            }

            PackBean pb = user.getDao().findPack(uid)
            rl.pack = pb
            if (isRec) {
                GameServer.getInstance().info(pb.logInfos())
            }

            /***
             * 增加一个检测，英雄hid，装备eid，当前最大值如果比已经有的装备小，则更新这个最大值
             */

            /**
             * 记录一下英雄和对应的装备是否有重复的，如果有就+1
             */
//            HashSet<Integer> hids = new HashSet<>()
//            HashSet<Integer> eids = new HashSet<>()
//
//            int max_eid = 0
//            for (int key : pb.equips.keySet()) {
//                if (key > max_eid) {
//                    max_eid = key
//                }
//                eids.add(key)
//            }
//
//            int max_hid = 0
//            for (int key : pb.heros.keySet()) {
//                if (key > max_hid) {
//                    max_hid = key
//                }
//                hids.add(key)
//            }
//
//            for (HeroBean hb : hsb.heros) {
//                if (hb != null) {
//                    if (hb.hid > max_hid) {
//                        max_hid = hb.hid
//                    }
//
//                    for (EquipBean eb : hb.equips) {
//                        if (eb != null) {
//                            if (eb.eid > max_eid) {
//                                max_eid = eb.eid
//                            }
//                        }
//                    }
//                }
//            }
//            if (max_hid > pb.maxhid) {
//                pb.maxhid = max_hid
//            }
//            if (max_eid > pb.maxeid) {
//                pb.maxeid = max_eid
//            }
//
//            // 得到最大值后，再来看hids, eids,会不会在英雄装备的时候发生重复， 此时修改对应的值即可
//            for (HeroBean hb : hsb.heros) {
//                if (hb != null) {
//                    if (hids.contains(hb.hid)) {
//                        hb.hid = ++pb.maxhid
//                    }
//
//                    hids.add(hb.hid)
//
//                    for (EquipBean eb : hb.equips) {
//                        if (eb != null) {
//                            if (eids.contains(eb.eid)) {
//                                eb.eid = ++pb.maxeid
//                            }
//                            eids.add(eb.eid)
//                        }
//                    }
//                }
//            }

            /** -----------  检测修正结束 ----------- by leon **/


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
            rl.role.create = (long) (rb.getCreatetime().getTime() / 1000)
            rl.role.gm = user.getGmlevel()

            if (TimeUtil.getDay(rb.getCreatetime()) == today) { // new guy
                user.SetOldUser(false)
            }
            else { // oldguy
                user.SetOldUser(true)
            }

            ChargeBean cb = user.getDao().findCharge(uid)
            cb.checkSendCards()
            ActivityBean ab = user.getDao().findActivity(uid)
            if (cb.getTotal() > 0) {
                if (ab.getFirstAward() != 0) {
                    rl.first = 2
                }
                else {
                    rl.first = 1
                }
            }
            ab.flush()
            rl.relicCount = ab.relicActivate

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
    }
}

