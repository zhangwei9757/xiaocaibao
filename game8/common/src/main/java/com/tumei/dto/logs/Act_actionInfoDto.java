package com.tumei.dto.logs;

public class Act_actionInfoDto {
    /**
     * 时间戳，13位，精确到毫秒
     */
    public long create_time;
    /**
     * 模块id
     */
    public int module_id;
    /**
     * 行为id
     */
    public int action_id;
    /**
     * 行为涉及参数1[string，备用行为涉及的参数，没有填空]
     */
    public String parama;
    public String paramb;
    public String paramc;
    public String paramd;
    public String parame;
    public String paramf;
    public String paramg;
    public String paramh;
    public String parami;
    /**
     * 行为涉及参数10[string，备用行为涉及的参数，没有填空]
     */
    public String paramj;
}

