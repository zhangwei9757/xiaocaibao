package com.tumei.dto.limit;

import com.tumei.common.webio.AwardStruct;

import java.util.List;

/**
 * Created by zw on 2018/09/28
 */
public class InvadingLoginDto {
    public List<AwardStruct> awards;
    /**
     * 登陆状态:
     * -1表示已过期需要补签
     * 0表示未领取
     * 1表示已领取
     * 2表示已购买
     * 3未到领取日期状态
     */
    public int status;
    /**
     * 当天补签所需要的钻石费用
     */
    public int resign;

    public int ddagain;
}
