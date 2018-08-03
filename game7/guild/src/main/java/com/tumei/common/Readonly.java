package com.tumei.common;

import com.tumei.configs.IReadonly;
import com.tumei.modelconf.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2017/1/19 0019.
 * <p>
 * 只读配置的预加载，可以在运行时异步加载
 */
@Service
public class Readonly extends IReadonly {
    private static Readonly _instance = null;

    public static Readonly getInstance() {
        return _instance;
    }

    private static Log log = LogFactory.getLog(Readonly.class);

    class Conf {
        public HashMap<Integer, GroupConf> groups = new HashMap<>();

        public HashMap<Integer, GuildraidConf> guildraids = new HashMap<>();

        public List<BossConf> bossConfs;

        public List<BossrankConf> bossrankConfs;

        public List<GuildbagConf> guildbagConfs;

        public void initialize() {
            {
                List<GroupConf> tmp = mongoTemplate.findAll(GroupConf.class);
                tmp.forEach(gc -> groups.put(gc.key, gc));
            }
            {
                List<GuildraidConf> tmp = mongoTemplate.findAll(GuildraidConf.class);
                tmp.forEach(gc -> guildraids.put(gc.key, gc));
            }

            {
                bossConfs = mongoTemplate.findAll(BossConf.class);
                bossConfs.sort((o1, o2) -> {
                    if (o1.key < o2.key) {
                        return -1;
                    } else if (o1.key > o2.key) {
                        return 1;
                    }
                    return 0;
                });
            }

            {
                bossrankConfs = mongoTemplate.findAll(BossrankConf.class);
                bossrankConfs.sort((o1, o2) -> {
                    if (o1.key < o2.key) {
                        return -1;
                    } else if (o1.key > o2.key) {
                        return 1;
                    }
                    return 0;
                });
            }

            {
                guildbagConfs = mongoTemplate.findAll(GuildbagConf.class);
                guildbagConfs.sort((o1, o2) -> {
                    if (o1.key < o2.key) {
                        return -1;
                    } else if (o1.key > o2.key) {
                        return 1;
                    }
                    return 0;
                });

            }
        }
    }

    private Conf conf;

    @PostConstruct
    public void init() {
        _instance = this;
        refresh();
    }

    /**
     * 带锁刷新只读表到内存中
     */
    @Override
    public void refresh() {
        log.info("--- 刷新公会配置 ---");

        super.refresh();

        Conf _conf = new Conf();
        _conf.initialize();
        this.conf = _conf;

        log.info("--- 结束刷新公会配置 ---");
    }

    /**
     * 公会等级信息
     *
     * @param level
     * @return
     */
    public GroupConf findGroup(int level) {
        return conf.groups.get(level);
    }

    /**
     * 公会副本
     *
     * @param level
     * @return
     */
    public GuildraidConf findGuildraid(int level) {
        return conf.guildraids.get(level);
    }

    /**
     * 获取对应等级的boss状态数据
     *
     * @param level [0, ...] 标识boss的等级
     * @return
     */
    public BossConf findBossConf(int level) {
        return conf.bossConfs.get(level - 1);
    }

    /**
     * 获取所有boss的排名奖励,然后调用端负责顺序发送奖励
     *
     * @return
     */
    public List<BossrankConf> getBossRanks() {
        return conf.bossrankConfs;
    }

    /***
     * 获取公会红包的配置信息
     *
     *
     * @return
     * */
    public List<GuildbagConf> findGuildbagConfs() {
        return conf.guildbagConfs;
    }
}
