package com.tumei.model;

import com.tumei.common.utils.RandomUtil;
import com.tumei.dto.guild.GuildbagBasicDto;
import com.tumei.dto.guild.GuildbagDetailDto;
import com.tumei.dto.guild.GuildbagRoleDto;

import java.util.HashMap;
import java.util.Map;

public class GuildbagStruct {

    public GuildbagStruct() {
        for (int i = 0; i < this.resouce.length; ++i) {
            float between = RandomUtil.getBetween(1, 10000);
            this.resouce[i] = between;
            this.sum += between;
        }
        for (int i = 0; i < this.resouce.length; ++i) {
            this.resouce[i] = resouce[i] / sum;
        }
        sum = 0;
    }

    /**
     * 红包id：当前时间 - 红包类型
     **/
    public String bagId;

    /**
     * 0：未生成红包
     * 1：已生成红包，未开启
     * 2：已开启红包，待领取
     * 3：已领取红包，只可读信息
     **/
    public int status;

    /**
     * 配置表key
     */
    public int key;
    /**
     * 红包类型
     **/
    public int mode;
    /**
     * 红包生成所属公会
     */
    public long gid;
    /**
     * 领取过奖励的玩家资源信息 uid-name-count-source
     */
    public Map<Long, GuildbagStruct.IDS> ids = new HashMap<>();

    public class IDS {
        public IDS(long uid, String name, int count, int source) {
            this.uid = uid;
            this.name = name;
            this.count = count;
            this.source = source;
        }

        public long uid;
        public String name;
        public int count;
        public int source;
    }

    public void addIDS(long uid, String name, int count, int source) {
        ids.put(uid, new GuildbagStruct.IDS(uid, name, count, source));
    }

    /**
     * 激活红包玩家的呢称
     **/
    public String openName;

    /**
     * 剩余可领取红包个数
     **/
    public int count;
    /**
     * 未开启红包，最后可开启时间
     * 0 :表示非生成状态
     **/
    public long openLast;
    /**
     * 已开启红包，最后领取时间
     * 0：表示非开启状态
     */
    public long existLast;
    /**
     * 开启所需充值金额
     **/
    public long money;
    /**
     * 已被领取奖励总和
     */
    public int reward;

    /**
     * 红包资源信息左id ，右数量
     * */
    public int [] sources = new int[2];

    /**
     * 获取指定玩家已领取红包的资源信息
     * 随机十次生成 1-10000
     */
    public float[] resouce = new float[10];
    public float sum;

    public GuildbagBasicDto createBasic() {
        return new GuildbagBasicDto(bagId, status, key, mode, gid, openName, count, openLast, existLast, money, reward, sources);
    }

    public GuildbagDetailDto createDetail() {
        GuildbagDetailDto gdd = new GuildbagDetailDto();
        ids.values().forEach(_ids -> {
            GuildbagRoleDto r = new GuildbagRoleDto(_ids.uid,_ids.name,_ids.count,_ids.source);
            gdd.ids.put(r.uid,r);
        });
        return gdd;
    }
}
