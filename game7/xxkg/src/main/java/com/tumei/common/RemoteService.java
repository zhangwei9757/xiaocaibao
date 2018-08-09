package com.tumei.common;

import com.google.common.base.Strings;
import com.tumei.common.group.*;
import com.tumei.common.service.BaseRemoteService;
import com.tumei.common.service.ServiceRouter;
import com.tumei.common.utils.JwtUtil;
import com.tumei.common.webio.AwardStruct;
import com.tumei.common.webio.BattleResultStruct;
import com.tumei.common.webio.RankStruct;
import com.tumei.dto.MessageDto;
import com.tumei.dto.arena.*;
import com.tumei.dto.battle.HerosStruct;
import com.tumei.dto.boss.BossDto;
import com.tumei.dto.boss.BossGuildDto;
import com.tumei.dto.boss.BossRoleDto;
import com.tumei.dto.guildbag.GuildBagDtos;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/12/28 0028.
 *
 * 远程服务请求的函数集合
 *
 */
@Service
public class RemoteService extends BaseRemoteService {
	private static final Log log = LogFactory.getLog(RemoteService.class);

	private static RemoteService _instance = null;

	public static RemoteService getInstance() {
		return _instance;
	}
	
	private static final String prefix = "http://Arena";

	private static final String guild_prefix = "http://Guild";

	private static final String center_prefix = "http://Center";

	private HttpHeaders headers;

	@Autowired
	private ServiceRouter serviceRouter;

	@PostConstruct
	void init() {
		String token = JwtUtil.generate365Days(999L, "gm", "USER,ADMIN");
		log.warn("jwtToken:[" + token + "].");
		headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + token);
		_instance = this;
	}

	/***
	 * 发送到注册服务器 更新最近登录的玩家信息
	 * @param id
	 * @param server
	 * @param level
	 * @param icon
	 * @param vip
	 * @param name
	 * @return
	 */
	public String updateLatestServer(long id, int server, int level, int icon, int vip, String name) {
		String url = "";
		try {
			url = center_prefix + "/service/updateLatestServer?id={id}&server={server}&level={level}&icon={icon}&vip={vip}&name={name}";
				return restTemplate.getForObject(url, String.class, id, server, level, icon, vip, name);
		} catch (Exception ex) {
			log.error("更新最近登录的服务器错误:" + ex.getMessage() + " url:" + url);
		}
		return "";
	}

	public GroupMessage askGroupInfo(long gid, long role) throws RemoteException {
		try {
			String url = guild_prefix + "/infoGuild?gid={gid}&role={role}";
			return restTemplate.getForObject(url, GroupMessage.class, gid, role);
		} catch (Exception e) {

		}
		throw new RemoteException();
	}

	public List<String> askGroupMessage(long gid) {
		try {
			String url = guild_prefix + "/messages?gid={gid}";
			return restTemplate.getForObject(url, ArrayList.class, gid);
		} catch (Exception e) {
		}
		return null;
	}

	public String askGroupApprove(long gid, long role, long target, int mode) {
		try {
			String url = guild_prefix + "/approve?gid={gid}&role={role}&target={target}&mode={mode}";
			return restTemplate.getForObject(url, String.class, gid, role, target, mode);
		} catch (Exception ex) {

		}
		return "公会服务维护中";
	}

	public List<GroupSimpleStruct> askGroupRecommands(int zone) throws RemoteException {
		try {
			String url = guild_prefix + "/recommand?zone={zone}";
			return restTemplate.getForObject(url, ArrayList.class, zone);
		} catch (Exception ex) {

		}
		throw new RemoteException();
	}

	public GroupReturn askGroupCreate(GroupRoleMessage role, String name, int icon, int approval) {
		try {
			String url = guild_prefix + "/create?name={name}&icon={icon}&approval={approval}";
			return restTemplate.postForObject(url, role, GroupReturn.class, name, icon, approval);
		} catch (Exception ex) {

		}

		GroupReturn rtn = new GroupReturn();
		rtn.result = "公会服务维护中";
		return rtn;
	}

	public String askGroupLogon(GroupRoleMessage role, long gid) {
		try {
			String url = guild_prefix + "/logon?gid={gid}";
			return restTemplate.postForObject(url, role, String.class, gid);
		} catch (Exception ex) {

		}

		return "公会服务维护中";
	}


	public String askGroupJoin(GroupRoleMessage role, long gid) {
		try {
			String url = guild_prefix + "/join?gid={gid}";
			return restTemplate.postForObject(url, role, String.class, gid);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return "公会服务维护中";
	}

	public String askGroupLeave(long gid, long role) {
		try {
			String url = guild_prefix + "/leave?role={role}&gid={gid}";
			return restTemplate.getForObject(url, String.class, role, gid);
		} catch (Exception ex) {

		}
		return "公会服务维护中";
	}

	public String askGroupApproveMode(long gid, long role, int mode) {
		try {
			String url = guild_prefix + "/modifyApproval?role={role}&gid={gid}&mode={mode}";
			return restTemplate.getForObject(url, String.class, role, gid, mode);
		} catch (Exception ex) {

		}
		return "公会服务维护中";
	}

	public String askGroupDesc(long gid, long role, String desc) {
		try {
			String url = guild_prefix + "/modifyDesc?role={role}&gid={gid}&desc={desc}";
			return restTemplate.getForObject(url, String.class, role, gid, desc);
		} catch (Exception ex) {
			log.error("askGroupDesc error:" + ex.getMessage());
		}
		return "公会服务维护中";
	}

	public String askGroupNotify(long gid, long role, String desc) {
		try {
			String url = guild_prefix + "/modifyNotify?role={role}&gid={gid}&desc={desc}";
			return restTemplate.getForObject(url, String.class, role, gid, desc);
		} catch (Exception ex) {
			log.error("askGroupNotify error:" + ex.getMessage());
		}
		return "公会服务维护中";
	}

	public String askGroupImpeach(long gid, long role) {
		try {
			String url = guild_prefix + "/impeach?role={role}&gid={gid}";
			return restTemplate.getForObject(url, String.class, role, gid);
		} catch (Exception ex) {
			log.error("askGroupImpeach error:" + ex.getMessage());
		}
		return "公会服务维护中";
	}

	public String askGroupKick(long gid, long role, long target) {
		try {
			String url = guild_prefix + "/kick?role={role}&gid={gid}&target={target}";
			return restTemplate.getForObject(url, String.class, role, gid, target);
		} catch (Exception ex) {
			log.error("askGroupKick error:" + ex.getMessage());
		}
		return "公会服务维护中";
	}

	public String askGroupModify(long gid, long role, long target, int mode) {
		try {
			String url = guild_prefix + "/modify?role={role}&gid={gid}&target={target}&mode={mode}";
			return restTemplate.getForObject(url, String.class, role, gid, target, mode);
		} catch (Exception ex) {
			log.error("askGroupModify error:" + ex.getMessage());
		}
		return "公会服务维护中";
	}

	public GroupSimpleStruct askGroupFindId(long gid) {
		try {
			String url = guild_prefix + "/findById?gid={gid}";
			return restTemplate.getForObject(url, GroupSimpleStruct.class, gid);
		} catch (Exception ex) {
			log.error("askGroupFindId error:" + ex.getMessage());
		}

		return null;
	}

	public GroupSimpleStruct askGroupFindName(String name) {
		try {
			String url = guild_prefix + "/findByName?name=" + name;
			return restTemplate.getForObject(url, GroupSimpleStruct.class);
		} catch (Exception ex) {
			log.error("askGroupFindName error:" + ex.getMessage());
		}

		return null;
	}

	public String askGroupDonate(long gid, long role, int progress, int exp, int contrib) {
		try {
			String url = guild_prefix + "/donate?role=" + role + "&gid=" + gid + "&pg=" + progress + "&exp=" + exp + "&cb=" + contrib;
			return restTemplate.getForObject(url, String.class);
		} catch (Exception ex) {
			log.error("askGroupDonate error:" + ex.getMessage());
		}
		return "公会服务维护中";
	}

    public void askDonateSuccessGenerateGuildBag(long gid, long role, int mode) {
        try {
            String url = guild_prefix + "/donateSuccessGenerateGuildBag?gid=" + gid + "&role=" + role + "&mode=" + mode;
            restTemplate.getForObject(url, null);
        } catch (Exception ex) {
            log.error("askDonateSuccess error:" + ex.getMessage());
        }
    }

    public void askPaymentValidation(long gid, long role, int payment) {
        try {
            String url = guild_prefix + "/paymentValidation?gid=" + gid + "&role=" + role + "&payment=" + payment;
            restTemplate.getForObject(url, String.class);
        } catch (Exception ex) {
            log.error("askPaymentValidation error:" + ex.getMessage());
        }
    }

    public GuildBagDtos askGuildBagList(long gid, long role) {
        try {
            String url = guild_prefix + "/guildBagList?gid=" + gid + "&role=" + role;
            return restTemplate.getForObject(url, GuildBagDtos.class);
        } catch (Exception ex) {
            log.error("askGuildBagList error:" + ex.getMessage());
        }
        return null;
    }

    public String askGuildBagReceive(long gid, long role, String name, String bagId) {
        try {
            String url = guild_prefix + "/guildBagReceive?gid=" + gid + "&role=" + role + "&name=" + name + "&bagId=" + bagId;
            return restTemplate.getForObject(url, String.class);
        } catch (Exception ex) {
            log.error("askGuildBagReceive error:" + ex.getMessage());
        }
        return "公会服务维护中";
    }



	public Integer askGroupDonateProgress(long gid, long role) {
		try {
			String url = guild_prefix + "/donateProgress?role=" + role + "&gid=" + gid;
			return restTemplate.getForObject(url, Integer.class);
		} catch (Exception ex) {
			log.error("askGroupDonateProgress error:" + ex.getMessage());
		}
		return 0;
	}

	public GroupSceneStruct askGroupSceneInfo(long gid, long role) {
		try {
			String url = guild_prefix + "/sceneInfo?role=" + role + "&gid=" + gid;
			return restTemplate.getForObject(url, GroupSceneStruct.class);
		} catch (Exception ex) {
			log.error("askGroupSceneInfo error:" + ex.getMessage());
		}
		return null;
	}

	public BattleResultStruct askGroupSceneFight(HerosStruct bs, long gid, int index) {
		try {
			String url = guild_prefix + "/sceneFight?gid=" + gid + "&index=" + index;
			return restTemplate.postForObject(url, bs, BattleResultStruct.class);
		} catch (Exception ex) {
			log.error("askGroupSceneFight error:" + ex.getMessage());
		}
		return null;
	}

	public List<AwardStruct> askGroupSceneAwards(long gid, long role, int index) {
		try {
			String url = guild_prefix + "/sceneAwards?role=" + role + "&gid=" + gid + "&index=" + index;
			return restTemplate.getForObject(url, ArrayList.class);
		} catch (Exception ex) {
			log.error("askGroupSceneAwards error:" + ex.getMessage());
		}
		return null;
	}

	public List<RankStruct> askGroupRanks(int mode) {
		try {
			String url = guild_prefix + "/ranks?mode=" + mode;
			return restTemplate.getForObject(url, ArrayList.class);
		} catch (Exception ex) {
			log.error("askGroupRanks error:" + ex.getMessage());
		}
		return null;
	}

	public AwardStruct askGroupGetSceneAwards(long gid, long role, int index) {
		try {
			String url = guild_prefix + "/getSceneAwards?role=" + role + "&gid=" + gid + "&index=" + index;
			return restTemplate.getForObject(url, AwardStruct.class);
		} catch (Exception ex) {
			log.error("askGroupGetSceneAwards error:" + ex.getMessage());
		}
		return null;
	}

	/**
	 * 请求boss战斗信息
	 * @param uid
	 * @param name
	 * @return
	 */
	public BossDto askBossInfo(long uid, String name) {
		try {
			String url = guild_prefix + "/boss/getInfo?uid={uid}&name={name}";
			return restTemplate.getForObject(url, BossDto.class, uid, name);
		} catch (Exception ex) {
			log.error("请求boss战信息错误:" + ex.getMessage());
		}

		return null;
	}

	/**
	 * 获取boss当前血量
	 * @return 当前的boss血量
	 */
	public long askBossLife() {
		try {
			String url = guild_prefix + "/boss/getLife";
			return restTemplate.getForObject(url, Long.class);
		} catch (Exception ex) {
			log.error("请求boss战血量错误:" + ex.getMessage());
		}

		return -1;
	}

	/**
	 * 获取单人boss战排名
	 * @return
	 */
	public List<BossRoleDto> askBossRanks() {
		try {
			String url = guild_prefix + "/boss/rank";
			return restTemplate.getForEntity(url, ArrayList.class).getBody();
		} catch (Exception ex) {
			log.error("请求boss战单人排名错误:" + ex.getMessage());
		}

		return null;
	}

	/**
	 * 获取公会boss战排名
	 * @return
	 */
	public List<BossGuildDto> askBossGroupRanks() {
		try {
			String url = guild_prefix + "/boss/guildrank";
			return restTemplate.getForEntity(url, ArrayList.class).getBody();
		} catch (Exception ex) {
			log.error("请求boss战公会排名错误:" + ex.getMessage());
		}

		return null;
	}

	/**
	 * 参加boss战斗
	 * @param bs 战斗数据
	 * @return
	 */
	public BattleResultStruct askBossFight(HerosStruct bs) {
		try {
			String url = guild_prefix + "/boss/callFight?uid={uid}";
			return restTemplate.postForObject(url, bs, BattleResultStruct.class, bs.uid);
		} catch (Exception ex) {
			log.error("调用boss战斗 error:" + ex.getMessage());
		}
		return null;
	}

	/**
	 * 上传跨服竞技场玩家数据
	 * @param dto
	 */
	public void arenaSubmitInfo(ArenaRoleDto dto) {
		try {
			String url = prefix + serviceRouter.chooseArena(dto.uid) + "/submitInfo";
			String rtn = restTemplate.postForEntity(url, dto, String.class).getBody();
			if (!Strings.isNullOrEmpty(rtn)) {
				log.error("上传跨服竞技场玩家信息失败:" + rtn);
			}
		} catch (Exception ex) {
			log.error("上传跨服竞技场玩家信息失败:" + ex.getMessage());
		}
	}

	/**
	 * 进入竞技场，需要拉取当前信息
	 * @param uid
	 * @return
	 */
	public ArenaInfo arenaGetInfo(long uid) {
		try {
			String url = prefix + serviceRouter.chooseArena(uid) + "/getInfo?uid=" + uid;
			return restTemplate.getForEntity(url, ArenaInfo.class).getBody();
		} catch (Exception ex) {
			log.error("上传跨服竞技场玩家信息失败.");
		}
		return null;
	}

	/**
	 * 跨服竞技场挑战
	 * @param uid
	 * @return
	 */
	public ArenaFightResult arenaFight(long uid, int peer) {
		try {
			String url = prefix + serviceRouter.chooseArena(uid) + "/fight?uid={uid}&peer={peer}";
			return restTemplate.getForEntity(url, ArenaFightResult.class, uid, peer).getBody();
		} catch (Exception ex) {
			log.error("跨服竞技场维护中.");
		}
		return null;
	}

	/**
	 * 天梯赛信息
	 * @param uid
	 * @return
	 */
	public LadderInfoDto arenaLadder(long uid) {
		try {
			String url = prefix + serviceRouter.chooseArena(uid) + "/ladder?uid={uid}";
			return restTemplate.getForEntity(url, LadderInfoDto.class, uid).getBody();
		} catch (Exception ex) {
			log.error("跨服竞技场维护中.");
		}
		return null;
	}

	/**
	 * 天梯赛选择分组
	 * @param uid
	 * @return
	 */
	public int arenaChooseSlot(long uid, int slot) {
		try {
			String url = prefix + serviceRouter.chooseArena(uid) + "/chooseSlot?uid={uid}&slot={slot}";
			return restTemplate.getForEntity(url, Integer.class, uid, slot).getBody();
		} catch (Exception ex) {
			log.error("跨服竞技场维护中.");
		}
		return -1;
	}

	/**
	 * 天梯赛获取累计的荣誉
	 *
	 * @param uid
	 * @return
	 */
	public LadderHonorDto arenaHonor(long uid) {
		try {
			String url = prefix + serviceRouter.chooseArena(uid) + "/getHonor?uid={uid}";
			return restTemplate.getForEntity(url, LadderHonorDto.class, uid).getBody();
		} catch (Exception ex) {
			log.error("arenaHonor error:" + ex.getMessage());
		}
		return null;
	}

	public List<LadderVideoDto> arenaVideos(long uid) {
		try {
			String url = prefix + serviceRouter.chooseArena(uid) + "/getVideos?uid={uid}";
			return restTemplate.getForEntity(url, ArrayList.class, uid).getBody();
		} catch (Exception ex) {
			log.error("arenaVideos error:" + ex.getMessage());
		}
		return null;
	}


	/**
	 * 天梯赛挑战
	 *
	 * @param uid
	 * @return
	 */
	public LadderFightResult arenaLadderFight(long uid, long pid, int group, int index) {
		try {
			String url = prefix + serviceRouter.chooseArena(uid) + "/fightLadder?uid={uid}&pid={pid}&group={group}&index={index}";
			return restTemplate.getForEntity(url, LadderFightResult.class, uid, pid, group, index).getBody();
		} catch (Exception ex) {
			log.error("arenaLadderFight error:" + ex.getMessage());
		}
		return null;
	}


	/**
	 * 发送全局和公会聊天消息消息
	 * @return
	 */
	public void sendAllMessage(MessageDto msg, long group) {
		try {
			HttpEntity<MessageDto> he = new HttpEntity<>(msg, headers);
			String url = guild_prefix + "/sendAllMessage?group={group}";
			restTemplate.exchange(url, HttpMethod.POST, he, String.class, group);
		} catch (Exception ex) {
			log.error("发送跨服聊天消息失败:" + ex.getMessage());
		}
	}

}
