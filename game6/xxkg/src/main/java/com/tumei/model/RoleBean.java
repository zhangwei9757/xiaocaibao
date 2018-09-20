package com.tumei.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tumei.common.utils.Defs;
import com.tumei.modelconf.TeamExpConf;
import com.tumei.common.Readonly;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

/**
 * Created by leon on 2016/11/5.
 */
@Document(collection = "Role")
public class RoleBean {
    @JsonIgnore
    @Id
    private String objectId;

    @Field("id")
    private Long id;
    /**
     * 昵称
     */
    private String nickname;
    /**
     * 领主品质，由英雄列表中领主英雄的等级决定
     */
    private int grade = 2;
    /**
     * 头像
     */
    private int icon = 0;
    /**
     * 领主等级
     */
    private int level = 1;
    /**
     * 领主经验
     */
    private int exp;
    /**
     * gm等级
     */
    private int gmlevel;
    /**
     * 新手步骤
     */
	private int newbie;
    /**
     * 总在线
     */
    private int totaltime;
    /**
     * 今日在线
     */
    private int todaytime;
    /**
     * 最近登录时间
     */
    private Date logtime;
    /**
     * 最近登出时间
     */
    private Date logouttime;
    /**
     * 0：不在线
     * 1: 在线
     */
    private int online;
    /**
     * 上次登录天,判定是否跨天，跨天后logdays要增加
     */
    private int logDay;
    /**
     * 登录总天数
     */
	private int logdays;
    /**
     * vip等级
     */
	private int vip;
    /**
     * vip经验
     */
    private int vipexp;
    /**
     * 角色创建时间
     */
    private Date createtime;
    /**
     * 禁言结束时间
     */
    private long saytime;

    /**
     * 禁止登录结束时间
     */
    private long playtime;
    /**
     * 设备标识符(最近登录使用的)
     */
	private String idfa;

    /**
     * 性别
     */
    private int sex;

    public static RoleBean createNewRole(Long _id) {
		RoleBean rb = new RoleBean();
        rb.id = _id;
        rb.grade = 2;
        rb.level = 1;
        rb.newbie = 1;
        rb.setVip(Defs.初始化VIP);
        rb.setVipexp(Defs.初始化VIPEXP);
        rb.nickname = "tm_" + _id;
        rb.createtime = new Date();

        return rb;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public int getGmlevel() {
        return gmlevel;
    }

    public void setGmlevel(int gmlevel) {
        this.gmlevel = gmlevel;
    }

    public int getNewbie() {
        return newbie;
    }

    public void setNewbie(int newbie) {
        this.newbie = newbie;
    }

    public int getTotaltime() {
        return totaltime;
    }

    public void setTotaltime(int totaltime) {
        this.totaltime = totaltime;
    }

    public int getTodaytime() {
        return todaytime;
    }

    public void setTodaytime(int todaytime) {
        this.todaytime = todaytime;
    }

    public Date getLogtime() {
        return logtime;
    }

    public long getLogtimeLong() {
        if (logtime != null) {
            return logtime.getTime() / 1000;
        }
        return 0;
    }

    public void setLogtime(Date logtime) {
        this.logtime = logtime;
        this.online = 1;
    }

    public Date getLogouttime() {
        return logouttime;
    }

    public void setLogouttime(Date logouttime) {
        this.logouttime = logouttime;
        this.online = 0;
    }

    public int getVip() {
        return vip;
    }

    public void setVip(int vip) {
        this.vip = vip;
    }

    public int getVipexp() {
        return vipexp;
    }

    public void setVipexp(int vipexp) {
        this.vipexp = vipexp;
    }

    public int getLogdays() {
        return logdays;
    }

    public void setLogdays(int logdays) {
        this.logdays = logdays;
    }

    public Date getCreatetime() {
        return createtime;
    }

    public void setCreatetime(Date createtime) {
        this.createtime = createtime;
    }

    public String getIdfa() {
        return idfa;
    }

    public void setIdfa(String idfa) {
        this.idfa = idfa;
    }

    public long getSaytime() {
        return saytime;
    }

    public void setSaytime(long saytime) {
        this.saytime = saytime;
    }

    public long getPlaytime() {
        return playtime;
    }

    public void setPlaytime(long playtime) {
        this.playtime = playtime;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

//    @JsonIgnore
//    private Log logger = LogFactory.getLog(RoleBean.class);
    /**
     * 增加领主经验
     * @param _exp
     * @return 是否领主本次升级
     */
    public boolean addExp(int _exp) {
    	boolean rtn = false;
		exp += _exp;
        TeamExpConf tec = Readonly.getInstance().findTeamExp(level);

		while (tec != null && exp >= tec.cost && tec.cost > 0) {
			exp -= tec.cost;
			++level;
			tec = Readonly.getInstance().findTeamExp(level);
			rtn = true;
		}
//        logger.warn("增加后，领主等级(" + level + ") 经验(" + exp + ")");
        return rtn;
    }

    public int getLogDay() {
        return logDay;
    }

    public void setLogDay(int logDay) {
        this.logDay = logDay;
    }

    public int getOnline() {
        return online;
    }

    public void setOnline(int online) {
        this.online = online;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }
}
