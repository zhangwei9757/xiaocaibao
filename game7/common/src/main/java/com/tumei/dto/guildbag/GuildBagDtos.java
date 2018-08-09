package com.tumei.dto.guildbag;

import java.util.ArrayList;
import java.util.List;
/**
 * Created by zw on 2018-08-07
 * */
public class GuildBagDtos {
    /**
     * 公会id
     * */
    public Long id;

    /**
     * 本公会对应的所有红包
     */
    public List<GuildbagDto> guildBags = new ArrayList<>();
}
