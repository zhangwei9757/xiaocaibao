package com.tumei.dto.logs;

import java.io.Serializable;

/**
 * Created by zw on 2018/10/12
 */
public class Res_ResInfoDto implements Serializable {
    /**
     * 资源类型
     */
    public int res_category;
    /**
     * 资源唯一标识值，如多个相同资源时，可以进行区分等等
     */
    public int res_extral_key;
    /**
     * 资源编号
     */
    public int res_code;
}
