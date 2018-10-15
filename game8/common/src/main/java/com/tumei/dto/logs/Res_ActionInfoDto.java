package com.tumei.dto.logs;

import java.io.Serializable;

/**
 * Created by zw on 2018/10/12
 */
public class Res_ActionInfoDto implements Serializable {
    /**
     * 剩余数量
     */
    public long left_num;
    /**
     * int64,时间戳，13位，精确到毫秒
     */
    public long create_time;
    /**
     * 变更数量
     */
    public int change_num;
    /**
     * 变更类型：0->增加，1->减少
     */
    public int change_type;
    /**
     * 模块id
     */
    public int module_id;
    /**
     * 行为id
     */
    public int action_id;
}
