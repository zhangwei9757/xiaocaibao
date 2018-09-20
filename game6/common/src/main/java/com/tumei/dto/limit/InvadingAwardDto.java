package com.tumei.dto.limit;

import com.tumei.common.webio.AwardStruct;

import java.util.List;

/**
 * Created by zw on 2018/09/14
 */
public class InvadingAwardDto {
    public int cost;
    public List<AwardStruct> awards;
    /**
     * 领取状态：
     * -1：表示充值金额未达标
     * 0：表示充值金额已达标未领取
     * 1：表示充值金额已达标已领取
     */
    public int status;
}
