package com.tumei.dto.logs;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by zw on 2018/10/12
 */
public class DeviceInfoDto {
    /**
     * 枚举，平台类型：1-> iOS，2-> android，3-> 其他
     */
    public int pt;
    /**
     * 操作系统信息，ios 11.0.1，android 6.0.1等
     */
    public String os_info;
    /**
     * 串，设备类型，ipad，iPhone、HUAWEI、小米、OPPO、VIVO等
     */
    public String device_type;
    /**
     * 设备型号iPhone6s，ipad mini2，oppoR9，vivo x7等等
     */
    public String device_model;
    /**
     * 设备唯一标识
     */
    public String udid;
    /**
     * 设备ip地址
     */
    @JsonIgnore
    public String ip;
}
