package com.tumei.dto.yunding;

public class YdPayNotify {
    public String sign;
    public int timestamp;
    public String app_id;
    public String server_id;
    public String account_id;
    public String role_id;
    public String role_name;
    public int role_level;
    public int vip_level;
    public int vip_exp;
    public String order_id;
    public String iap_id;
    public String iap_des;
    public double currency_amount;//充值人民币 元
    public int currency_type = 1;
    public int platform_id;
    public int access_type = 1;
    public double virtual_currency_amount; // 代币
    public double virtual_currency_amount_ex; // 附赠代币
    public String ip;
}
