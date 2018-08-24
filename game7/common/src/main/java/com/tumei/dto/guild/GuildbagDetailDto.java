package com.tumei.dto.guild;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zw on 2018/08/22
 */

public class GuildbagDetailDto {
    public GuildbagDetailDto() {
    }

    /**
     * 领取过奖励的玩家资源信息 uid-name-count-source
     */
    public HashMap<Long,GuildbagRoleDto> ids = new HashMap<>();

    public int id;
    public int count;
    public int remaining;

}
