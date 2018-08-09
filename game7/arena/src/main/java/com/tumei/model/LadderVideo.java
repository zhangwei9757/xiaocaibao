package com.tumei.model;

import com.tumei.dto.arena.LadderVideoDto;

/**
 * Created by Leon on 2017/11/23 0023.
 */
public class LadderVideo {
	public int zone;
	public int grade;
	public String name = "";
	public String data;
	public long time;
	public int win;

	public LadderVideoDto createDto() {
		LadderVideoDto dto = new LadderVideoDto();
		dto.zone = zone;
		dto.grade = grade;
		dto.name = name;
		dto.data = data;
		dto.time = time;
		dto.win = win;
		return dto;
	}
}
