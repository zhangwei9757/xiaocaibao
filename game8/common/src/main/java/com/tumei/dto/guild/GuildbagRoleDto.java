package com.tumei.dto.guild;

public class GuildbagRoleDto {
    public GuildbagRoleDto() {
    }

    public GuildbagRoleDto(long uid, String name, int count, int source) {
        this.uid = uid;
        this.name = name;
        this.count = count;
        this.source = source;
    }

    public long uid;
    public String name;
    public int count;
    public int source;
}
