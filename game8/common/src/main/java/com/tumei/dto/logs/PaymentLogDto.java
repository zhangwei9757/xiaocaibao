package com.tumei.dto.logs;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by zw on 2018/10/15
 * <p>
 * 充值 日志
 */
public class PaymentLogDto {
    @JsonIgnore
    public String log_type;

    public String log_id;
    /**
     * 外部订单号（渠道产生的）
     */
    public String order_no;
    /**
     * 时间戳，13位，精确到毫秒，充值时间
     */
    public long create_time;
    /**
     * 枚举，状态：0->普通，1->网页，2-> GMTool，3->后台补发充值
     */
    public int recharge_type;
    /**
     * 枚举，状态：0->失败，1->正常，2->进行中
     */
    public int status;
    /**
     * 充值项目
     */
    public String recharge_id;

    public int recharge_item_type;
    /**
     * 以分为单位
     */
    public int money;
    /**
     * 枚举，币种类型：'CNY' - >人民币，'JPY' - >日元
     */
    public String money_type;
    /**
     * 充值项名称，如，钻石60
     */
    public String recharge_name;
    /**
     * 获得钻石数，不包括额外获得
     */
    public int recharge_diamond;
    /**
     * 玩家钻石数，如果充值成功为充值前钻石数+充值正常获得钻石+额外赠送钻石
     */
    public int diamond;
    /**
     * 枚举，是否全局首冲：0->不是，1->是
     */
    public int is_first_all;
    /**
     * 枚举，是否档位首冲：0->不是，1->是
     */
    public int is_first_team;

    public AccountInfoDto account_info;
    public Res_RoleInfoDto role_info;
}
